package com.choi.sensorproject.ui.showrecord.composeui

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.RectF
import androidx.compose.ui.unit.IntSize
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.choi.sensorproject.ui.model.AppInfoUIModel
import com.choi.sensorproject.ui.model.RecordsForHourUIModel
import com.choi.sensorproject.ui.model.SensorRecordUIModel
import com.choi.sensorproject.ui.viewmodel.AppInfoUIState
import com.choi.sensorproject.ui.viewmodel.SensorRecordUIState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

@SuppressLint("SimpleDateFormat")
object SensorRecordLogic{

    var mainViewChangeListener: MainViewChangeListener? = null
    var pagingViewChangeListener: PagingViewChangeListener? = null
    var loadingDialogChangeListener: LoadingDialogChangeListener? = null

    private val dayFormat = SimpleDateFormat("yyyy-MM-dd")
    private val hourFormat = SimpleDateFormat("HH")
    private val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    private val minuteFormat = SimpleDateFormat("mm")
    private val secondFormat = SimpleDateFormat("ss")


    private var allAppInfos: List<AppInfoUIModel> = mutableListOf()

    private var lastLoadingType: LoadingType = LoadingType.NONE

    enum class LoadingType{
        NONE, REFRESH, APPEND, PREPEND
    }

    enum class ForceScrollType{
        NONE, REFRESH, APPEND, PREPEND
    }

    fun runSensorRecordCollector(uiState: StateFlow<SensorRecordUIState>){
        CoroutineScope(Dispatchers.IO).launch {
            uiState.collect(){ uiState ->
                if (uiState is SensorRecordUIState.Success) {
                    mainViewChangeListener?.onRecordPagingDataChange(uiState.records)
                }
                else{

                }
            }
        }
    }

    fun runAppInfoCollector(uiState: StateFlow<AppInfoUIState>){
        CoroutineScope(Dispatchers.IO).launch {
            uiState.collect(){ uiState ->
                if (uiState is AppInfoUIState.Success) {
                    allAppInfos = uiState.appInfos
                }
                else{

                }
            }
        }
    }

    fun changeCurRecordsForHourModel(model: RecordsForHourUIModel?){
        model?.let{
            mainViewChangeListener?.onCurRecordsForHourChange(it)
        }
    }
    fun changeLoadingDialog(isShowing: Boolean){
        loadingDialogChangeListener?.onLoadingDialogChange(isShowing)
    }

    fun getScrollPosition(initPageDate: String, items: LazyPagingItems<RecordsForHourUIModel>): Int{
        val curTimeMillis = System.currentTimeMillis()

        // pagingSource의 INIT_PAGE_DATE에 따라 스크롤 위치가 달라짐
        when(initPageDate){
            // 오늘 날짜인 경우 현재 시간으로 스크롤함
            dayFormat.format(curTimeMillis) -> {
                for(index in 0 until items.itemCount){
                    val curModel = items[index]
                    if(curModel != null && curModel.hour.toInt() == hourFormat.format(curTimeMillis).toInt()
                        && curModel.date == initPageDate) {
                        return index
                    }
                }
            }
            // 다른 날짜인 경우 해당 날짜로 스크롤함
            else -> {
                for(index in 0 until items.itemCount){
                    val curModel = items[index]
                    if(curModel != null && curModel.date == initPageDate){
                        return index
                    }
                }
            }
        }
        return 0
    }

    fun manageLoadState (loadState: CombinedLoadStates){
        when {
            loadState.refresh is LoadState.Loading -> {
                loadingDialogChangeListener?.onLoadingDialogChange(true)
                lastLoadingType = LoadingType.REFRESH
            }
            loadState.append is LoadState.Loading ->{
                loadingDialogChangeListener?.onLoadingDialogChange(true)
                lastLoadingType = LoadingType.APPEND
            }
            loadState.prepend is LoadState.Loading ->{
                loadingDialogChangeListener?.onLoadingDialogChange(true)
                lastLoadingType = LoadingType.PREPEND
            }
            loadState.refresh is LoadState.NotLoading && loadState.append is LoadState.NotLoading && loadState.prepend is LoadState.NotLoading-> {
                loadingDialogChangeListener?.onLoadingDialogChange(false)

                when(lastLoadingType){
                    LoadingType.NONE -> {
                        pagingViewChangeListener?.onForceScrollTypeChange(ForceScrollType.NONE)
                    }
                    LoadingType.REFRESH -> {
                        pagingViewChangeListener?.onForceScrollTypeChange(ForceScrollType.REFRESH)
                    }
                    LoadingType.APPEND -> {
                        pagingViewChangeListener?.onForceScrollTypeChange(ForceScrollType.APPEND)
                    }
                    LoadingType.PREPEND -> {
                        pagingViewChangeListener?.onForceScrollTypeChange(ForceScrollType.PREPEND)
                    }
                }
                lastLoadingType = LoadingType.NONE
            }
        }
    }
    fun getPlayingImage(appName: String): Bitmap? {
        for(appInfo in allAppInfos){
            if(appInfo.appName == appName){
                return appInfo.appPlayingImage
            }
        }
        return null
    }

    fun getAppIcon(appName: String): Bitmap? {
        for(appInfo in allAppInfos){
            if(appInfo.appName == appName){
                return appInfo.appIcon
            }
        }
        return null
    }

    fun getPinPoint(model: SensorRecordUIModel, clockSize: IntSize): FloatArray{
        val curDate = timeFormat.parse(model.recordTime)
        val curMinute = minuteFormat.format(curDate).toInt()
        val curSecond = secondFormat.format(curDate).toInt()
        val curTotalSeconds = curMinute * 60 + curSecond

        val insideRecF = RectF()
        val min = Math.min(clockSize.width, clockSize.height)
        val radius = (min  - 90) / 2
        val arcStrokeWidth = 100f
        val insideRadius = radius - arcStrokeWidth.toInt() / 2

        val centerX = (clockSize.width.div(2)).toFloat()
        val centerY = (clockSize.height.div(2)).toFloat()

        insideRecF.apply {
            set(centerX - insideRadius, centerY - insideRadius, centerX + insideRadius, centerY + insideRadius)
        }

        val startAngle = -90f + 0.1f * curTotalSeconds
        val sweepAngle = 0.1f

        val arcPath = Path()
        arcPath.arcTo(insideRecF, startAngle, sweepAngle)

        // 호 경로의 절반에 있는 지점의 좌표를 찾음
        val pm = PathMeasure(arcPath, false)
        val point = FloatArray(2)
        pm.getPosTan(pm.getLength() * 0.5f, point, null) // distance 만큼 시작 지점을 이동한 후의 위치를 point 변수에 담음

        // opengl 내부 좌표에 맞게 변환
        point[0] = (point[0] - centerX) / (clockSize.width / 2)
        point[1] = -(point[1] - centerY) / (clockSize.height / 2)

        return point
    }
}