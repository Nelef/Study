package com.inzisoft.ibks.view.overlayviews

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import com.inzisoft.mobile.view.MaskingCanvasView

class CustomMaskingCanvasView(context: Context, attrs: AttributeSet?, val usableMasking: Boolean) :
    MaskingCanvasView(context, attrs) {

    override fun onTouchEvent(motionEvent: MotionEvent?): Boolean {
        if(!usableMasking) {
            return true
        }
        return super.onTouchEvent(motionEvent)
    }
}