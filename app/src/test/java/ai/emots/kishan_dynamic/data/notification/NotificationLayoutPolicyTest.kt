package ai.emots.kishan_dynamic.data.notification

import ai.emots.kishan_dynamic.data.model.NotificationActionInfo
import ai.emots.kishan_dynamic.data.model.NotificationActionKind
import ai.emots.kishan_dynamic.data.model.NotificationInfo
import ai.emots.kishan_dynamic.data.model.NotificationMessageInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Universal adaptive notification contract: recipe selection from typed source
 * capabilities only, typed action presentation, and honest height estimates.
 */
class NotificationLayoutPolicyTest {

    private fun source(id: String = "n1") = NotificationInfo(
        id = id,
        packageName = "com.test.app",
        appName = "TestApp",
        title = "Title",
        text = "Body"
    )

    // --- Recipe selection ---

    @Test
    fun plainSource_fallsBackToTextAlert() {
        assertEquals(NotificationLayoutRecipe.TEXT_ALERT, NotificationLayoutPolicy.recipe(source()))
    }

    @Test
    fun emptySource_stillResolvesCleanFallback() {
        val empty = source().copy(title = "", text = "", expandedText = "")
        assertEquals(NotificationLayoutRecipe.TEXT_ALERT, NotificationLayoutPolicy.recipe(empty))
        assertEquals("TestApp", NotificationLayoutPolicy.headline(empty))
    }

    @Test
    fun conversationWithPicture_usesThumbnailAlert() {
        val item = source().copy(picturePath = "content://pic", isConversation = true)
        assertEquals(NotificationLayoutRecipe.THUMBNAIL_ALERT, NotificationLayoutPolicy.recipe(item))
    }

    @Test
    fun bigPictureWithoutConversation_usesLargeMedia() {
        val item = source().copy(picturePath = "content://pic")
        assertEquals(NotificationLayoutRecipe.LARGE_MEDIA, NotificationLayoutPolicy.recipe(item))
    }

    @Test
    fun conversationHistory_usesConversationRecipe() {
        val item = source().copy(
            isConversation = true,
            conversationTitle = "Design team",
            messages = listOf(NotificationMessageInfo("Alex", "Hello", 1L))
        )
        assertEquals(NotificationLayoutRecipe.CONVERSATION, NotificationLayoutPolicy.recipe(item))
    }

    @Test
    fun multipleInboxLines_usesInboxDigest() {
        val item = source().copy(inboxLines = listOf("line one", "line two"))
        assertEquals(NotificationLayoutRecipe.INBOX_DIGEST, NotificationLayoutPolicy.recipe(item))
    }

    @Test
    fun singleInboxLine_staysTextAlert() {
        val item = source().copy(inboxLines = listOf("only line"))
        assertEquals(NotificationLayoutRecipe.TEXT_ALERT, NotificationLayoutPolicy.recipe(item))
    }

    @Test
    fun determinateProgress_usesProgressRecipe() {
        val item = source().copy(progress = 40, progressMax = 100)
        assertEquals(NotificationLayoutRecipe.PROGRESS, NotificationLayoutPolicy.recipe(item))
    }

    @Test
    fun indeterminateProgress_usesProgressRecipe() {
        val item = source().copy(isProgressIndeterminate = true)
        assertEquals(NotificationLayoutRecipe.PROGRESS, NotificationLayoutPolicy.recipe(item))
    }

    @Test
    fun chronometer_usesTimerRecipe() {
        assertEquals(
            NotificationLayoutRecipe.TIMER,
            NotificationLayoutPolicy.recipe(source().copy(showChronometer = true))
        )
        assertEquals(
            NotificationLayoutRecipe.TIMER,
            NotificationLayoutPolicy.recipe(source().copy(category = "timer"))
        )
    }

    @Test
    fun ongoingNavigation_usesNavigationRecipe() {
        val item = source().copy(isOngoing = true, category = "navigation")
        assertEquals(NotificationLayoutRecipe.NAVIGATION, NotificationLayoutPolicy.recipe(item))
    }

    @Test
    fun nonOngoingNavigation_staysTextAlert() {
        val item = source().copy(isOngoing = false, category = "navigation")
        assertEquals(NotificationLayoutRecipe.TEXT_ALERT, NotificationLayoutPolicy.recipe(item))
    }

    @Test
    fun groupSummary_usesGroupRecipe() {
        val item = source().copy(isGroupSummary = true, groupChildCount = 7)
        assertEquals(NotificationLayoutRecipe.GROUP_SUMMARY, NotificationLayoutPolicy.recipe(item))
    }

    @Test
    fun progressCapability_outranksPictureAndConversation() {
        val item = source().copy(
            picturePath = "content://pic",
            isConversation = true,
            progressMax = 100,
            progress = 10
        )
        assertEquals(NotificationLayoutRecipe.PROGRESS, NotificationLayoutPolicy.recipe(item))
    }

    // --- Typed action presentation ---

    @Test
    fun replyAction_isPrimaryOnlyAsFirstForwardAction() {
        val reply = NotificationActionInfo("a1", "Reply", isReply = true)
        val primary = NotificationLayoutPolicy.actionPresentation(reply, isFirstForwardAction = true)
        assertEquals(NotificationActionVisual.PRIMARY, primary.style)
        assertEquals(NotificationActionGlyph.CHAT, primary.glyph)
        assertTrue(primary.isPrimary)

        val second = NotificationActionInfo("a2", "Open", kind = NotificationActionKind.OPEN)
        val presentation = NotificationLayoutPolicy.actionPresentation(second, isFirstForwardAction = false)
        assertFalse(presentation.isPrimary)
    }

    @Test
    fun localizedReplyLabel_withoutCapability_isNotPrimary() {
        val generic = NotificationActionInfo("a1", "Répondre")
        val presentation = NotificationLayoutPolicy.actionPresentation(generic, isFirstForwardAction = true)
        assertEquals(NotificationActionVisual.SECONDARY, presentation.style)
        assertFalse(presentation.isPrimary)
    }

    @Test
    fun englishLabelNeverManufacturesSemantics() {
        val deleteLookalike = NotificationActionInfo("a1", "Delete forever")
        assertEquals(
            NotificationActionVisual.SECONDARY,
            NotificationLayoutPolicy.actionPresentation(deleteLookalike, isFirstForwardAction = true).style
        )
        val replyLookalike = NotificationActionInfo("a2", "Reply later")
        assertFalse(NotificationLayoutPolicy.isForwardAction(replyLookalike))
    }

    @Test
    fun semanticKinds_mapToTypedVisuals() {
        val cases = mapOf(
            NotificationActionKind.OPEN to NotificationActionVisual.PRIMARY,
            NotificationActionKind.CALL to NotificationActionVisual.POSITIVE,
            NotificationActionKind.DELETE to NotificationActionVisual.DESTRUCTIVE,
            NotificationActionKind.ARCHIVE to NotificationActionVisual.SECONDARY,
            NotificationActionKind.MARK_READ to NotificationActionVisual.SECONDARY,
            NotificationActionKind.MUTE to NotificationActionVisual.SECONDARY,
            NotificationActionKind.THUMBS_UP to NotificationActionVisual.SECONDARY,
            NotificationActionKind.THUMBS_DOWN to NotificationActionVisual.SECONDARY
        )
        cases.forEach { (kind, visual) ->
            val action = NotificationActionInfo("id-${kind.name}", "Label", kind = kind)
            assertEquals(
                "Kind $kind must map to $visual",
                visual,
                NotificationLayoutPolicy.actionPresentation(action, isFirstForwardAction = true).style
            )
        }
    }

    @Test
    fun legacyFlags_stillResolveTypedKinds() {
        assertEquals(
            NotificationActionKind.REPLY,
            NotificationLayoutPolicy.effectiveKind(NotificationActionInfo("a", "X", isReply = true))
        )
        assertEquals(
            NotificationActionKind.DELETE,
            NotificationLayoutPolicy.effectiveKind(NotificationActionInfo("a", "X", isDestructive = true))
        )
    }

    // --- Copy resolution ---

    @Test
    fun bodyText_prefersInboxThenExpandedThenText() {
        val item = source().copy(
            text = "short",
            expandedText = "expanded",
            inboxLines = listOf("line a", "line b")
        )
        assertEquals("line a\nline b", NotificationLayoutPolicy.bodyText(item))
        assertEquals("expanded", NotificationLayoutPolicy.bodyText(item.copy(inboxLines = emptyList())))
        assertEquals("short", NotificationLayoutPolicy.bodyText(item.copy(inboxLines = emptyList(), expandedText = "")))
    }

    @Test
    fun headline_prefersConversationTitleOnlyForConversations() {
        val conversation = source().copy(isConversation = true, conversationTitle = "Design team")
        assertEquals("Design team", NotificationLayoutPolicy.headline(conversation))
        assertEquals("Design team", NotificationLayoutPolicy.headline(conversation.copy(title = "Updated title")))
        val nonConversation = conversation.copy(isConversation = false)
        assertEquals("Title", NotificationLayoutPolicy.headline(nonConversation))
    }

    // --- Height estimates ---

    @Test
    fun estimates_keepFamilyFloorAndReplySheet() {
        assertTrue(NotificationLayoutPolicy.estimatePageHeight(source()).value >= 86f)
        assertEquals(86f, NotificationLayoutPolicy.estimatePageHeight(source()).value, 0.01f)
        assertEquals(166f, NotificationLayoutPolicy.estimatePageHeight(source(), isReplying = true).value, 0.01f)
    }

    @Test
    fun thumbnailEstimate_matchesFigma126() {
        val item = source().copy(picturePath = "content://pic", isConversation = true)
        assertEquals(126f, NotificationLayoutPolicy.estimatePageHeight(item).value, 0.01f)
    }

    @Test
    fun largeMediaEstimate_neverBelow126() {
        val item = source().copy(picturePath = "content://pic")
        assertTrue(NotificationLayoutPolicy.estimatePageHeight(item).value >= 126f)
    }

    @Test
    fun multilineText_addsMeasuredLineAllowance() {
        val item = source().copy(text = "one\ntwo\nthree", expandedText = "one\ntwo\nthree")
        assertEquals(104.8f, NotificationLayoutPolicy.estimatePageHeight(item).value, 0.01f)
    }

    @Test
    fun actions_reserveOneRow() {
        val item = source().copy(actions = listOf(NotificationActionInfo("a", "Reply", isReply = true)))
        assertEquals(134f, NotificationLayoutPolicy.estimatePageHeight(item).value, 0.01f)
    }

    @Test
    fun mediaRecipes_doNotDoubleCountTextLines() {
        val item = source().copy(
            picturePath = "content://pic",
            isConversation = true,
            text = "one\ntwo\nthree"
        )
        assertEquals(126f, NotificationLayoutPolicy.estimatePageHeight(item).value, 0.01f)
    }
}