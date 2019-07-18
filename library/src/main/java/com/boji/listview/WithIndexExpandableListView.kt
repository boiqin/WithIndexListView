/*******************************************************************************
 * Copyright 2019 boji
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 *******************************************************************************/
/**
 * Created by boji on 2019-07-17.
 * ExpandableListView+快速索引功能
 */
package com.boji.listview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ExpandableListView
import kotlin.math.max
import kotlin.math.min

class WithIndexExpandableListView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    ExpandableListView(context, attrs, defStyleAttr) {

    private var mCurrentIndex = -1
    private var mIsIndexing: Boolean = false

    /**
     * 是否显示中间的预览
     */
    private var mIsShowPreview = true
    /**
     * 索引栏和预览的背景颜色
     */
    private var mBarBackground = Color.BLACK
    private var mTextColor = Color.WHITE
    /**
     * 索引栏和预览的透明度,当透明度为0时,预览将自动隐藏
     */
    private var mBarAlpha = 0.5f
    private var mBarPadding = 0f
    private var mBarMargin = 0f
    private var mTextSize = 0f
    private var mLeading = 0f

    private var mIndexList: List<String>? = null
    private var mBar: IndexBar? = null

    private var mBarTextPaint: Paint? = null //用于绘制索引栏内的文字
    private var mPreviewTextPaint: Paint? = null //绘制预览框的文字
    private var mBarBgPaint: Paint? = null   //绘制索引栏的背景
    private var mPreviewBgPaint: Paint? = null  //绘制预览框的背景

    private var mIsWithIndex: Boolean = false

    init {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        val res = resources
        mTextSize = res.getDimension(R.dimen.wilv__fontSize)
        mBarPadding = res.getDimension(R.dimen.wilv__barPadding)
        mBarMargin = res.getDimension(R.dimen.wilv__barMargin)
        mLeading = res.getDimension(R.dimen.wilv__textLeading)

        if (attrs == null) {
            return
        }
        val a = context.obtainStyledAttributes(attrs, R.styleable.WILV, 0, 0)
        try {
            mTextSize = a.getDimension(R.styleable.WILV_indexBarFontSize, mTextSize)
            mBarPadding = a.getDimension(R.styleable.WILV_indexBarPadding, mBarPadding)
            mBarMargin = a.getDimension(R.styleable.WILV_indexBarMargin, mBarMargin)
            mIsShowPreview = a.getBoolean(R.styleable.WILV_showPreview, true)
            mBarAlpha = a.getFloat(R.styleable.WILV_indexBarAlpha, 0.5f)
            mBarBackground = a.getColor(R.styleable.WILV_indexBarBackground, Color.BLACK)
            mTextColor = a.getColor(R.styleable.WILV_indexBarTextColor, Color.WHITE)
        } finally {
            a.recycle()
        }
    }

    private fun initPaint() {
        /* 预览框背景 */
        val defaultSize = resources.getDimension(R.dimen.wilv__previewTextSizeDefault)

        mPreviewBgPaint = mPreviewBgPaint ?: Paint().apply {
            isAntiAlias = true
            color = mBarBackground
            textSize = defaultSize
            alpha = (mBarAlpha * 255).toInt()
        }

        /* 预览框内字体 */
        mPreviewTextPaint = mPreviewTextPaint ?: Paint().apply {
            color = mTextColor
            textSize = defaultSize
            alpha = (mBarAlpha * 255).toInt()
        }

        /* 索引栏内文字画笔 */
        mBarTextPaint = mBarTextPaint ?: Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = mTextColor
            textSize = mTextSize
            alpha = (mBarAlpha * 255).toInt()
        }

        /* 索引栏背景画笔 */
        mBarBgPaint = mBarBgPaint ?: Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = mBarBackground
            alpha = (mBarAlpha * 255).toInt()
        }
    }

    private fun initIndexBar(listViewWidth: Float, listViewHeight: Float) {
        mBar = IndexBar(listViewWidth, listViewHeight)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val x = event.x
                val y = event.y

                mBar?.apply {
                    if (isTouchInside(x, y)) {
                        mIsIndexing = true
                        mCurrentIndex = getIndexByPoint(y)
                        setSelectedGroup(mCurrentIndex)
                        return true
                    }
                }
            }

            MotionEvent.ACTION_MOVE -> {
                mBar?.apply {
                    if (mIsIndexing) {
                        mCurrentIndex = getIndexByPoint(event.y)
                        setSelectedGroup(mCurrentIndex)
                        return true
                    }
                }
            }

            MotionEvent.ACTION_UP -> {
                if (mIsIndexing) {
                    mIsIndexing = false
                    mCurrentIndex = -1
                    invalidate()
                    return true
                }
            }
        }

        return super.onTouchEvent(event)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (mBar == null) {
            if (mIsWithIndex) {
                initIndexBar(w.toFloat(), h.toFloat())
            }
        } else {
            // 可能转屏
            mBar = null // 创建新的bar
            initPaint()
            initIndexBar(w.toFloat(), h.toFloat())
        }
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        mBar?.draw(canvas)
    }

    fun setWithIndexAdapter(adapter: WithIndexExpandableListAdapter) {
        setAdapter(adapter)
        initPaint()
        mIndexList = adapter.indexList
        mIsWithIndex = true
    }

    private inner class IndexBar(listViewWidth: Float, listViewHeight: Float) {
        private val mWidth: Float
        private val mHeight: Float
        private var mRect: RectF? = null
        private var mPreviewRect: RectF? = null

        init {
            mWidth = calculateIndexBarWidth()
            mHeight = calculateIndexBarHeight(listViewHeight)
            initRect(listViewWidth, listViewHeight)
            initPreviewRect(listViewWidth, listViewHeight)
        }

        private fun initRect(listViewWidth: Float, listViewHeight: Float) {
            val left = listViewWidth - mWidth - mBarMargin
            val top = (listViewHeight - mHeight) / 2
            mRect = RectF(left, top, left + mWidth, top + mHeight + mBarPadding)
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
            return maxWidth + mBarPadding
        }

        private fun calculateIndexBarHeight(listViewHeight: Float): Float {
            mIndexList?.run {
                if (isNotEmpty()) {
                    //屏幕可以为每个item分配的最大高度
                    val maxHeight = (listViewHeight - mBarMargin * 2 - mBarPadding * 2) / size
                    val textHeight = mBarTextPaint?.let { paint ->
                        //在用户设置的字体下,每个item需要占用的高度
                        var textHeight = paint.descent() - paint.ascent() + mLeading

                        if (maxHeight < textHeight) {
                            val fontSize = getTextSize(maxHeight, textHeight)
                            paint.textSize = fontSize
                            //新字体大小的文字高度
                            textHeight = paint.descent() - paint.ascent() + mLeading
                        }
                        textHeight
                    } ?: 0f
                    return textHeight * size + mBarPadding
                }
            }
            return 0f
        }

        private fun getTextSize(maxHeight: Float, textHeight: Float): Float {
            val sp = resources.displayMetrics.scaledDensity
            mBarTextPaint?.let { paint ->
                val currentSize = (paint.textSize / sp).toInt()

                var measureHeight = textHeight
                val tempPaint = Paint()

                for (i in currentSize downTo 1) {
                    tempPaint.textSize = i * sp
                    measureHeight = tempPaint.descent() - tempPaint.ascent() + mLeading

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
            if (mIsShowPreview && mIsIndexing) {
                drawPreview(canvas)
            }
        }

        fun drawIndexBar(canvas: Canvas) {
            mBarBgPaint?.let { paint ->
                if (paint.alpha >= 0) {
                    mRect?.run {
                        canvas.drawRoundRect(this, mTextSize / 5, mTextSize / 5, paint)
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
                val textHeight = paint.descent() - paint.ascent() + mLeading
                mIndexList?.forEachIndexed { index, text ->
                    //算出可用的范围
                    mRect?.run {
                        val left = this.left + (mWidth - paint.measureText(text)) / 2
                        val top = textHeight * index + this.top - paint.ascent() + mBarPadding
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
                if (mCurrentIndex < 0 && alpha <= 0) {
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
                    y <= rect.top - mBarPadding -> 0
                    y >= rect.bottom - mBarPadding -> {
                        val index = mIndexList?.let {
                            it.size - 1
                        }
                        index ?: 0
                    }
                    else -> {
                        val index = mBarTextPaint?.let { paint ->
                            val textHeight = paint.descent() - paint.ascent() + mLeading
                            val top = rect.top - mBarPadding - paint.ascent() //rect 顶部坐标
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


    fun isShowPreview(): Boolean {
        return mIsShowPreview
    }

    fun setSowPreview(isShowPreview: Boolean) {
        mIsShowPreview = isShowPreview
    }

    fun getBarTextPaint(): Paint? {
        return mBarTextPaint
    }

    fun getBarBgPaint(): Paint? {
        return mBarBgPaint
    }

    fun getPreviewTextPaint(): Paint? {
        return mPreviewTextPaint
    }

    fun getPreviewBgPaint(): Paint? {
        return mPreviewBgPaint
    }

}