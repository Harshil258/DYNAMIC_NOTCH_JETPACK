package ai.emots.kishan_dynamic.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ai.emots.kishan_dynamic.data.model.AppLanguage
import ai.emots.kishan_dynamic.data.model.AppLanguages
import ai.emots.kishan_dynamic.data.preferences.AuroraPreferences
import ai.emots.kishan_dynamic.ui.components.AppText
import ai.emots.kishan_dynamic.ui.components.AppleGlyph
import ai.emots.kishan_dynamic.ui.components.AppleIcon
import ai.emots.kishan_dynamic.ui.kit.AppButton
import ai.emots.kishan_dynamic.ui.kit.AppFootnote
import ai.emots.kishan_dynamic.ui.kit.AppListCard
import ai.emots.kishan_dynamic.ui.kit.AppRowDivider
import ai.emots.kishan_dynamic.ui.kit.AppScreen
import ai.emots.kishan_dynamic.ui.kit.AppSectionSpacer
import ai.emots.kishan_dynamic.ui.kit.AppSectionTitle
import ai.emots.kishan_dynamic.ui.kit.AppTopBar
import ai.emots.kishan_dynamic.ui.motion.AppMotion
import ai.emots.kishan_dynamic.ui.motion.appReveal
import ai.emots.kishan_dynamic.ui.motion.rememberPressScale
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import kotlinx.coroutines.launch

/**
 * LANGUAGE — a first-class screen rather than a list buried in Settings.
 *
 * Structure: hero of the current language → suggested cards → live search →
 * the full alphabetical list. Selection is applied instantly and confirmed
 * with an animated check, so the screen never needs a "save" round-trip.
 */
@Composable
fun LanguageScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val preferences = remember { AuroraPreferences(context) }

    val savedLanguage by preferences.languageCode.collectAsState(initial = "en")
    var query by remember { mutableStateOf("") }

    val results = remember(query) { AppLanguages.search(query) }
    val current = AppLanguages.byCode(savedLanguage) ?: AppLanguages.all.first()

    fun select(language: AppLanguage) {
        scope.launch { preferences.setLanguageCode(language.code) }
    }

    AppScreen {
        AppTopBar(
            title = "Language",
            subtitle = "Choose how the app speaks to you.",
            onBack = onBack,
            modifier = Modifier.appReveal(0)
        )

        LanguageHero(
            language = current,
            modifier = Modifier.appReveal(1)
        )

        AppSectionSpacer()

        AnimatedVisibility(
            visible = query.isEmpty(),
            enter = fadeIn(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column {
                AppSectionTitle("Suggested")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
                ) {
                    AppLanguages.suggested.forEach { language ->
                        SuggestedLanguageCard(
                            language = language,
                            selected = language.code == savedLanguage,
                            onClick = { select(language) }
                        )
                    }
                }
                AppSectionSpacer()
            }
        }

        LanguageSearchField(
            query = query,
            onQueryChange = { query = it },
            modifier = Modifier.appReveal(2)
        )

        Spacer(modifier = Modifier.height(AppTheme.spacing.lg))

        AppSectionTitle(
            if (query.isEmpty()) "All languages" else "${results.size} result${if (results.size == 1) "" else "s"}"
        )

        if (results.isEmpty()) {
            EmptyLanguageState(query = query, onClear = { query = "" })
        } else {
            AppListCard(modifier = Modifier.appReveal(3)) {
                results.forEachIndexed { index, language ->
                    LanguageRow(
                        language = language,
                        selected = language.code == savedLanguage,
                        onClick = { select(language) }
                    )
                    if (index < results.lastIndex) AppRowDivider()
                }
            }
        }

        AppFootnote(
            "Island text, dialogs and live activities all follow this setting. " +
                "Some system-provided names stay in their original language."
        )
    }
}

// =============================================================================
// Hero
// =============================================================================

@Composable
private fun LanguageHero(
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(AppTheme.radius.card)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(
                        AppTheme.colors.accent.copy(alpha = 0.20f),
                        AppTheme.colors.surfaceVariant
                    )
                )
            )
            .border(0.5.dp, AppTheme.colors.border, shape)
            .padding(AppTheme.layout.cardPadding)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(AppTheme.colors.accent.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center
            ) {
                AppleIcon(
                    glyph = AppleGlyph.Globe,
                    tint = AppTheme.colors.accent,
                    size = 24.dp
                )
            }
            Spacer(modifier = Modifier.width(AppTheme.spacing.lg))
            Column(modifier = Modifier.weight(1f)) {
                AppText(
                    text = language.name,
                    style = AppTheme.typography.h2,
                    color = AppTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(AppTheme.spacing.xxs))
                AppText(
                    text = "${language.english} · ${language.region}",
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.textSecondary,
                    maxLines = 1
                )
            }
        }
    }
}

// =============================================================================
// Suggested card
// =============================================================================

@Composable
private fun SuggestedLanguageCard(
    language: AppLanguage,
    selected: Boolean,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val scale by rememberPressScale(interaction, pressedScale = 0.95f)
    val selection by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = AppMotion.selectionSpring(),
        label = "suggested_selection"
    )
    val shape = RoundedCornerShape(AppTheme.radius.lg)

    Column(
        modifier = Modifier
            .width(124.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .background(
                if (selected) AppTheme.colors.accent.copy(alpha = 0.16f)
                else AppTheme.colors.surfaceVariant
            )
            .border(
                0.5.dp,
                if (selected) AppTheme.colors.accent.copy(alpha = 0.55f) else AppTheme.colors.border,
                shape
            )
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(AppTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xxs)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppText(
                text = language.code.uppercase(),
                style = AppTheme.typography.caption,
                fontWeight = FontWeight.SemiBold,
                color = if (selected) AppTheme.colors.accent else AppTheme.colors.textTertiary,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .graphicsLayer {
                        alpha = selection
                        scaleX = 0.6f + 0.4f * selection
                        scaleY = 0.6f + 0.4f * selection
                    },
                contentAlignment = Alignment.Center
            ) {
                AppleIcon(
                    glyph = AppleGlyph.Check,
                    tint = AppTheme.colors.accent,
                    size = 14.dp
                )
            }
        }
        AppText(
            text = language.name,
            style = AppTheme.typography.h3,
            color = AppTheme.colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        AppText(
            text = language.english,
            style = AppTheme.typography.caption,
            color = AppTheme.colors.textSecondary,
            maxLines = 1
        )
    }
}

// =============================================================================
// Search
// =============================================================================

@Composable
private fun LanguageSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(AppTheme.radius.pill)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(shape)
            .background(AppTheme.colors.surfaceVariant)
            .border(0.5.dp, AppTheme.colors.border, shape)
            .padding(horizontal = AppTheme.spacing.lg),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppleIcon(
            glyph = AppleGlyph.Search,
            tint = AppTheme.colors.textTertiary,
            size = 15.dp
        )
        Spacer(modifier = Modifier.width(AppTheme.spacing.sm))
        Box(modifier = Modifier.weight(1f)) {
            if (query.isEmpty()) {
                AppText(
                    text = "Search languages",
                    style = AppTheme.typography.body,
                    color = AppTheme.colors.textTertiary
                )
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = AppTheme.typography.body.copy(color = AppTheme.colors.textPrimary),
                cursorBrush = SolidColor(AppTheme.colors.accent),
                modifier = Modifier.fillMaxWidth()
            )
        }
        AnimatedVisibility(
            visible = query.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(AppTheme.colors.surfaceElevated)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onQueryChange("") },
                contentAlignment = Alignment.Center
            ) {
                AppleIcon(
                    glyph = AppleGlyph.Close,
                    tint = AppTheme.colors.textSecondary,
                    size = 10.dp
                )
            }
        }
    }
}

// =============================================================================
// Rows & empty state
// =============================================================================

@Composable
private fun LanguageRow(
    language: AppLanguage,
    selected: Boolean,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val selection by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = AppMotion.selectionSpring(),
        label = "row_selection"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .heightIn(min = 58.dp)
            .padding(
                horizontal = AppTheme.layout.cardPadding,
                vertical = AppTheme.spacing.sm
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (selected) AppTheme.colors.accent.copy(alpha = 0.18f)
                    else AppTheme.colors.surfaceVariant
                ),
            contentAlignment = Alignment.Center
        ) {
            AppText(
                text = language.code.uppercase(),
                style = AppTheme.typography.caption,
                fontWeight = FontWeight.SemiBold,
                color = if (selected) AppTheme.colors.accent else AppTheme.colors.textTertiary
            )
        }

        Spacer(modifier = Modifier.width(AppTheme.spacing.md))

        Column(modifier = Modifier.weight(1f)) {
            AppText(
                text = language.name,
                style = AppTheme.typography.body,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = AppTheme.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(AppTheme.spacing.xxs))
            AppText(
                text = language.english,
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.textSecondary,
                maxLines = 1
            )
        }

        Box(
            modifier = Modifier
                .size(22.dp)
                .graphicsLayer {
                    alpha = selection
                    scaleX = 0.5f + 0.5f * selection
                    scaleY = 0.5f + 0.5f * selection
                },
            contentAlignment = Alignment.Center
        ) {
            AppleIcon(
                glyph = AppleGlyph.Check,
                tint = AppTheme.colors.accent,
                size = 16.dp
            )
        }
    }
}

@Composable
private fun EmptyLanguageState(
    query: String,
    onClear: () -> Unit
) {
    val shape = RoundedCornerShape(AppTheme.radius.card)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(AppTheme.colors.surface)
            .border(0.5.dp, AppTheme.colors.border, shape)
            .padding(AppTheme.layout.cardPadding * 1.4f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
    ) {
        AppleIcon(
            glyph = AppleGlyph.Search,
            tint = AppTheme.colors.textTertiary,
            size = 26.dp
        )
        AppText(
            text = "No match for \"$query\"",
            style = AppTheme.typography.h3,
            color = AppTheme.colors.textPrimary
        )
        AppText(
            text = "Try the English name of the language.",
            style = AppTheme.typography.bodySmall,
            color = AppTheme.colors.textSecondary
        )
        Spacer(modifier = Modifier.height(AppTheme.spacing.xs))
        AppButton(
            text = "Clear search",
            onClick = onClear,
            style = ai.emots.kishan_dynamic.ui.kit.AppButtonStyle.Secondary,
            fillMaxWidth = false
        )
    }
}
