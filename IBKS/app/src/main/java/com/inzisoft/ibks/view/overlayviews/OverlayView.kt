package com.inzisoft.ibks.view.overlayviews

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import com.inzisoft.mobile.recogdemolib.LibConstants
import com.inzisoft.mobile.view.overlay.CameraOverlayView

class OverlayView(context: Context, private val recogType: Int,
                  private val resultLayoutWidth: Int) : CameraOverlayView (context) {
    private val widthRatio = when(recogType) {
        LibConstants.TYPE_SEAL -> 3f / 8f
        LibConstants.TYPE_OTHERS,
        LibConstants.TYPE_PAPER -> 9f / 10f
        else -> 3f / 5f
    }

    private val heightRatio = when(recogType) {
        LibConstants.TYPE_SEAL -> 0f
        LibConstants.TYPE_OTHERS,
        LibConstants.TYPE_PAPER -> 1.41f
        else -> 0.63f
    }

    override fun onDrawOverlayView(canvas: Canvas?) {
        // Make guide rect instance.
        mGuideRect = createGuideRect()

        if(recogType != LibConstants.TYPE_PAPER) {
            // Draw guide lines.
            val paint = Paint()
            paint.color = 0xffffffff.toInt()
            drawGuideRect(canvas!!, paint, mGuideRect)

            // Draw outer bar of the guide area.
            paint.color = 0x55000000
            drawGuideOuter(canvas!!, paint, mGuideRect, width, height)
        }
    }

    private fun createGuideRect(): Rect {
        val guideWidth: Int
        val guideHeight: Int
        val guideLayoutWidth = width - resultLayoutWidth

        if(recogType == LibConstants.TYPE_SEAL) {
            guideWidth = ((guideLayoutWidth * widthRatio).toInt())
            guideHeight = guideWidth
        } else {
            guideWidth = (guideLayoutWidth * widthRatio).toInt()
            guideHeight = (guideWidth * heightRatio).toInt()
        }

        // Set start point of the guide area.
        val guideStartX: Int = (guideLayoutWidth - guideWidth) / 2
        val guideStartY: Int = ((height - guideHeight) * 0.7f).toInt()

        return Rect(guideStartX, guideStartY, guideStartX + guideWidth, guideStartY + guideHeight)
    }

    private fun drawGuideRect(canvas: Canvas, paint: Paint?, guideRect: Rect) {
        canvas.drawLine(
            guideRect.left.toFloat(),
            guideRect.top.toFloat(),
            guideRect.right.toFloat(),
            guideRect.top.toFloat(),
            paint!!
        )
        canvas.drawLine(
            guideRect.left.toFloat(),
            guideRect.top.toFloat(),
            guideRect.left.toFloat(),
            guideRect.bottom.toFloat(),
            paint
        )
        canvas.drawLine(
            guideRect.left.toFloat(),
            guideRect.bottom.toFloat(),
            guideRect.right.toFloat(),
            guideRect.bottom.toFloat(),
            paint
        )
        canvas.drawLine(
            guideRect.right.toFloat(),
            guideRect.top.toFloat(),
            guideRect.right.toFloat(),
            guideRect.bottom.toFloat(),
            paint
        )
    }

    private fun drawGuideOuter(canvas: Canvas, paint: Paint?, guideRect: Rect, width: Int, height: Int) {
        val leftOverlayBar = Rect(
            0, 0, guideRect.left,
            height
        )
        val rightOverlayBar = Rect(
            guideRect.right, 0,
            width, height
        )
        val topOverlayBar = Rect(guideRect.left, 0, guideRect.right, guideRect.top)
        val bottomOverlayBar = Rect(
            guideRect.left, guideRect.bottom, guideRect.right,
            height
        )
        canvas.drawRect(leftOverlayBar, paint!!)
        canvas.drawRect(rightOverlayBar, paint)
        canvas.drawRect(topOverlayBar, paint)
        canvas.drawRect(bottomOverlayBar, paint)
    }
}