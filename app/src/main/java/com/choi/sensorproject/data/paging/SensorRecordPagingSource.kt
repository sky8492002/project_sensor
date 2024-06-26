package com.choi.sensorproject.data.paging

import android.annotation.SuppressLint
import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.choi.sensorproject.data.mapper.toRecordsForHourModels
import com.choi.sensorproject.domain.model.RecordsForHourModel
import com.choi.sensorproject.domain.repository.SensorRecordRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar

// 페이지의 끝에 도달할 때 다음 날짜의 데이터를 room에서 가져오도록 하는 커스텀 PagingSource
class SensorRecordPagingSource (
    private val sensorRecordRepository: SensorRecordRepository,
    private val initPageDate: String
) : PagingSource<String, RecordsForHourModel>() {

    @SuppressLint("SimpleDateFormat")
    val dayFormat = SimpleDateFormat("yyyy-MM-dd")

    // paging 시작 지점 초기값 = 생성자로 받은 initPageDate
    private var INIT_PAGE_DATE = initPageDate

    // 연결된 PagingAdapter가 refresh 함수 호출 시 실행됨
    override fun getRefreshKey(state: PagingState<String, RecordsForHourModel>): String? {
        // refresh 하기 직전에 보던 날짜로 위치 잡음
//        state.anchorPosition?.let { anchorPosition ->
//            val calendar = Calendar.getInstance()
//            state.closestPageToPosition(anchorPosition)?.nextKey?.let{ key ->
//                dayFormat.parse(key)?.let{ date ->
//                    calendar.time = date
//                    calendar.add(Calendar.DATE, -1)
//                    return dayFormat.format(calendar.time)
//                }
//            }
//            state.closestPageToPosition(anchorPosition)?.prevKey?.let{ key ->
//                dayFormat.parse(key)?.let{ date ->
//                    calendar.time = date
//                    calendar.add(Calendar.DATE, 1)
//                    return dayFormat.format(calendar.time)
//                }
//            }
//        }
        return null
    }

    //  recyclerview를 왼쪽으로 이동하면 하루 전 날짜의 데이터를, 오른쪽으로 이동하면 하루 뒤 날짜의 데이터를 가져오도록 설정
    override suspend fun load(params: LoadParams<String>): LoadResult<String, RecordsForHourModel> {
        return try {
            withContext(Dispatchers.IO) {
                val pageDate = (params.key ?: INIT_PAGE_DATE) as String
                val pageDateTime = dayFormat.parse(pageDate)

                Log.d("getFromDB", "$pageDate 가져오기 시작")

                // 해당 날짜에 기록된 데이터를 가져옴 (UseCase에 요청)
                // LoadResult을 바로 return해야 하기 때문에 flow collect 사용하기 어려움
                val sensorRecordModelList = sensorRecordRepository.getSensorRecords(pageDate)

                Log.d("getFromDB", "$pageDate 가져오기 완료")

                // 시간 별로 통합하여 분류
                val recordsForHourModelList = sensorRecordModelList.toRecordsForHourModels(pageDate)

                Log.d("getFromDB", "$pageDate 분류 완료")

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

            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}