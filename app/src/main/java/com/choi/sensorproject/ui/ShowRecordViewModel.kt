package com.choi.sensorproject.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.choi.sensorproject.domain.paging.CustomPagingSource
import com.choi.sensorproject.domain.usecase.GetSensorRecordsUseCase
import com.choi.sensorproject.ui.model.RecordsForHourModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShowRecordViewModel  @Inject constructor(
    private val customPagingSource: CustomPagingSource
): ViewModel(){

    private val _uiState =
        MutableStateFlow<ShowRecordUIState>(ShowRecordUIState.Success(PagingData.empty()))
    val uiState: StateFlow<ShowRecordUIState> = _uiState

    fun getRecords() : Flow<PagingData<RecordsForHourModel>> {
        // pageSize와 initialLoadSize는 CustomPagingSource에서 사용되지 않음 (개수 기준이 아닌 날짜 기준으로 요청하기 때문)
        return Pager(config = PagingConfig(pageSize = 1, enablePlaceholders = false, initialLoadSize = 1 ), pagingSourceFactory = {
            customPagingSource
        }).flow
    }

    init{
        viewModelScope.launch(Dispatchers.IO) {
            try {
                getRecords().collectLatest {
                    _uiState.value = ShowRecordUIState.Success(it)
                }
            }
            catch (e: Exception){
                _uiState.value = ShowRecordUIState.Fail(e)
            }
        }
    }
}

sealed class ShowRecordUIState {
    data class Success(val records: PagingData<RecordsForHourModel>) : ShowRecordUIState()

    data class Fail(val error: Exception) : ShowRecordUIState()
}