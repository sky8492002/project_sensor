package com.choi.sensorproject.ui.setting

import androidx.lifecycle.ViewModel
import com.choi.sensorproject.domain.usecase.appinfo.GetAppInfoUseCase
import com.choi.sensorproject.domain.usecase.appinfo.InsertAppInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ManageAppInfoViewModel @Inject constructor(
    insertAppInfoUseCase: InsertAppInfoUseCase,
    getAppInfoUseCase: GetAppInfoUseCase
) : ViewModel(){

}