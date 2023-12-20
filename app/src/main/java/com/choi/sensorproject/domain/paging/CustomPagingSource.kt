package com.choi.sensorproject.domain.paging

import android.annotation.SuppressLint
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.choi.sensorproject.domain.model.SensorRecordModel
import com.choi.sensorproject.domain.usecase.GetSensorRecordsUseCase
import com.choi.sensorproject.ui.model.RecordsForHourModel
import kotlinx.coroutines.flow.collect
import java.text.SimpleDateFormat
import java.util.Calendar
import javax.inject.Inject

// 페이지의 끝에 도달할 때 다음 날짜의 데이터를 room에서 가져오도록 하는 커스텀 PagingSource
class CustomPagingSource @Inject constructor(
    private val getSensorRecordsUseCase: GetSensorRecordsUseCase
) : PagingSource<String, RecordsForHourModel>() {

    lateinit var INIT_PAGE_DATE: String
    @SuppressLint("SimpleDateFormat")
    val dayFormat = SimpleDateFormat("yyyy-MM-dd")
    @SuppressLint("SimpleDateFormat")
    val hourFormat = SimpleDateFormat("HH")
    val currentTime : Long = System.currentTimeMillis()

    // paging 시작 지점 초기값 = 오늘 날짜
    init{
        INIT_PAGE_DATE = dayFormat.format(currentTime)
    }
    override fun getRefreshKey(state: PagingState<String, RecordsForHourModel>): String? {
        return null
    }

    //  recyclerview를 왼쪽으로 이동하면 하루 전 날짜의 데이터를, 오른쪽으로 이동하면 하루 뒤 날짜의 데이터를 가져오도록 설정
    override suspend fun load(params: LoadParams<String>): LoadResult<String, RecordsForHourModel> {
        return try {
            val pageDate = (params.key ?: INIT_PAGE_DATE) as String
            val pageDateTime = dayFormat.parse(pageDate)

            val recordsForHourModelList: MutableList<RecordsForHourModel> = mutableListOf()

            for(hour in 0 until 24){
                val emptyRecords: MutableList<SensorRecordModel> = mutableListOf()
                recordsForHourModelList.add(RecordsForHourModel(pageDate, hour, emptyRecords))
            }

            // 해당 날짜에 기록된 데이터를 가져옴 (UseCase에 요청)
            getSensorRecordsUseCase(pageDate).collect(){
                for(sensorRecordModel in it){
                    val hour = hourFormat.format(sensorRecordModel.recordTime).toInt()
                    recordsForHourModelList[hour].records.add(sensorRecordModel)
                }
            }


            // 어제, 내일 날짜 구하기
            val calendar = Calendar.getInstance()
            if (pageDateTime != null) {
                calendar.time = pageDateTime
            }

            calendar.add(Calendar.DATE, -1)
            val prevKey = dayFormat.format(calendar.time)
            calendar.add(Calendar.DATE, 2)
            val nextKey = dayFormat.format(calendar.time)

            // 갱신된 LoadResult를 return
            LoadResult.Page(
                data = recordsForHourModelList,
                prevKey = prevKey,
                nextKey = nextKey
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}