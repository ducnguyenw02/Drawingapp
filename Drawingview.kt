package com.example.imageeditorapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class DrawingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val squarePaint = Paint().apply {
        color = Color.YELLOW // Mặc định màu vàng
        style = Paint.Style.STROKE
        strokeWidth = 10f
    }

    private var squareRect: RectF? = null
    private var isDrawingEnabled = false

    fun setSquareColor(color: Int) {
        squarePaint.color = color
        invalidate() // Vẽ lại view để cập nhật màu
    }

    fun enableDrawing(enable: Boolean) {
        isDrawingEnabled = enable
        if (isDrawingEnabled) {
            // Đặt vị trí hình vuông mặc định khi bắt đầu vẽ
            val centerX = width / 2f
            val centerY = height / 2f
            val size = 200f // Kích thước hình vuông
            squareRect = RectF(
                centerX - size / 2,
                centerY - size / 2,
                centerX + size / 2,
                centerY + size / 2
            )
        } else {
            squareRect = null // Ẩn hình vuông khi không vẽ
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (isDrawingEnabled && squareRect != null) {
            canvas.drawRect(squareRect!!, squarePaint)
        }
    }
}
