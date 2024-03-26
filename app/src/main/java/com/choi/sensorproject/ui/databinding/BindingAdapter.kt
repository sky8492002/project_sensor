package com.choi.sensorproject.ui.databinding

import android.graphics.Bitmap
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
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

@BindingAdapter("inside_hour") // recyclerview item의 배경
fun ConstraintLayout.setInsideBackgroundByHour(hour: String?){
    if(hour != null){
        when(hour.toInt()){
            // 시간에 따라 다른 이미지 적용 (낮, 노을, 밤)
            in 0..4 ->{
                this.setBackgroundResource(R.drawable.background_night_green)
            }
            in 5..7 ->{
                this.setBackgroundResource(R.drawable.background_sunset_green)
            }
            in 8..16 ->{
                this.setBackgroundResource(R.drawable.background_light_sky_green)
            }
            in 17..19 ->{
                this.setBackgroundResource(R.drawable.background_sunset_green)
            }
            in 20..23 ->{
                this.setBackgroundResource(R.drawable.background_night_green)
            }
        }
    }
    else{
        this.setBackgroundResource(R.drawable.background_gray)
    }
}

@BindingAdapter("outside_hour") // 전체 화면의 배경
fun ConstraintLayout.setOutsideBackgroundByHour(hour: String?){
    if(hour != null){
        when(hour.toInt()){
            // 시간에 따라 다른 이미지 적용 (낮, 노을, 밤)
            in 0..4 ->{
                this.setBackgroundResource(R.drawable.background_night_gray)
            }
            in 5..7 ->{
                this.setBackgroundResource(R.drawable.background_sunset_gray)
            }
            in 8..16 ->{
                this.setBackgroundResource(R.drawable.background_sky_gray)
            }
            in 17..19 ->{
                this.setBackgroundResource(R.drawable.background_sunset_gray)
            }
            in 20..23 ->{
                this.setBackgroundResource(R.drawable.background_night_gray)
            }
        }
    }
    else{
        this.setBackgroundResource(R.drawable.background_gray)
    }
}