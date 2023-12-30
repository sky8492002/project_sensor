package com.choi.sensorproject.ui.setting

import androidx.lifecycle.ViewModel
import com.choi.sensorproject.domain.usecase.appinfo.GetAppInfoUseCase
import com.choi.sensorproject.domain.usecase.appinfo.InsertAppInfoUseCase
import com.choi.sensorproject.ui.model.AppInfoUIModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ManageAppInfoViewModel @Inject constructor(
    private val insertAppInfoUseCase: InsertAppInfoUseCase,
    private val getAppInfoUseCase: GetAppInfoUseCase
) : ViewModel(){

    suspend fun insertAppInfo(appInfoUiModel: AppInfoUIModel){
        insertAppInfoUseCase.invoke(appInfoUiModel)
    }

}