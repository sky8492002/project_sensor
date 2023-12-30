package com.choi.sensorproject.domain.usecase.appinfo

import com.choi.sensorproject.domain.mapper.toUiModel
import com.choi.sensorproject.domain.model.AppInfoModel
import com.choi.sensorproject.domain.repository.AppInfoRepository
import com.choi.sensorproject.ui.model.AppInfoUIModel
import javax.inject.Inject

class GetAppInfoUseCase @Inject constructor(
    private val repository: AppInfoRepository
){
    suspend operator fun invoke(appName: String):AppInfoUIModel{
        return repository.getAppInfo(appName).toUiModel()
    }
}