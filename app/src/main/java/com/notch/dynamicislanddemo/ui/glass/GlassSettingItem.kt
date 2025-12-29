package com.notch.dynamicislanddemo.ui.glass

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.notch.dynamicislanddemo.catalog.components.LiquidToggle

/**
 * Glass-styled settings item with optional toggle, navigation arrow, or custom trailing content
 */
@Composable
fun GlassSettingItem(
    title: String,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    showNavigationArrow: Boolean = false,
    toggle: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {
    val isLightTheme = !isSystemInDarkTheme()
    val primaryTextColor = if (isLightTheme) Color.Black else Color.White
    val secondaryTextColor = if (isLightTheme) {
        Color.Black.copy(alpha = 0.6f)
    } else {
        Color.White.copy(alpha = 0.6f)
    }
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = primaryTextColor
                )
                Spacer(modifier = Modifier.width(16.dp))
            }
            
            Column {
                BasicText(
                    text = title,
                    style = TextStyle(
                        color = primaryTextColor,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Normal
                    )
                )
                
                if (subtitle != null) {
                    Spacer(modifier = Modifier.size(4.dp))
                    BasicText(
                        text = subtitle,
                        style = TextStyle(
                            color = secondaryTextColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal
                        )
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        when {
            toggle != null -> toggle()
            trailingContent != null -> trailingContent()
            showNavigationArrow -> {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Navigate",
                    modifier = Modifier.size(24.dp),
                    tint = secondaryTextColor
                )
            }
        }
    }
}

/**
 * Glass setting item with toggle switch
 */
@Composable
fun GlassSettingToggleItem(
    title: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    GlassSettingItem(
        title = title,
        backdrop = backdrop,
        modifier = modifier.alpha(if (enabled) 1f else 0.5f),
        subtitle = subtitle,
        icon = icon,
        onClick = if (enabled) { { onCheckedChange(!isChecked) } } else null,
        toggle = {
            LiquidToggle(
                selected = { isChecked },
                onSelect = if (enabled) onCheckedChange else { {} },
                backdrop = backdrop
            )
        }
    )
}

/**
 * Glass setting item with navigation
 */
@Composable
fun GlassSettingNavigationItem(
    title: String,
    onClick: () -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null
) {
    GlassSettingItem(
        title = title,
        backdrop = backdrop,
        modifier = modifier,
        subtitle = subtitle,
        icon = icon,
        showNavigationArrow = true,
        onClick = onClick
    )
}

/**
 * Glass setting item with slider
 */
@Composable
fun GlassSettingSliderItem(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    valueDisplay: String = value.toInt().toString()
) {
    val isLightTheme = !isSystemInDarkTheme()
    val primaryTextColor = if (isLightTheme) Color.Black else Color.White
    
    Column(modifier = modifier.fillMaxWidth()) {
        GlassSettingItem(
            title = title,
            backdrop = backdrop,
            subtitle = subtitle,
            icon = icon,
            trailingContent = {
                BasicText(
                    text = valueDisplay,
                    style = TextStyle(
                        color = primaryTextColor,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        )
        
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 12.dp),
            colors = SliderDefaults.colors(
                thumbColor = primaryTextColor,
                activeTrackColor = primaryTextColor,
                inactiveTrackColor = primaryTextColor.copy(alpha = 0.2f)
            )
        )
    }
}
