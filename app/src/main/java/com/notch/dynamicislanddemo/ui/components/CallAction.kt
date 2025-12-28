package com.notch.dynamicislanddemo.ui.components

/**
 * Call actions
 */
sealed class CallAction {
    data object Accept : CallAction()
    data object Decline : CallAction()
}
