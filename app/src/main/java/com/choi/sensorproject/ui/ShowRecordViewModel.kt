package com.choi.sensorproject.ui

import com.choi.sensorproject.domain.usecase.GetSensorRecordsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ShowRecordViewModel  @Inject constructor(
    private val getSensorRecordsUseCase: GetSensorRecordsUseCase
){

}