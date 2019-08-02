package com.boiqin.listview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import kotlin.math.max
import kotlin.math.min


internal data class IndexBar(
    val context: Context,
    var textSize: Float,
    var textColor: Int,
    var barPadding: Float,
    var barMargin: Float,
    var leading: Float,
    var barAlpha: Float,
    var barBackground: Int,
    var isShowPreview: Boolean
) {


    var mCurrentIndex = -1
    var mIsIndexing: Boolean = false

    var mIndexList: List<String>? = null
    /**
     * 索引栏和预览的透明度,当透明度为0时,预览将自动隐藏
     */
    private var mWidth: Float = 0f
    private var mHeight: Float = 0f
    private var mRect: RectF? = null
    private var mPreviewRect: RectF? = null

    private var mBarTextPaint: Paint? = null //用于绘制索引栏内的文字
    private var mPreviewTextPaint: Paint? = null //绘制预览框的文字
    private var mBarBgPaint: Paint? = null   //绘制索引栏的背景
    private var mPreviewBgPaint: Paint? = null  //绘制预览框的背景


    private fun initPaint() {
        /* 预览框背景 */
        val defaultSize = context.resources.getDimension(R.dimen.wilv__previewTextSizeDefault)

        mPreviewBgPaint = mPreviewBgPaint ?: Paint().apply {
            isAntiAlias = true
            color = this@IndexBar.barBackground
            textSize = defaultSize
            alpha = (this@IndexBar.barAlpha * 255).toInt()
        }

        /* 预览框内字体 */
        mPreviewTextPaint = mPreviewTextPaint ?: Paint().apply {
            color = this@IndexBar.textColor
            textSize = defaultSize
            alpha = (this@IndexBar.barAlpha * 255).toInt()
        }

        /* 索引栏内文字画笔 */
        mBarTextPaint = mBarTextPaint ?: Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = this@IndexBar.textColor
            textSize = this@IndexBar.textSize
            alpha = (this@IndexBar.barAlpha * 255).toInt()
        }

        /* 索引栏背景画笔 */
        mBarBgPaint = mBarBgPaint ?: Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = this@IndexBar.barBackground
            alpha = (this@IndexBar.barAlpha * 255).toInt()
        }
    }

    fun initSize(width: Float, height: Float) {
        initPaint()

        mWidth = calculateIndexBarWidth()
        mHeight = calculateIndexBarHeight(height)
        initRect(width, height)
        initPreviewRect(width, height)
    }


    private fun initRect(listViewWidth: Float, listViewHeight: Float) {
        val left = listViewWidth - mWidth - barMargin
        val top = (listViewHeight - mHeight) / 2
        mRect = RectF(left, top, left + mWidth, top + mHeight + barPadding)
    }

    private fun initPreviewRect(listViewWidth: Float, listViewHeight: Float) {
        val size = mPreviewBgPaint?.descent()?.minus(mPreviewBgPaint?.ascent() ?: 0f) ?: 0f
        val left = listViewWidth / 2 - size
        val right = listViewWidth / 2 + size * 2
        val top = listViewHeight / 2 - size
        val bottom = listViewHeight / 2 + size * 2
        mPreviewRect = RectF(left, top, right, bottom)
    }

    private fun calculateIndexBarWidth(): Float {
        if (mIndexList == null || mIndexList?.size ?: 0 <= 0) {
            return 0f
        }

        var maxWidth = 0f
        mIndexList?.forEach { indexStr ->
            mBarTextPaint?.measureText(indexStr)?.let {
                if (it > maxWidth) {
                    maxWidth = it
                }
            }
        }
        return maxWidth + barPadding
    }

    private fun calculateIndexBarHeight(listViewHeight: Float): Float {
        mIndexList?.run {
            if (isNotEmpty()) {
                //屏幕可以为每个item分配的最大高度
                val maxHeight = (listViewHeight - barMargin * 2 - barPadding * 2) / size
                val textHeight = mBarTextPaint?.let { paint ->
                    //在用户设置的字体下,每个item需要占用的高度
                    var textHeight = paint.descent() - paint.ascent() + leading

                    if (maxHeight < textHeight) {
                        val fontSize = getTextSize(maxHeight, textHeight)
                        paint.textSize = fontSize
                        //新字体大小的文字高度
                        textHeight = paint.descent() - paint.ascent() + leading
                    }
                    textHeight
                } ?: 0f
                return textHeight * size + barPadding
            }
        }
        return 0f
    }

    private fun getTextSize(maxHeight: Float, textHeight: Float): Float {
        val sp = context.resources.displayMetrics.scaledDensity
        mBarTextPaint?.let { paint ->
            val currentSize = (paint.textSize / sp).toInt()
            val tempPaint = Paint()
            for (i in currentSize downTo 1) {
                tempPaint.textSize = i * sp
                val measureHeight = tempPaint.descent() - tempPaint.ascent() + leading

                if (measureHeight <= maxHeight) {
                    return i * sp
                }
            }
        }


        return 10f // 默认字体大小
    }

    fun isTouchInside(x: Float, y: Float): Boolean {
        mRect?.run {
            val isInHorizontal = x in left..right
            val isInVertical = y in top..bottom
            return isInHorizontal && isInVertical
        }
        return false
    }


    fun draw(canvas: Canvas) {
        drawIndexBar(canvas)
        if (isShowPreview && mIsIndexing) {
            drawPreview(canvas)
        }
    }

    fun drawIndexBar(canvas: Canvas) {
        mBarBgPaint?.let { paint ->
            if (paint.alpha >= 0) {
                mRect?.run {
                    canvas.drawRoundRect(this, textSize / 5, textSize / 5, paint)
                }
            }
        }
        drawText(canvas)
    }

    /**
     * draw text on index bar
     * @param canvas 画布
     */
    fun drawText(canvas: Canvas) {
        mBarTextPaint?.let { paint ->
            val textHeight = paint.descent() - paint.ascent() + leading
            mIndexList?.forEachIndexed { index, text ->
                //算出可用的范围
                mRect?.run {
                    val left = this.left + (mWidth - paint.measureText(text)) / 2
                    val top = textHeight * index + this.top - paint.ascent() + barPadding
                    canvas.drawText(text, left, top, paint)
                }
            }
        }
    }

    /**
     * draw preview
     * @param canvas ExpandableListview's canvas
     */
    fun drawPreview(canvas: Canvas) {
        mPreviewBgPaint?.let {
            if (mCurrentIndex < 0) {
                return
            }
            mPreviewRect?.let { rect ->
                canvas.drawRoundRect(rect, 10f, 10f, it)

                mPreviewTextPaint?.let { paint ->
                    val text = mIndexList?.get(mCurrentIndex) ?: ""
                    val width = rect.right - rect.left
                    val textHeight = paint.descent() - paint.ascent()
                    val previewHeight = rect.bottom - rect.top
                    val x = rect.left + (width - paint.measureText(text)) / 2
                    val y = rect.top + (previewHeight - textHeight) / 2 - paint.ascent()
                    canvas.drawText(text, x, y, paint)
                }

            }
        }
    }

    /**
     * 根据当前y坐标转换为position
     * @param y 当前y坐标
     * @return 返回当前坐标对应的position
     */
    fun getIndexByPoint(y: Float): Int {
        mRect?.let { rect ->
            return when {
                y <= rect.top - barPadding -> 0
                y >= rect.bottom - barPadding -> {
                    val index = mIndexList?.let {
                        it.size - 1
                    }
                    index ?: 0
                }
                else -> {
                    val index = mBarTextPaint?.let { paint ->
                        val textHeight = paint.descent() - paint.ascent() + leading
                        val top = rect.top - barPadding - paint.ascent() //rect 顶部坐标
                        val section = ((y - top) / textHeight).toInt()
                        //防止极端情况出现数组越界
                        min(max(0, section), mIndexList?.size?.minus(1) ?: 0)
                    }
                    index ?: 0
                }
            }
        }
        // 一般走不到这
        return -1
    }
}