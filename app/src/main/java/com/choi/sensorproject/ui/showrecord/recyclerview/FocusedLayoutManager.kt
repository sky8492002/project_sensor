package com.choi.sensorproject.ui.showrecord.recyclerview

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import kotlin.math.min

class FocusedLayoutManager(context: Context, private val snapHelper: SnapHelper, private val yOffset: Float): LinearLayoutManager(context) {

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(
            RecyclerView.LayoutParams.MATCH_PARENT,
            RecyclerView.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        super.onLayoutChildren(recycler, state)
        focusCenterView()
    }

    override fun scrollHorizontallyBy(
        dx: Int,
        recycler: RecyclerView.Recycler?,
        state: RecyclerView.State?
    ): Int {
        focusCenterView()
        return super.scrollHorizontallyBy(dx, recycler, state)
    }

    override fun onItemsAdded(recyclerView: RecyclerView, positionStart: Int, itemCount: Int) {
        super.onItemsAdded(recyclerView, positionStart, itemCount)
        focusCenterView()
    }

    override fun onItemsUpdated(recyclerView: RecyclerView, positionStart: Int, itemCount: Int) {
        super.onItemsUpdated(recyclerView, positionStart, itemCount)
        focusCenterView()
    }

    private fun focusCenterView(){
        val centerPos = getCenterPosition() ?: return
        // 최소 화면에 보이는 만큼은 위치 조정해야 함
        for(pos in centerPos-10..centerPos+10){
            val view = findViewByPosition(pos) ?: continue
            computeYCenterView(view)
        }
    }

    private fun computeYCenterView(view: View){
        val itemCenterX = view.x + view.width/2
        view.y = computeY(itemCenterX)
    }

    private fun getCenterPosition(): Int? {
        val centerView = snapHelper.findSnapView(this)
        return centerView?.let { getPosition(it) }
    }

    private fun computeY(viewCenterX: Float): Float {
        // 중앙을 기준으로 왼쪽/오른쪽에 있는 view의 위치가 조금씩 내려가게 설정
        return if(viewCenterX < width/2) {
            min(1f, viewCenterX / (width/2)) * yOffset - yOffset
        }else{
            min(1f, (width - viewCenterX) / (width/2)) * yOffset - yOffset
        }
    }

}