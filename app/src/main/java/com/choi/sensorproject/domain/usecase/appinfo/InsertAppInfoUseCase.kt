package com.choi.sensorproject.domain.usecase.appinfo

import com.choi.sensorproject.domain.mapper.toModel
import com.choi.sensorproject.domain.repository.AppInfoRepository
import com.choi.sensorproject.ui.model.AppInfoUIModel
import javax.inject.Inject

class InsertAppInfoUseCase @Inject constructor(
    private val repository: AppInfoRepository
) {
    suspend operator fun invoke(appInfoUIModel: AppInfoUIModel){
        repository.insertAppInfo(appInfoUIModel.toModel())
    }
}