package com.choi.sensorproject.ui.databinding

import android.graphics.Bitmap
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.choi.sensorproject.ui.recyclerview.AppInfoAdapter
import com.choi.sensorproject.ui.viewmodel.AppInfoUIState
import com.choi.sensorproject.ui.viewmodel.SensorRecordUIState
import com.example.sensorproject.R

//@BindingAdapter("show_record_ui_state")
//fun RecyclerView.putSensorRecords(uiState: SensorRecordUIState) {
//    //문제점: 디버깅 모드에서 실행하지 않으면 recyclerview 아이템이 표시되지 않음 (fragment에서 submitData하면 잘됨)
//    if (uiState is SensorRecordUIState.Success) {
//        findViewTreeLifecycleOwner()?.lifecycleScope?.launch {
//            (adapter as RecordsForHourAdapter).submitData(uiState.records)
//        }
//    }
//}

//@BindingAdapter("app_info_ui_state")
//fun RecyclerView.putAppInfos(uiState: AppInfoUIState){
//    if(uiState is AppInfoUIState.Success){
//        (this.adapter as AppInfoAdapter).submitList(uiState.appInfos)
//    }
//}
@BindingAdapter("bitmap_image")
fun ImageView.setImage(bitmap: Bitmap?){
    if(bitmap != null){
        this.setImageBitmap(bitmap)
    }
    else{
        this.setImageResource(R.drawable.phone)
    }
}