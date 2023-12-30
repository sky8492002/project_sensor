package com.choi.sensorproject.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.choi.sensorproject.domain.usecase.appinfo.GetAppInfoUseCase
import com.choi.sensorproject.domain.usecase.appinfo.InsertAppInfoUseCase
import com.choi.sensorproject.ui.model.AppInfoUIModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManageAppInfoViewModel @Inject constructor(
    private val insertAppInfoUseCase: InsertAppInfoUseCase,
    private val getAppInfoUseCase: GetAppInfoUseCase
) : ViewModel(){

    private val _uiState =
        MutableStateFlow <AppInfoUIState>(AppInfoUIState.Success(mutableListOf()))

    val uiState: StateFlow<AppInfoUIState> = _uiState


    suspend fun insertAppInfo(appInfoUiModel: AppInfoUIModel){
        insertAppInfoUseCase(appInfoUiModel)
    }

    private suspend fun getAllAppInfos(): Flow<List<AppInfoUIModel>> {
        return getAppInfoUseCase.getAllAppInfos()
    }

    init{
        viewModelScope.launch(Dispatchers.IO) {
            try {
                getAllAppInfos().collectLatest {
                    _uiState.value = AppInfoUIState.Success(it)
                }
            }
            catch (e: Exception){
                _uiState.value = AppInfoUIState.Fail(e)
            }
        }
    }

}

sealed class AppInfoUIState {
    data class Success(val appInfoList: List<AppInfoUIModel>): AppInfoUIState()

    data class Fail(val error: Exception) : AppInfoUIState()
}