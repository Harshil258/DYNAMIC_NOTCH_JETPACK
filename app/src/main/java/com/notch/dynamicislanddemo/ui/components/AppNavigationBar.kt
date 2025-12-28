package com.notch.dynamicislanddemo.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.notch.dynamicislanddemo.catalog.components.LiquidBottomTab
import com.notch.dynamicislanddemo.catalog.components.LiquidBottomTabs

/**
 * Bottom navigation bar using LiquidBottomTabs from catalog
 * Following the EXACT pattern from BottomTabsContent reference
 */
@Composable
fun AppNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier
) {
    val isLightTheme = !isSystemInDarkTheme()
    val contentColor = if (isLightTheme) Color.Black else Color.White
    val selectedColor = if (isLightTheme) Color(0xFF007AFF) else Color(0xFF0A84FF)
    
    // Tab configuration
    val tabEmojis = listOf("🏠", "👁️", "⚙️", "ℹ️")
    val tabLabels = listOf("Home", "Preview", "Settings", "About")
    
    // CRITICAL: Use LOCAL state management exactly like BottomTabsContent
    var localSelectedTab by rememberSaveable { mutableIntStateOf(selectedTab) }
    
    // Sync external state changes to local state
    LaunchedEffect(selectedTab) {
        if (localSelectedTab != selectedTab) {
            localSelectedTab = selectedTab
        }
    }
    
    // Notify parent when local state changes
    LaunchedEffect(localSelectedTab) {
        onTabSelected(localSelectedTab)
    }
    
    LiquidBottomTabs(
        selectedTabIndex = { localSelectedTab },  // Pass local state as lambda
        onTabSelected = { localSelectedTab = it }, // Update local state in callback
        backdrop = backdrop,
        tabsCount = 4,
        modifier = modifier
            .fillMaxWidth()
            .systemBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // CRITICAL: Use exact pattern from BottomTabsContent
        repeat(4) { index ->
            LiquidBottomTab(onClick = { localSelectedTab = index }) {  // Direct state update
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    BasicText(
                        text = tabEmojis[index],
                        style = TextStyle(
                            fontSize = 24.sp
                        )
                    )
                    BasicText(
                        text = tabLabels[index],
                        style = TextStyle(
                            color = if (localSelectedTab == index) selectedColor else contentColor.copy(alpha = 0.6f),
                            fontSize = 10.sp,
                            fontWeight = if (localSelectedTab == index) FontWeight.SemiBold else FontWeight.Normal
                        )
                    )
                }
            }
        }
    }
}
