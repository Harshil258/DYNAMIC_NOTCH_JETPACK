package com.notch.dynamicislanddemo.ui.components

/**
 * Music player actions
 */
sealed class MusicAction {
    data object PlayPause : MusicAction()
    data object Previous : MusicAction()
    data object Next : MusicAction()
    data object AirPlay : MusicAction()
    data class Seek(val position: Float) : MusicAction()
}
