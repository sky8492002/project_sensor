package com.choi.sensorproject.ui.viewmodel

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.choi.sensorproject.domain.mapper.toUIModelsPagingData
import com.choi.sensorproject.domain.usecase.sensor.GetSensorRecordsUseCase
import com.choi.sensorproject.ui.model.RecordsForHourUIModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import javax.inject.Inject

@HiltViewModel
class ManageSensorRecordViewModel  @Inject constructor(
    private val getSensorRecordsUseCase: GetSensorRecordsUseCase
): ViewModel(){

    private val _uiState =
        MutableStateFlow<SensorRecordUIState>(SensorRecordUIState.Success(PagingData.empty()))
    val uiState: StateFlow<SensorRecordUIState> = _uiState

    @SuppressLint("SimpleDateFormat")
    val dayFormat = SimpleDateFormat("yyyy-MM-dd")
    private var initPageDate = dayFormat.format(System.currentTimeMillis())

    // 연결된 PagingAdapter가 refresh 함수 호출 시 PagingSourceFactory가 새로운 PagingSource를 만들어서 Flow에 적용 (PageFetcher의 refreshEvents에서 진행)
    // 매번 새로운 CustomPagingSource가 필요하기 때문에 Hilt를 사용한 의존성 주입은 적합하지 않음
    // pageSize와 initialLoadSize는 CustomPagingSource에서 사용되지 않음 (개수 기준이 아닌 날짜 기준으로 요청하기 때문)
    private val sensorRecordPager = Pager(config = PagingConfig(pageSize = 1, enablePlaceholders = false, initialLoadSize = 1 ), pagingSourceFactory = {
        getSensorRecordsUseCase.getSensorRecordPagingSource(initPageDate)
    })

    init{
        viewModelScope.launch(Dispatchers.IO) {
            try {
                sensorRecordPager.flow.collect {
                    _uiState.value = SensorRecordUIState.Success(it.toUIModelsPagingData())
                }
            }
            catch (e: Exception){
                _uiState.value = SensorRecordUIState.Fail(e)
            }
        }
    }

    fun changeInitPageDate(initPageDate: String){
        this.initPageDate = initPageDate
    }

    fun getInitPageDate(): String{
        return initPageDate
    }

}

sealed class SensorRecordUIState {
    data class Success(val records: PagingData<RecordsForHourUIModel>) : SensorRecordUIState()

    data class Fail(val error: Exception) : SensorRecordUIState()
}