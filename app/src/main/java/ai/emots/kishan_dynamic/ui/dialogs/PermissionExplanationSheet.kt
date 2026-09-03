package ai.emots.kishan_dynamic.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ai.emots.kishan_dynamic.ui.components.AppleButton
import ai.emots.kishan_dynamic.ui.components.AppleButtonStyle
import ai.emots.kishan_dynamic.ui.components.AppleGlyph
import ai.emots.kishan_dynamic.ui.components.AppleIcon
import ai.emots.kishan_dynamic.ui.components.LuxuryCard
import ai.emots.kishan_dynamic.ui.screens.PermissionItemData
import ai.emots.kishan_dynamic.ui.theme.AuroraTokens

@Composable
fun PermissionExplanationSheet(
    permission: PermissionItemData,
    onDismiss: () -> Unit,
    onGrantClick: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(32.dp, RoundedCornerShape(28.dp), ambientColor = Color.Black, spotColor = AuroraTokens.Palette.primary.copy(alpha = 0.5f))
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF1E1930), Color(0xFF110E1C))
                        )
                    )
                    .border(1.dp, AuroraTokens.Border.hairlineSpecular, RoundedCornerShape(28.dp))
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    // Header Row: Icon + Title + Close
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(AuroraTokens.Palette.primary.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                AppleIcon(
                                    glyph = permission.glyph,
                                    tint = AuroraTokens.Palette.primaryLight,
                                    size = 22.dp
                                )
                            }

                            Column {
                                Text(
                                    text = permission.title,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = if (permission.isRequired) "Essential capability" else "Optional enhancement",
                                    fontSize = 12.sp,
                                    color = AuroraTokens.TextColor.secondary
                                )
                            }
                        }

                        // Close Disc Button
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF202228))
                                .border(1.dp, Color(0x20FFFFFF), CircleShape)
                                .clickable { onDismiss() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "✕",
                                fontSize = 13.sp,
                                color = AuroraTokens.TextColor.secondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Explanation Card
                    LuxuryCard(padding = 16.dp) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AppleIcon(glyph = AppleGlyph.Shield, tint = AuroraTokens.Palette.success, size = 16.dp)
                                Text(
                                    text = "Why this is required",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                            Text(
                                text = permission.privacyDetail,
                                fontSize = 12.5.sp,
                                lineHeight = 18.sp,
                                color = AuroraTokens.TextColor.secondary
                            )
                        }
                    }

                    // Security Guarantee
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF13201B))
                            .border(1.dp, Color(0x3310B981), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AppleIcon(glyph = AppleGlyph.Check, tint = AuroraTokens.Palette.success, size = 16.dp)
                        Text(
                            text = "Strictly zero tracking. Data never leaves your device.",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = AuroraTokens.Palette.success
                        )
                    }

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AppleButton(
                            text = "Not Now",
                            onClick = onDismiss,
                            style = AppleButtonStyle.SECONDARY,
                            modifier = Modifier.weight(1f),
                            paddingVertical = 12.dp
                        )

                        AppleButton(
                            text = if (permission.isGranted) "Granted ✓" else "Allow Access",
                            onClick = onGrantClick,
                            glyph = AppleGlyph.Sparkles,
                            style = AppleButtonStyle.PRIMARY,
                            modifier = Modifier.weight(1.3f),
                            paddingVertical = 12.dp
                        )
                    }
                }
            }
        }
    }
}
