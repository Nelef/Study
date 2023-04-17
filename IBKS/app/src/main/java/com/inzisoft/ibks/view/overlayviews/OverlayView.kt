package com.inzisoft.ibks.view.overlayviews

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import com.inzisoft.ibks.R
import com.inzisoft.mobile.recogdemolib.LibConstants
import com.inzisoft.mobile.view.overlay.CameraOverlayView

class OverlayView(context: Context, private val recogType: Int,
                  private val resultLayoutWidth: Int) : CameraOverlayView (context) {
    private var guideLT: Bitmap? = null
    private var guideRT: Bitmap? = null
    private var guideLB: Bitmap? = null
    private var guideRB: Bitmap? = null

    init {
        setStillGuide()
    }

    private val foundEdgeCallBackListener =
        FoundEdgeCallbackListener { isSuccess, points ->
            Log.e("SW_DEBUG", "isSuccess: $isSuccess")
            processFoundEdge(isSuccess, points)
            invalidate()
        }

    init {
        if(recogType == LibConstants.TYPE_IDCARD_OVERSEA || recogType == LibConstants.TYPE_IDCARD) {
            // 자동촬영 시, 촬영 대상의 외곽 꼭짓점을 반환하는 CallbacnkListener등록
            super.setFoundEdgeCallbackListener(foundEdgeCallBackListener)
        }
    }

    private val widthRatio = when(recogType) {
        LibConstants.TYPE_SEAL -> 3f / 9f
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

    override fun onDrawOverlayView(canvas: Canvas) {
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

        if(recogType == LibConstants.TYPE_IDCARD_OVERSEA || recogType == LibConstants.TYPE_IDCARD) {
            //Guide Bitmap
            guideLT?.let {
                canvas.drawBitmap(
                    it,
                    (mGuideRect.left - guideLT!!.width / 3).toFloat(),
                    (mGuideRect.top - guideLT!!.height / 3).toFloat(),
                    null
                )
            }

            guideRT?.let {
                canvas.drawBitmap(
                    it,
                    (mGuideRect.right - guideRT!!.width * 2 / 3).toFloat(),
                    (mGuideRect.top - guideRT!!.height / 3).toFloat(),
                    null
                )
            }

            guideLB?.let {
                canvas.drawBitmap(
                    it,
                    (mGuideRect.left - guideLT!!.width / 3).toFloat(),
                    (mGuideRect.bottom - guideRT!!.height * 2 / 3).toFloat(),
                    null
                )
            }

            guideRB?.let {
                canvas.drawBitmap(
                    it,
                    (mGuideRect.right - guideRT!!.width * 2 / 3).toFloat(),
                    (mGuideRect.bottom - guideRT!!.height * 2 / 3).toFloat(),
                    null
                )
            }
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

        val guideStartY: Int = if(recogType == LibConstants.TYPE_SEAL) {
            ((height - guideHeight) * 0.5f).toInt()
        } else {
            ((height - guideHeight) * 0.5f).toInt()
        }

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

    private fun setStillGuide() {
        guideLT = (resources.getDrawable(R.drawable.before_top_left) as BitmapDrawable).bitmap
        guideLB = (resources.getDrawable(R.drawable.before_bottom_left) as BitmapDrawable).bitmap
        guideRT = (resources.getDrawable(R.drawable.before_top_right) as BitmapDrawable).bitmap
        guideRB = (resources.getDrawable(R.drawable.before_bottom_right) as BitmapDrawable).bitmap
    }

    private fun setAlertGuide() {
        guideLT = (resources.getDrawable(R.drawable.fail_top_left) as BitmapDrawable).bitmap
        guideLB = (resources.getDrawable(R.drawable.fail_bottom_left) as BitmapDrawable).bitmap
        guideRT = (resources.getDrawable(R.drawable.fail_top_right) as BitmapDrawable).bitmap
        guideRB = (resources.getDrawable(R.drawable.fail_bottom_right) as BitmapDrawable).bitmap
    }

    private fun setOkGuide() {
        guideLT = (resources.getDrawable(R.drawable.success_top_left) as BitmapDrawable).bitmap
        guideLB = (resources.getDrawable(R.drawable.success_bottom_left) as BitmapDrawable).bitmap
        guideRT = (resources.getDrawable(R.drawable.success_top_right) as BitmapDrawable).bitmap
        guideRB = (resources.getDrawable(R.drawable.success_bottom_right) as BitmapDrawable).bitmap
    }

    private fun processFoundEdge(isFindingSuccess: Boolean, pointArray: Array<Point>?) {
        if (pointArray == null) {
            setAlertGuide()
        } else {
            if (isFindingSuccess) {  //인식가능상태
                setOkGuide()
            } else {
                setStillGuide()
            }
        }
    }
}