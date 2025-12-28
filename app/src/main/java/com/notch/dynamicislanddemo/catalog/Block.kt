package com.notch.dynamicislanddemo.catalog

import androidx.compose.runtime.Composable

@Composable
fun Block(content: @Composable () -> Unit) {
    content()
}
