package com.choi.sensorproject.ui.databinding

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.choi.sensorproject.ui.showrecord.ShowRecordUIState

@BindingAdapter("show_record_ui_state")
fun RecyclerView.setShowRecordUIState(uiState: ShowRecordUIState) {
    // 문제점: 디버깅 모드에서 실행하지 않으면 recyclerview 아이템이 표시되지 않음 (fragment에서 submitData하면 잘됨)
//    if (uiState is ShowRecordUIState.Success) {
//        findViewTreeLifecycleOwner()?.lifecycleScope?.launch {
//            (adapter as RecordsForHourAdapter).submitData(uiState.records)
//        }
//    }
}