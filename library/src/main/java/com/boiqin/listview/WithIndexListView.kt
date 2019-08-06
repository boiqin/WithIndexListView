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
package com.boiqin.listview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ListView

class WithIndexListView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    ListView(context, attrs, defStyleAttr) {

    private var mBar: IndexBar? = null
    private var mAdapter: WithIndexListAdapter? = null
    private var mIsWithIndex: Boolean = false

    init {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        if (attrs == null) {
            return
        }
        val a = context.obtainStyledAttributes(attrs, R.styleable.WILV, 0, 0)
        try {
            val textSize =
                a.getDimension(R.styleable.WILV_indexBarFontSize, resources.getDimension(R.dimen.wilv__fontSize))
            val barPadding =
                a.getDimension(R.styleable.WILV_indexBarPadding, resources.getDimension(R.dimen.wilv__barPadding))
            val barMargin =
                a.getDimension(R.styleable.WILV_indexBarMargin, resources.getDimension(R.dimen.wilv__barMargin))
            val leading = a.getDimension(R.styleable.WILV_leading, resources.getDimension(R.dimen.wilv__textLeading))
            val isShowPreview = a.getBoolean(R.styleable.WILV_showPreview, true)
            val barAlpha = a.getFloat(R.styleable.WILV_indexBarAlpha, 0.5f)
            val barBackground = a.getColor(R.styleable.WILV_indexBarBackground, Color.BLACK)
            val textColor = a.getColor(R.styleable.WILV_indexBarTextColor, Color.WHITE)
            mBar = IndexBar(
                context,
                textSize,
                textColor,
                barPadding,
                barMargin,
                leading,
                barAlpha,
                barBackground,
                isShowPreview
            )
        } finally {
            a.recycle()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val x = event.x
                val y = event.y

                mBar?.apply {
                    if (isTouchInside(x, y)) {
                        mIsIndexing = true
                        mCurrentIndex = getIndexByPoint(y)
                        setSelection(mCurrentIndex)
                        mAdapter?.onIndexSelect(mCurrentIndex)
                        return true
                    }
                }
            }

            MotionEvent.ACTION_MOVE -> {
                mBar?.apply {
                    if (mIsIndexing) {
                        mCurrentIndex = getIndexByPoint(event.y)
                        setSelection(mCurrentIndex)
                        mAdapter?.onIndexSelect(mCurrentIndex)
                        return true
                    }
                }
            }

            MotionEvent.ACTION_UP -> {
                mBar?.apply {
                    if (this.mIsIndexing) {
                        this.mIsIndexing = false
                        this.mCurrentIndex = -1
                        invalidate()
                        return true
                    }
                }
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (mIsWithIndex) {
            mBar?.initSize(w.toFloat(), h.toFloat())
        }
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        mBar?.draw(canvas)
    }

    fun setWithIndexAdapter(adapter: WithIndexListAdapter) {
        mAdapter = adapter
        setAdapter(adapter)
        mBar?.mIndexList = adapter.indexList
        mIsWithIndex = true
    }
}