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
        return Pager(config = PagingConfig(pageSize = 24, enablePlaceholders = false, initialLoadSize = 24 ), pagingSourceFactory = {
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