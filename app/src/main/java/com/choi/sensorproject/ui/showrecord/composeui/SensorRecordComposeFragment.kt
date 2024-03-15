package com.choi.sensorproject.ui.showrecord.composeui

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
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
import com.choi.sensorproject.ui.opngl.CustomGLSurfaceView
import com.choi.sensorproject.ui.showrecord.CustomBalanceView
import com.choi.sensorproject.ui.showrecord.CustomClockSurfaceView
import com.choi.sensorproject.ui.showrecord.DrawSuccessListener
import com.choi.sensorproject.ui.viewmodel.ManageAppInfoViewModel
import com.choi.sensorproject.ui.viewmodel.ManageSensorRecordViewModel
import com.choi.sensorproject.ui.viewmodel.SensorRecordUIState
import com.example.sensorproject.R
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

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

        // UI를 변경하는 변수 목록
        var curRecordsForHourModel: RecordsForHourUIModel? by remember { mutableStateOf(null) }
        var curRecord: SensorRecordUIModel? by remember { mutableStateOf(null) }
        var curPinPoint: FloatArray? by remember { mutableStateOf(null) }
        var curPhoneAngle: FloatArray? by remember { mutableStateOf(null) }
        var curAppIcon: Bitmap? by remember { mutableStateOf(null) }
        var curAppPlayingImage: Bitmap? by remember { mutableStateOf(null) }

        var curPagingData: PagingData<RecordsForHourUIModel>? by remember { mutableStateOf(null) }

        LaunchedEffect(Unit) {
            SensorRecordLogic.mainViewChangeListener = object: MainViewChangeListener{
                override fun onRecordPagingDataChange(pagingData: PagingData<RecordsForHourUIModel>) {
                    curPagingData = pagingData
                }

                override fun onCurSensorRecordChange(model: SensorRecordUIModel) {
                    curRecord = model
                    curPinPoint = SensorRecordLogic.getPinPoint(model, clockSize)
                    curAppIcon = SensorRecordLogic.getAppIcon(model.runningAppName)
                    curAppPlayingImage = SensorRecordLogic.getPlayingImage(model.runningAppName)
                }

                override fun onCurRecordsForHourChange(model: RecordsForHourUIModel) {
                    curRecordsForHourModel = model
                }
            }
            SensorRecordLogic.runSensorRecordCollector(manageSensorRecordViewModel.uiState)
            SensorRecordLogic.runAppInfoCollector(manageAppInfoViewModel.uiState)
        }


        LoadingDialog()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned {
                    deviceSize = it.size
                }){
            Text(text = "기록 시간" + curRecord?.recordTime)
            Text(text = "앱 패키지명" + curRecord?.runningAppName)

            Box{
                ClockView(curRecordsForHourModel)
                OpenGLView(curPinPoint, curPhoneAngle, curAppIcon, curAppPlayingImage)
            }

            BalanceView(curRecordsForHourModel)

            PagingView(curPagingData)
        }
    }

    // 기록 시간 단위로 변경되는 UI
    @Composable
    fun UpdateInRecordHourView(recordsForHourUIModel: RecordsForHourUIModel?){

    }

    // 기록 초 단위로 변경되는 UI
    @Composable
    fun UpdateInRecordSecondsView(curPinPoint: FloatArray?, curPhoneAngle: FloatArray?, curAppIcon: Bitmap?, curAppPlayingImage: Bitmap?){

    }

    @Composable
    fun ClockView(recordsForHourUIModel: RecordsForHourUIModel?){
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
                view.drawSuccessListener = object : DrawSuccessListener {
                    override fun onDrawSuccess() {
                        SensorRecordLogic.changeLoadingDialog(false)

//                        // 실시간으로 화면에 기록을 보여주던 이전 coroutine job cancel 필수 (한번 더 확인)
//                        curUIJob?.let{ job ->
//                            if(job.isActive) {
//                                job.cancel()
//                            }
//                        }
//
//                        // 변경된 데이터(centerModel)를 balanceView에 적용
//                        binding.customBalanceView.setCurModel(centerModel)
//
//                        // 실시간으로 화면에 기록을 보여주는 새로운 coroutine job launch
//                        curUIJob = runUIJobByRecordsForHour(centerModel, null)
                    }
                }
                recordsForHourUIModel?.let{
                    view.setCurModel(it)
                    SensorRecordLogic.changeLoadingDialog(true)
                }
            }
        )
    }

    @Composable
    fun OpenGLView(curPinPoint: FloatArray?, curPhoneAngle: FloatArray?, curAppIcon: Bitmap?, curAppPlayingImage: Bitmap?){
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
    fun BalanceView(recordsForHourUIModel: RecordsForHourUIModel?){
        AndroidView(
            factory = { context ->
                CustomBalanceView(context)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            update = { view ->
                recordsForHourUIModel?.let{
                    view.setCurModel(it)
                }
            }
        )
    }

    @Composable
    fun PagingView(pagingData: PagingData<RecordsForHourUIModel>?){
        var curForceScrollType by remember { mutableStateOf( SensorRecordLogic.ForceScrollType.NONE)}

        LaunchedEffect(Unit) {
            SensorRecordLogic.pagingViewChangeListener = object : PagingViewChangeListener {
                override fun onForceScrollTypeChange(forceScrollType: SensorRecordLogic.ForceScrollType) {
                    curForceScrollType = forceScrollType
                }
            }
        }

        pagingData?.let{
            val lazyPagingItems = remember(it) {
                flow {
                    emit(it)
                }
            }.collectAsLazyPagingItems()
            SensorRecordLogic.manageLoadState(lazyPagingItems.loadState)

            RecordsForHourLazyRowView(lazyPagingItems, curForceScrollType)
        }
    }
//    @Composable
//    fun PagingView(uiState: StateFlow<SensorRecordUIState>) {
//        var lastLoadingType by remember { mutableStateOf(LoadingType.NONE) }
//
//        var showDialog by remember { mutableStateOf(false) }
//        LoadingDialog(showDialog)
//
//        var models: LazyPagingItems<RecordsForHourUIModel>? by remember { mutableStateOf(null) }
//        var forceScrollType by remember { mutableStateOf(ForceScrollType.NONE) }
//        RecordsForHourLazyRowView(models, forceScrollType)
//
//
//        uiState.collectAsState().value.let {
//            if (it is SensorRecordUIState.Success) {
//                forceScrollType = ForceScrollType.NONE
//
//                // remember(key) : key 값이 변경되었을 때만 값이 갱신된다. (그 외의 recomposition 수행 시 유지된다.)
//                val lazyPagingItems = remember(it.records) {
//                    flow {
//                        emit(it.records)
//                    }
//                }.collectAsLazyPagingItems()
//
//                // 로드 상태 관찰
//                val loadState = lazyPagingItems.loadState
//                // LaunchedEffect(key) : key 값이 변경되었을 때만 내부 코드가 실행된다.
//                LaunchedEffect(key1 = loadState) {
//                    when {
//                        loadState.refresh is LoadState.Loading -> {
//                            showDialog = true
//                            lastLoadingType = LoadingType.REFRESH
//                        }
//                        loadState.append is LoadState.Loading ->{
//                            showDialog = true
//                            lastLoadingType = LoadingType.APPEND
//                        }
//                        loadState.prepend is LoadState.Loading ->{
//                            showDialog = true
//                            lastLoadingType = LoadingType.PREPEND
//                        }
//                        loadState.refresh is LoadState.NotLoading && loadState.append is LoadState.NotLoading && loadState.prepend is LoadState.NotLoading-> {
//                            showDialog = false
//                            models = lazyPagingItems
//
//                            forceScrollType = when(lastLoadingType){
//                                LoadingType.NONE -> {
//                                    ForceScrollType.NONE
//                                }
//
//                                LoadingType.REFRESH -> {
//                                    ForceScrollType.REFRESH
//                                }
//
//                                LoadingType.APPEND -> {
//                                    ForceScrollType.APPEND
//                                }
//
//                                LoadingType.PREPEND -> {
//                                    ForceScrollType.PREPEND
//                                }
//                            }
//                            lastLoadingType = LoadingType.NONE
//                        }
//                    }
//                }
//            }
//            else{
//                requireActivity().finish()
//            }
//        }
//
//    }

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
                        val centerModel = models?.get(state.firstVisibleItemIndex + 2)
                        SensorRecordLogic.changeCurRecordsForHourModel(centerModel)
                    }
                }
        }

        models?.let{
            when(forceScrollType){
                SensorRecordLogic.ForceScrollType.NONE -> {}
                SensorRecordLogic.ForceScrollType.REFRESH -> {
                    // refresh 후 강제 스크롤 필요 (처음 실행, 날짜 이동, 새로고침 등)
                    viewLifecycleOwner.lifecycleScope.launch{
                        state.scrollToItem(SensorRecordLogic.getScrollPosition(manageSensorRecordViewModel.getInitPageDate(), it))
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
        var backgroundImageId by remember { mutableStateOf(R.drawable.background_gray) }

        when(item.hour.toInt()) {
            in 0..4 -> {
                backgroundImageId = R.drawable.background_night_green
            }
            in 5..7 -> {
                backgroundImageId = R.drawable.background_sunset_green
            }
            in 8..16 -> {
                backgroundImageId = R.drawable.background_light_sky_green
            }
            in 17..19 -> {
                backgroundImageId = R.drawable.background_sunset_green
            }
            in 20..23 -> {
                backgroundImageId = R.drawable.background_night_green
            }
            else -> {
                backgroundImageId = R.drawable.background_gray
            }
        }

        OutlinedCard(modifier = Modifier
            .width(90.dp)
            .height(100.dp)){
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = backgroundImageId),
                    contentDescription = "",
                    contentScale = ContentScale.Crop
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(item.date)
                    Text(item.hour)
                }
            }
        }
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