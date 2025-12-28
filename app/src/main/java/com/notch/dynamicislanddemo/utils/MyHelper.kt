package com.notch.dynamicislanddemo.utils

import android.content.Context
import android.util.TypedValue

object MyHelper {
    fun convertDpToPixel(dp: Float, context: Context?): Float {
        return if (context != null) {
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                context.resources.displayMetrics
            )
        } else {
            dp
        }
    }
}
