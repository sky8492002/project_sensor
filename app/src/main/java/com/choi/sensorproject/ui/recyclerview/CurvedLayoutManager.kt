package com.choi.sensorproject.ui.recyclerview

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.sin

class CurvedLayoutManager(
    private val context: Context,
    private var horizontalOffset: Int = 0,
    private val itemWidth: Float = 0f,
    private val itemHeight: Float = 0f
): RecyclerView.LayoutManager() {
    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams =
        RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )

    // 가로 스크롤
    override fun canScrollHorizontally(): Boolean = true

    // 시작 지점 설정
    override fun scrollHorizontallyBy(
        dx: Int,
        recycler: RecyclerView.Recycler?,
        state: RecyclerView.State?
    ): Int {
        horizontalOffset += dx
        fill(recycler, state)
        return dx
    }

    //리사이클러뷰에 보여줄 아이템이 1개 이상인 경우 이를 그리도록 호출되는 함수
    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        super.onLayoutChildren(recycler, state)

        fill(recycler, state)
    }

    // recyclerview를 채우는 방법 설정
    private fun fill(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        detachAndScrapAttachedViews(recycler ?: return)

        for (itemIndex in 0 until itemCount) {
            val view = recycler.getViewForPosition(itemIndex)
            addView(view)

            val viewWidth = pxFromDp(context, itemWidth)
            val viewHeight = pxFromDp(context, itemHeight)


            measureChildWithMargins(view ?: return, viewWidth.toInt(), viewHeight.toInt())

            val left = (itemIndex * viewWidth) - horizontalOffset
            val right = left + viewWidth
            val yComponent = computeYComponent((left + right) / 2, viewHeight)
            val top = yComponent.first
            val bottom = top + viewHeight

            val alpha = yComponent.second
            view.rotation = (alpha * (180 / PI)).toFloat() - itemHeight

            // 리사이클러뷰 소속 view의 위치 조정
            layoutDecoratedWithMargins(
                view,
                left.toInt(),
                top,
                right.toInt(),
                bottom.toInt()
            )

        }

        recycler.scrapList.toList().forEach {
            recycler.recycleView(it.itemView)
        }
    }

    // dp -> px 변환
    private fun pxFromDp(context: Context, dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }

    // 리사이클러뷰 소속 view에 대해 y축 위치, 각도 설정 후 반환
    private fun computeYComponent(viewCenterX: Float,
                                  h: Float): Pair<Int, Double> {
        val screenWidth = context.resources.displayMetrics.widthPixels
        val s = screenWidth.toDouble() / 2
        val radius = (h * h + s * s) / (h * 2)

        val xScreenFraction = viewCenterX.toDouble() / screenWidth.toDouble()
        val beta = acos(s / radius)

        val alpha = beta + (xScreenFraction * (Math.PI - (2 * beta)))
        val yComponent = radius - (radius * sin(alpha))

        return Pair(yComponent.toInt(), alpha)
    }
}