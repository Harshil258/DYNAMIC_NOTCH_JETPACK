package com.notch.dynamicislanddemo.ui.components

/**
 * Sealed class representing navigation destinations in the app
 */
sealed class NavigationDestination {
    object Home : NavigationDestination()
    object Preview : NavigationDestination()
    object Settings : NavigationDestination()
    object About : NavigationDestination()
    
    companion object {
        val values = listOf(Home, Preview, Settings, About)
        
        fun fromIndex(index: Int): NavigationDestination {
            return when (index) {
                0 -> Home
                1 -> Preview
                2 -> Settings
                3 -> About
                else -> Home
            }
        }
        
        fun toIndex(destination: NavigationDestination): Int {
            return when (destination) {
                Home -> 0
                Preview -> 1
                Settings -> 2
                About -> 3
            }
        }
    }
}
