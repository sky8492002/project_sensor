package com.choi.sensorproject.domain.usecase.sensor

import com.choi.sensorproject.domain.model.SensorRecordModel
import com.choi.sensorproject.data.paging.SensorRecordPagingSource
import com.choi.sensorproject.domain.repository.SensorRecordRepository
import javax.inject.Inject

class GetSensorRecordsUseCase @Inject constructor(
    private val repository: SensorRecordRepository
) {
    // 날짜별 통계 분석 시 사용 예정
    suspend fun getSensorRecords(pageDate: String): List<SensorRecordModel> {
        return repository.getSensorRecords(pageDate)
    }

    fun getSensorRecordPagingSource(initPageDate: String): SensorRecordPagingSource {
        return repository.getSensorRecordPagingSource(initPageDate)
    }
}