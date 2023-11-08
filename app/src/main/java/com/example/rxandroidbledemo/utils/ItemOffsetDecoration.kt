package com.example.rxandroidbledemo.utils

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

const val HORIZONTAL = 0
val VERTICAL = 1
val GRID = 2

class ItemOffsetDecoration(private val spacing: Int, private var displayMode: Int) :
    RecyclerView.ItemDecoration() {

    private var gridSize: Int = -1

    constructor(
        spacing: Int,
        displayMode: Int,
        gridSize: Int
    ) : this(spacing, displayMode) {
        this.gridSize = gridSize
    }


    private var shouldExcludeTop = false
    private var shouldExcludeLeft = false
    private var shouldExcludeRight = false
    private var shouldExcludeBottom = false

    private var allowTopMargin = true
    private var allowLeftMargin = true
    private var allowRightMargin = true
    private var allowBottomMargin = true
    fun excludeSides(
        top: Boolean = false,
        left: Boolean = false,
        right: Boolean = false,
        bottom: Boolean = false
    ) {
        shouldExcludeTop = top
        shouldExcludeLeft = left
        shouldExcludeRight = right
        shouldExcludeBottom = bottom
    }

    fun allowMargin(
        top: Boolean = true,
        left: Boolean = true,
        right: Boolean = true,
        bottom: Boolean = true,
    ) {
        allowTopMargin = top
        allowLeftMargin = left
        allowRightMargin = right
        allowBottomMargin = bottom
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildViewHolder(view).adapterPosition
        val itemCount = state.itemCount
        val layoutManager = parent.layoutManager
        setSpacingForDirection(outRect, layoutManager, position, itemCount)
    }

    private fun setSpacingForDirection(
        outRect: Rect,
        layoutManager: RecyclerView.LayoutManager?,
        position: Int,
        itemCount: Int
    ) {

        // Resolve display mode automatically
        if (displayMode == -1) {
            displayMode = resolveDisplayMode(layoutManager)
        }
        when (displayMode) {
            HORIZONTAL -> {
                if (!shouldExcludeLeft) {
                    outRect.left = spacing
                }
                if (!shouldExcludeTop) {
                    outRect.top = spacing
                }
                if (!shouldExcludeRight) {
                    outRect.right = if (position == itemCount - 1) spacing else 0
                }
                if (!shouldExcludeBottom) {
                    outRect.bottom = spacing
                }
            }

            VERTICAL -> {
                outRect.left = spacing
                outRect.right = spacing
                outRect.top = spacing
                outRect.bottom = if (position == itemCount - 1) spacing else 0
            }

            GRID -> if (layoutManager is GridLayoutManager) {
                val cols = if (gridSize == -1) {
                    layoutManager.spanCount
                } else {
                    gridSize
                }
                val rows = itemCount / cols
                outRect.left = spacing
                outRect.right = if (position % cols == cols - 1) spacing else 0
                outRect.top = spacing
                outRect.bottom = if (position / cols == rows - 1) spacing else 0
            }
        }
    }

    private fun resolveDisplayMode(layoutManager: RecyclerView.LayoutManager?): Int {
        if (layoutManager is GridLayoutManager) return GRID
        return if (layoutManager!!.canScrollHorizontally()) HORIZONTAL else VERTICAL
    }
}