package com.studentprofile.app.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.studentprofile.app.R

class CircularProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var progress = 0f
    private var maxProgress = 100f
    private var progressColor = Color.BLUE
    private var trackColor = Color.LTGRAY
    private var strokeWidth = 15f
    private var showPercentage = true
    private var centerText: String? = null
    private var centerTextSize = 36f
    private var centerTextColor = Color.BLACK

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }

    private val rectF = RectF()

    init {
        val typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CircularProgressView, defStyleAttr, 0
        )
        try {
            progress = typedArray.getFloat(R.styleable.CircularProgressView_cpv_progress, 0f)
            maxProgress = typedArray.getFloat(R.styleable.CircularProgressView_cpv_maxProgress, 100f)
            progressColor = typedArray.getColor(R.styleable.CircularProgressView_cpv_progressColor, Color.BLUE)
            trackColor = typedArray.getColor(R.styleable.CircularProgressView_cpv_trackColor, Color.LTGRAY)
            strokeWidth = typedArray.getDimension(R.styleable.CircularProgressView_cpv_strokeWidth, 15f)
            showPercentage = typedArray.getBoolean(R.styleable.CircularProgressView_cpv_showPercentage, true)
            centerText = typedArray.getString(R.styleable.CircularProgressView_cpv_centerText)
            centerTextSize = typedArray.getDimension(R.styleable.CircularProgressView_cpv_centerTextSize, 36f)
            centerTextColor = typedArray.getColor(R.styleable.CircularProgressView_cpv_centerTextColor, Color.BLACK)
        } finally {
            typedArray.recycle()
        }
        updatePaints()
    }

    private fun updatePaints() {
        trackPaint.color = trackColor
        trackPaint.strokeWidth = strokeWidth
        progressPaint.color = progressColor
        progressPaint.strokeWidth = strokeWidth
        textPaint.color = centerTextColor
        textPaint.textSize = centerTextSize
        textPaint.isFakeBoldText = true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val padding = strokeWidth / 2
        rectF.set(padding, padding, w - padding, h - padding)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw background track (full circle)
        canvas.drawArc(rectF, 0f, 360f, false, trackPaint)

        // Draw progress arc (starting from top: -90 degrees)
        val sweepAngle = (progress / maxProgress) * 360f
        canvas.drawArc(rectF, -90f, sweepAngle, false, progressPaint)

        // Draw center text
        val displayText = centerText ?: if (showPercentage) {
            String.format("%.1f%%", progress)
        } else {
            ""
        }

        if (displayText.isNotEmpty()) {
            val textY = rectF.centerY() - (textPaint.descent() + textPaint.ascent()) / 2
            canvas.drawText(displayText, rectF.centerX(), textY, textPaint)
        }
    }

    fun setProgress(value: Float) {
        progress = value.coerceIn(0f, maxProgress)
        invalidate()
    }

    fun getProgress(): Float = progress

    fun setProgressColor(color: Int) {
        progressColor = color
        progressPaint.color = color
        invalidate()
    }

    fun setTrackColor(color: Int) {
        trackColor = color
        trackPaint.color = color
        invalidate()
    }

    fun setCenterText(text: String?) {
        centerText = text
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredSize = 200
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val width = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> minOf(desiredSize, widthSize)
            else -> desiredSize
        }

        val height = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> minOf(desiredSize, heightSize)
            else -> desiredSize
        }

        val size = minOf(width, height)
        setMeasuredDimension(size, size)
    }
}
