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
        MutableStateFlow<UIState>(UIState.Success(PagingData.empty()))
    val uiState: StateFlow<UIState> = _uiState

    fun getRecords() : Flow<PagingData<RecordsForHourModel>> {
        return Pager(config = PagingConfig(pageSize = 20, enablePlaceholders = false, initialLoadSize = 20, ), pagingSourceFactory = {
            customPagingSource
        }).flow
    }

    init{
        viewModelScope.launch() {
            try {
                getRecords().collect {
                    _uiState.value = UIState.Success(it)
                }
            }
            catch (e: Exception){
                _uiState.value = UIState.Fail(e)
            }
        }
    }
}

sealed class UIState {
    data class Success(val records: PagingData<RecordsForHourModel>) : UIState()

    data class Fail(val error: Exception) : UIState()
}