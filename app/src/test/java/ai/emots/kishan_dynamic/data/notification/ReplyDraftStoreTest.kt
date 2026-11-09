package ai.emots.kishan_dynamic.data.notification

import org.junit.Assert.*
import org.junit.Test

class ReplyDraftStoreTest {
    @Test fun sourceAndActionIdentityRetainDraftOnlyInsideMonotonicWindow() {
        var now=100L; val store=ReplyDraftStore { now }
        store.write("one","reply","Unsent draft")
        assertEquals("Unsent draft",store.read("one","reply"))
        assertEquals("",store.read("two","reply"));assertEquals("",store.read("one","replacement"))
        now+=60_000; assertEquals("",store.read("one","reply"))
    }
    @Test fun privacyInvalidationAndSourceRemovalPreventResurrection() {
        val store=ReplyDraftStore { 0L }
        store.write("one","reply","private");store.privacyRestricted(true)
        store.write("one","reply","stale callback");store.privacyRestricted(false)
        assertEquals("",store.read("one","reply"))
        store.write("two","reply","draft");store.retainSources(emptySet())
        assertEquals("",store.read("two","reply"))
    }
}
