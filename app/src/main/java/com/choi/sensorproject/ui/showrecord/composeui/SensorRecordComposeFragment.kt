package com.choi.sensorproject.ui.showrecord.composeui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.choi.sensorproject.ui.model.RecordsForHourUIModel
import com.choi.sensorproject.ui.model.SensorRecordUIModel
import com.choi.sensorproject.ui.opngl.CustomCalendarGLSurfaceView
import com.choi.sensorproject.ui.opngl.CustomGLSurfaceView
import com.choi.sensorproject.ui.showrecord.CalendarListener
import com.choi.sensorproject.ui.showrecord.CustomBalanceView
import com.choi.sensorproject.ui.showrecord.CustomClockSurfaceView
import com.choi.sensorproject.ui.showrecord.DrawSuccessListener
import com.choi.sensorproject.ui.showrecord.TouchListener
import com.choi.sensorproject.ui.theme.GodoTypography
import com.choi.sensorproject.ui.viewmodel.ManageAppInfoViewModel
import com.choi.sensorproject.ui.viewmodel.ManageSensorRecordViewModel
import com.example.sensorproject.R
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.util.Date
import kotlin.math.abs

class SensorRecordComposeFragment: Fragment() {

    private var deviceSize = IntSize(0, 0)
    private var clockSize = IntSize(0, 0)

    private val manageSensorRecordViewModel: ManageSensorRecordViewModel by viewModels()
    private val manageAppInfoViewModel: ManageAppInfoViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MainView()
            }
        }
    }

    @Composable
    fun MainView(manageSensorRecordViewModel: ManageSensorRecordViewModel = hiltViewModel(),
                  manageAppInfoViewModel: ManageAppInfoViewModel = hiltViewModel()){
        // hiltViewModel 대신 viewModel을 쓰면 App Crash (@HiltViewModel로 ViewModel을 만들었기 때문)

        var curPagingData: PagingData<RecordsForHourUIModel>? by remember { mutableStateOf(null) }

        LaunchedEffect(Unit) {
            SensorRecordLogic.mainViewChangeListener = object: MainViewChangeListener{
                override fun onRecordPagingDataChange(pagingData: PagingData<RecordsForHourUIModel>) {
                    curPagingData = pagingData
                }
            }
            SensorRecordLogic.runSensorRecordCollector(manageSensorRecordViewModel.uiState)
            SensorRecordLogic.runAppInfoCollector(manageAppInfoViewModel.uiState)
        }


        LoadingDialog()

        Box(modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned {
                deviceSize = it.size
            }){
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ){

                RecordTextView()

                Box{
                    ClockView()
                    OpenGLView()
                }

                BalanceView()

                PagingView(curPagingData)
            }

            Column(modifier = Modifier.align(Alignment.BottomCenter)){
                CalendarGLView()
            }
            Column(modifier = Modifier.align(Alignment.BottomCenter)){
                CalendarButton()
            }
        }

    }

    @Composable
    fun RecordTextView(){
        var curRecord: SensorRecordUIModel? by remember { mutableStateOf(null) }

        LaunchedEffect(Unit) {
            SensorRecordLogic.recordTextViewChangeListener = object : RecordTextViewChangeListener{
                override fun onCurSensorRecordChange(model: SensorRecordUIModel) {
                    curRecord = model
                }
            }
        }

        Text(text = "기록 시간" + curRecord?.recordTime)
        Text(text = "앱 패키지명" + curRecord?.runningAppName)
    }

    @Composable
    fun ClockView(){
        var curRecordsForHourModel: RecordsForHourUIModel? by remember { mutableStateOf(null) }
        var curPlayRecordJob: Job? by remember { mutableStateOf(null) }
        val scope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            SensorRecordLogic.clockViewChangeListener = object : ClockViewChangeListener {
                override fun onCurRecordsForHourChange(model: RecordsForHourUIModel) {
                    curRecordsForHourModel = model
                }
            }
        }

        // AndroidView를 사용하여 기존의 View를 호출할 수 있음
        AndroidView(
            factory = { context ->
                CustomClockSurfaceView(context)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .onGloballyPositioned {
                    clockSize = it.size
                },
            update = { view ->
                view.touchListener = object : TouchListener {
                    override fun onSensorRecordTouch(
                        sensorRecordUIModel: SensorRecordUIModel,
                    ) {
                        // 실시간으로 화면에 기록을 보여주던 이전 coroutine job cancel 필수
                        curPlayRecordJob?.let{ job ->
                            if(job.isActive) {
                                job.cancel()
                            }
                        }

                        // 새로운 coroutine job launch
                        curRecordsForHourModel?.let{
                            curPlayRecordJob = SensorRecordLogic.playRecord(scope, it, sensorRecordUIModel.recordTime, clockSize)
                        }

                    }
                }

                view.drawSuccessListener = object : DrawSuccessListener {
                    override fun onDrawSuccess() {
                        SensorRecordLogic.changeLoadingDialog(false)

                        // 실시간으로 화면에 기록을 보여주던 이전 coroutine job cancel 필수
                        curPlayRecordJob?.let{ job ->
                            if(job.isActive) {
                                job.cancel()
                            }
                        }

                        // 변경된 데이터(centerModel)를 balanceView에 적용
                        SensorRecordLogic.changeBalanceView(curRecordsForHourModel)

                        // 실시간으로 화면에 기록을 보여주는 새로운 coroutine job launch
                        curRecordsForHourModel?.let{
                            curPlayRecordJob = SensorRecordLogic.playRecord(scope, it, null, clockSize)
                        }
                    }
                }
                curRecordsForHourModel?.let{
                    view.setCurModel(it)
                    SensorRecordLogic.changeLoadingDialog(true)
                }
            }
        )
    }

    @Composable
    fun OpenGLView(){
        var curRecord: SensorRecordUIModel? by remember { mutableStateOf(null) }
        var curPinPoint: FloatArray? by remember { mutableStateOf(null) }
        var curPhoneAngle: FloatArray? by remember { mutableStateOf(null) }
        var curAppIcon: Bitmap? by remember { mutableStateOf(null) }
        var curAppPlayingImage: Bitmap? by remember { mutableStateOf(null) }

        LaunchedEffect(Unit) {
            SensorRecordLogic.openGLViewChangeListener = object: OpenGLViewChangeListener{
                override fun onCurSensorRecordChange(model: SensorRecordUIModel) {
                    curRecord = model
                    curPinPoint = SensorRecordLogic.getPinPoint(model, clockSize)
                    curAppIcon = SensorRecordLogic.getAppIcon(model.runningAppName)

                    // 화면이 켜진 기록일 경우 실행 중이었던 앱 별 미리 설정해 둔 이미지를 띄움
                    curAppPlayingImage = if(model.isScreenOn == true){
                        SensorRecordLogic.getPlayingImage(model.runningAppName)
                    } else{
                        BitmapFactory.decodeResource(resources, R.drawable.phone_off)
                    }
                }

                override fun onPhoneAngleChange(phoneAngle: FloatArray) {
                    curPhoneAngle = phoneAngle
                }
            }
        }

        // AndroidView를 사용하여 기존의 View를 호출할 수 있음
        AndroidView(
            factory = { context ->
                CustomGLSurfaceView(context)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp),
            update = { view ->
                curPinPoint?.let{
                    view.changePinLocation(it[0], it[1], 0f)
                }
                curPhoneAngle?.let{
                    view.changePhoneAngle(it[0], it[1], it[2])
                }
                view.changeAppIcon(curAppIcon)
                view.changeAppPlayingImage(curAppPlayingImage)
            }
        )
    }

    @Composable
    fun BalanceView(){
        var curRecordsForHourModel: RecordsForHourUIModel? by remember { mutableStateOf(null) }

        LaunchedEffect(Unit) {
            SensorRecordLogic.balanceViewChangeListener = object : BalanceViewChangeListener {
                override fun onCurRecordsForHourChange(model: RecordsForHourUIModel) {
                    curRecordsForHourModel = model
                }
            }
        }

        AndroidView(
            factory = { context ->
                CustomBalanceView(context)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            update = { view ->
                curRecordsForHourModel?.let{
                    view.setCurModel(it)
                }
            }
        )
    }

    @Composable
    fun PagingView(pagingData: PagingData<RecordsForHourUIModel>?){
        var curForceScrollType by remember { mutableStateOf( SensorRecordLogic.ForceScrollType.NONE)}
        var lazyPagingItems: LazyPagingItems<RecordsForHourUIModel>? by remember { mutableStateOf(null) }

        LaunchedEffect(Unit) {
            SensorRecordLogic.pagingViewChangeListener = object : PagingViewChangeListener {
                override fun onForceScrollTypeChange(forceScrollType: SensorRecordLogic.ForceScrollType) {
                    curForceScrollType = forceScrollType
                }

                override fun onRefreshPage(initPageDate: String) {
                    manageSensorRecordViewModel.changeInitPageDate(initPageDate)
                    lazyPagingItems?.refresh()
                }
            }
        }

        pagingData?.let{
            lazyPagingItems = remember(it) {
                flow {
                    emit(it)
                }
            }.collectAsLazyPagingItems()
            lazyPagingItems?.let{
                SensorRecordLogic.manageLoadState(it.loadState)
                RecordsForHourLazyRowView(it, curForceScrollType)
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class) // 실험용 api를 사용할 때 추가하며, 실험용 api는 미래에 수정 또는 제거될 수 있다.
    @Composable
    fun RecordsForHourLazyRowView(models: LazyPagingItems<RecordsForHourUIModel>?, forceScrollType: SensorRecordLogic.ForceScrollType){
        // 스크롤 위치를 기억
        val state = rememberLazyListState()

        // 스크롤이 멈췄을 때에만 화면을 업데이트 (RecyclerView.SCROLL_STATE_IDLE 대체)
        LaunchedEffect(state) {
            // snapshotFlow: State -> Flow 변환
            snapshotFlow { state.isScrollInProgress }
                .collect {
                    if(it.not() && models?.itemCount != 0){
                        val centerModelIndex = state.firstVisibleItemIndex + 2
                        val centerModel = models?.get(centerModelIndex)
                        SensorRecordLogic.changeClockView(centerModel)
                    }
                }
        }

        models?.let{
            when(forceScrollType){
                SensorRecordLogic.ForceScrollType.NONE -> {}
                SensorRecordLogic.ForceScrollType.REFRESH -> {
                    // refresh 후 강제 스크롤 필요 (처음 실행, 날짜 이동, 새로고침 등)
                    viewLifecycleOwner.lifecycleScope.launch{
                        val centerModelIndex = SensorRecordLogic.getScrollPosition(manageSensorRecordViewModel.getInitPageDate(), it) + 2
                        //val centerModel = it.get(centerModelIndex)
                        //SensorRecordLogic.changeClockView(centerModel)
                        state.scrollToItem(centerModelIndex)
                    }
                }
                SensorRecordLogic.ForceScrollType.APPEND -> {}
                SensorRecordLogic.ForceScrollType.PREPEND -> {
                    // 앞에 데이터가 추가된 경우 스크롤 위치 조정이 필요함
                    viewLifecycleOwner.lifecycleScope.launch{
                    }
                }
            }
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                // flingBehavior: PagerSnapHelper 대체
                state = state, flingBehavior = rememberSnapFlingBehavior(lazyListState = state)) {
                items(count = it.itemCount) { index ->
                    val item = it[index]
                    if (item != null) {
                        RecordsForHourItemView(item)
                    }
                }
            }
        }
    }

    @Composable
    fun RecordsForHourItemView(item: RecordsForHourUIModel){
        var curBackgroundImageId by remember { mutableStateOf(R.drawable.background_gray) }
        // 가운데에서 얼마나 멀어졌는지에 따라 y축 높이 다르게 적용
        var curLocateX by remember { mutableStateOf(0f)}
        var curLocateY by remember { mutableStateOf(0f)}

        when(item.hour.toInt()) {
            in 0..4 -> {
                curBackgroundImageId = R.drawable.background_night_green
            }
            in 5..7 -> {
                curBackgroundImageId = R.drawable.background_sunset_green
            }
            in 8..16 -> {
                curBackgroundImageId = R.drawable.background_light_sky_green
            }
            in 17..19 -> {
                curBackgroundImageId = R.drawable.background_sunset_green
            }
            in 20..23 -> {
                curBackgroundImageId = R.drawable.background_night_green
            }
            else -> {
                curBackgroundImageId = R.drawable.background_gray
            }
        }

        OutlinedCard(modifier = Modifier
            .width(90.dp)
            .height(100.dp)
            .padding(5.dp)
            .onGloballyPositioned {
                curLocateX = it.positionInParent().x
                curLocateY =
                    it.positionInParent().y + abs(deviceSize.width / 2 - it.positionInParent().x - it.size.width / 2) / 10
            }
            .offset(y = curLocateY.dp)){
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = curBackgroundImageId),
                    contentDescription = "",
                    contentScale = ContentScale.Crop
                )

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceAround
                ) {
                    Text(text = item.date, style = GodoTypography.titleSmall)
                    Text(text = item.hour, style = GodoTypography.bodySmall)
                }
            }
        }
    }

    @Composable
    fun CalendarGLView(){
        var curVisibility by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            SensorRecordLogic.calendarGLViewChangeListener = object : CalendarGLViewChangeListener{
                override fun onChangeVisibility(isVisible: Boolean) {
                    curVisibility = isVisible
                }
            }
        }
        AnimatedVisibility(
            visible = curVisibility,
            enter = slideInVertically(
                initialOffsetY = { fullHeight -> 2 * fullHeight },
                animationSpec = tween(durationMillis = 1000, easing = FastOutLinearInEasing)
            ),
            exit = slideOutVertically(
                targetOffsetY = { fullHeight -> 2 * fullHeight },
                animationSpec = tween(durationMillis = 1000, easing = FastOutLinearInEasing)
            )
        ){
            AndroidView(
                factory = { context ->
                    CustomCalendarGLSurfaceView(context)
                },
                modifier = Modifier
                    .width(350.dp)
                    .height(350.dp)
                    .onGloballyPositioned {
                        clockSize = it.size
                    },
                update = { view ->
                    view.reset()
                    view.renderer.calendarListener = object: CalendarListener{
                        override fun onSelectedDateUpdate(selectedDate: Date) {
                            SensorRecordLogic.refreshPage(selectedDate)
                        }
                    }
                }
            )
        }
    }

    @Composable
    fun CalendarButton(){
        var calendarVisible by remember { mutableStateOf(false) }

        Image(
            modifier = Modifier
                .width(50.dp)
                .height(50.dp)
                .clickable {
                    calendarVisible = if (calendarVisible) {
                        SensorRecordLogic.changeCalendarGLView(false)
                        false
                    } else {
                        SensorRecordLogic.changeCalendarGLView(true)
                        true
                    }
                },
            painter = painterResource(id = R.drawable.loading),
            contentDescription = "",
            contentScale = ContentScale.Crop
        )
    }

    @Composable
    fun LoadingDialog(){
        var showDialog by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            SensorRecordLogic.loadingDialogChangeListener = object: LoadingDialogChangeListener{
                override fun onLoadingDialogChange(isShowing: Boolean) {
                    showDialog = isShowing
                }
            }
        }

        if(showDialog) {
            Dialog(
                onDismissRequest = { },
                properties = DialogProperties(
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true
                )
            ) {
                // dialog의 배경을 투명하게 설정
                val window = (LocalView.current.parent as DialogWindowProvider).window
                window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                // 레이아웃 적용을 화면 전체로 변경
                val params = window.attributes
                params?.width = WindowManager.LayoutParams.MATCH_PARENT
                params?.height = WindowManager.LayoutParams.MATCH_PARENT
                window.attributes = params
                window.setWindowAnimations(android.R.style.Animation) // 아래에서 위로 올라오는 dialog 기본 효과를 사용하지 않음

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.loading),
                        contentDescription = null
                    )
                    Text("Loading...")
                }
            }
        }
    }
}