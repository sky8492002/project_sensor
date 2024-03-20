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
import com.choi.sensorproject.ui.showrecord.composeui.listener.BackGroundViewChangeListener
import com.choi.sensorproject.ui.showrecord.composeui.listener.BalanceViewChangeListener
import com.choi.sensorproject.ui.showrecord.composeui.listener.CalendarGLViewChangeListener
import com.choi.sensorproject.ui.showrecord.composeui.listener.ClockViewChangeListener
import com.choi.sensorproject.ui.showrecord.composeui.listener.LazyRowViewChangeListener
import com.choi.sensorproject.ui.showrecord.composeui.listener.LoadingDialogChangeListener
import com.choi.sensorproject.ui.showrecord.composeui.listener.MainViewChangeListener
import com.choi.sensorproject.ui.showrecord.composeui.listener.OpenGLViewChangeListener
import com.choi.sensorproject.ui.showrecord.composeui.listener.PagingViewChangeListener
import com.choi.sensorproject.ui.showrecord.composeui.listener.RecordTextViewChangeListener
import com.choi.sensorproject.ui.viewmodel.AppInfoUIState
import com.choi.sensorproject.ui.viewmodel.SensorRecordUIState
import com.example.sensorproject.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date

@SuppressLint("SimpleDateFormat")
object SensorRecordLogic{

    var mainViewChangeListener: MainViewChangeListener? = null
    var backGroundViewChangeListener: BackGroundViewChangeListener?= null
    var balanceViewChangeListener: BalanceViewChangeListener?= null
    var clockViewChangeListener: ClockViewChangeListener?= null
    var recordTextViewChangeListener: RecordTextViewChangeListener?= null
    var openGLViewChangeListener: OpenGLViewChangeListener?= null
    var pagingViewChangeListener: PagingViewChangeListener? = null
    var lazyRowViewChangeListener: LazyRowViewChangeListener?= null
    var loadingDialogChangeListener: LoadingDialogChangeListener? = null
    var calendarGLViewChangeListener: CalendarGLViewChangeListener?= null

    private val dayFormat = SimpleDateFormat("yyyy-MM-dd")
    private val hourFormat = SimpleDateFormat("HH")
    private val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    private val minuteFormat = SimpleDateFormat("mm")
    private val secondFormat = SimpleDateFormat("ss")


    private var allAppInfos: List<AppInfoUIModel> = mutableListOf()

    private var lastLoadingType: LoadingType = LoadingType.NONE

    private var curPhoneViewPoint: PhoneViewPoint = PhoneViewPoint.FRONT

    enum class LoadingType{
        NONE, REFRESH, APPEND, PREPEND
    }

    enum class ForceScrollType{
        NONE, REFRESH, APPEND, PREPEND
    }

    enum class PhoneViewPoint{
        FRONT, BACK
    }

    enum class BackgroundImageStyle{
        GRAY, GREEN
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

    fun changeBackgroundView(hour: Int?){
        backGroundViewChangeListener?.onBackgroundImageChange(getBackgroundImageId(hour, BackgroundImageStyle.GRAY))
    }

    fun changeClockView(model: RecordsForHourUIModel?){
        model?.let{
            clockViewChangeListener?.onCurRecordsForHourChange(it)
        }
    }

    fun changeBalanceView(model: RecordsForHourUIModel?){
        model?.let{
            balanceViewChangeListener?.onCurRecordsForHourChange(it)
        }
    }

    fun changeCalendarGLView(isVisible: Boolean){
        calendarGLViewChangeListener?.onChangeVisibility(isVisible)
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
                        return index + 12
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
                        lazyRowViewChangeListener?.onForceScrollTypeChange(ForceScrollType.NONE)
                    }
                    LoadingType.REFRESH -> {
                        lazyRowViewChangeListener?.onForceScrollTypeChange(ForceScrollType.REFRESH)
                    }
                    LoadingType.APPEND -> {
                        lazyRowViewChangeListener?.onForceScrollTypeChange(ForceScrollType.APPEND)
                    }
                    LoadingType.PREPEND -> {
                        lazyRowViewChangeListener?.onForceScrollTypeChange(ForceScrollType.PREPEND)
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

    // startTime 이후의 RecordsForHourUIModel 내부 기록을 보여줌
    fun playRecord(scope: CoroutineScope, recordsForHourUIModel: RecordsForHourUIModel, startTime: String?, clockSize: IntSize): Job {

        return scope.launch {
            var lastXAngle: Float? = null
            var lastZAngle: Float? = null
            var lastYAngle: Float? = null

            // 시작 전 화면 초기화
            openGLViewChangeListener?.onReset()
            recordTextViewChangeListener?.onReset()

            for(record in recordsForHourUIModel.records){
                // 시작 시간보다 이르면 화면에 표시하지 않고 넘어감
                if((startTime != null) && (timeFormat.parse(startTime)!! > timeFormat.parse(record.recordTime))){
                    continue
                }

                openGLViewChangeListener?.onCurSensorRecordChange(record)
                recordTextViewChangeListener?.onCurSensorRecordChange(record)

                val curXAngle = record.xAngle
                var curYAngle = 0f
                var curZAngle = record.zAngle

                // 보는 시점이 뒤쪽이면 curYAngle, curZAngle 다르게 설정
                if(curPhoneViewPoint == PhoneViewPoint.BACK){
                    curYAngle = 180f
                    curZAngle = -curZAngle
                }

                // 실제 각도와 화면이 일치하게 조정 (이전 각도와 비교 후 10밀리 간격으로 미세조정)
                if(lastXAngle != null && lastZAngle != null && lastYAngle != null){
                    val diffXAngle = curXAngle - lastXAngle
                    val diffYAngle = curYAngle - lastYAngle
                    val diffZAngle = curZAngle - lastZAngle
                    for(n in 1..10){
                        val phoneAngle = FloatArray(3)
                        val splitedXAngle = lastXAngle + diffXAngle / 10 * n
                        val splitedYAngle = lastYAngle + diffYAngle / 10 * n
                        val splitedZAngle = lastZAngle + diffZAngle / 10 * n
                        phoneAngle[0] = -splitedZAngle / 180f * 200f
                        phoneAngle[1] = splitedYAngle / 180f * 200f
                        phoneAngle[2] = splitedXAngle / 180f * 200f
                        openGLViewChangeListener?.onPhoneAngleChange(phoneAngle)
                        delay(10)
                    }
                }
                else{
                    val phoneAngle = FloatArray(3)
                    phoneAngle[0] = -curZAngle / 180f * 200f
                    phoneAngle[1] = curYAngle / 180f * 200f
                    phoneAngle[2] = curXAngle / 180f * 200f
                    openGLViewChangeListener?.onPhoneAngleChange(phoneAngle)
                    delay(100)
                }

                lastXAngle = curXAngle
                lastYAngle = curYAngle
                lastZAngle = curZAngle
            }
        }
    }

    fun refreshPage(selectedDate: Date){
        pagingViewChangeListener?.onRefreshPage(dayFormat.format(selectedDate))
    }

    fun changePhoneViewPoint(phoneViewPoint: PhoneViewPoint){
        curPhoneViewPoint = phoneViewPoint
    }

    fun getBackgroundImageId(hour: Int?, backgroundImageStyle: BackgroundImageStyle): Int{
        when(hour) {
            in 0..4 -> {
                return when(backgroundImageStyle){
                    BackgroundImageStyle.GRAY -> R.drawable.background_night_gray
                    BackgroundImageStyle.GREEN -> R.drawable.background_night_green
                }
            }
            in 5..7 -> {
                return when(backgroundImageStyle){
                    BackgroundImageStyle.GRAY -> R.drawable.background_sunset_gray
                    BackgroundImageStyle.GREEN -> R.drawable.background_sunset_green
                }
            }
            in 8..16 -> {
                return when(backgroundImageStyle){
                    BackgroundImageStyle.GRAY -> R.drawable.background_sky_gray
                    BackgroundImageStyle.GREEN -> R.drawable.background_light_sky_green
                }
            }
            in 17..19 -> {
                return when(backgroundImageStyle){
                    BackgroundImageStyle.GRAY -> R.drawable.background_sunset_gray
                    BackgroundImageStyle.GREEN -> R.drawable.background_sunset_green
                }
            }
            in 20..23 -> {
                return when(backgroundImageStyle){
                    BackgroundImageStyle.GRAY -> R.drawable.background_night_gray
                    BackgroundImageStyle.GREEN -> R.drawable.background_night_green
                }
            }
            else -> {
                return R.drawable.background_gray
            }
        }
    }
}