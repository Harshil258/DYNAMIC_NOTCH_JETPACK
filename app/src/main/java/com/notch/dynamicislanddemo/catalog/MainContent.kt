package com.notch.dynamicislanddemo.catalog

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.notch.dynamicislanddemo.catalog.destinations.AdaptiveLuminanceGlassContent
import com.notch.dynamicislanddemo.catalog.destinations.BottomTabsContent
import com.notch.dynamicislanddemo.catalog.destinations.ButtonsContent
import com.notch.dynamicislanddemo.catalog.destinations.ControlCenterContent
import com.notch.dynamicislanddemo.catalog.destinations.DialogContent
import com.notch.dynamicislanddemo.catalog.destinations.GlassPlaygroundContent
import com.notch.dynamicislanddemo.catalog.destinations.HomeContent
import com.notch.dynamicislanddemo.catalog.destinations.LazyScrollContainerContent
import com.notch.dynamicislanddemo.catalog.destinations.MagnifierContent
import com.notch.dynamicislanddemo.catalog.destinations.ProgressiveBlurContent
import com.notch.dynamicislanddemo.catalog.destinations.ScrollContainerContent
import com.notch.dynamicislanddemo.catalog.destinations.SliderContent
import com.notch.dynamicislanddemo.catalog.destinations.ToggleContent

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainContent() {
    var destination by rememberSaveable { mutableStateOf(CatalogDestination.Home) }

    BackHandler(destination != CatalogDestination.Home) {
        destination = CatalogDestination.Home
    }

    when (destination) {
        CatalogDestination.Home -> HomeContent(onNavigate = { destination = it })

        CatalogDestination.Buttons -> ButtonsContent()
        CatalogDestination.Toggle -> ToggleContent()
        CatalogDestination.Slider -> SliderContent()
        CatalogDestination.BottomTabs -> BottomTabsContent()
        CatalogDestination.Dialog -> DialogContent()

        CatalogDestination.ControlCenter -> ControlCenterContent()
        CatalogDestination.Magnifier -> MagnifierContent()

        CatalogDestination.GlassPlayground -> GlassPlaygroundContent()
        CatalogDestination.AdaptiveLuminanceGlass -> AdaptiveLuminanceGlassContent()
        CatalogDestination.ProgressiveBlur -> ProgressiveBlurContent()
        CatalogDestination.ScrollContainer -> ScrollContainerContent()
        CatalogDestination.LazyScrollContainer -> LazyScrollContainerContent()
    }
}
