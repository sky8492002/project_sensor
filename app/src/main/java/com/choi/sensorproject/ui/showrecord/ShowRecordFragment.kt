package com.choi.sensorproject.ui.showrecord

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.RectF
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.choi.sensorproject.service.Orientation
import com.choi.sensorproject.ui.model.AppInfoUIModel
import com.choi.sensorproject.ui.model.RecordsForHourUIModel
import com.choi.sensorproject.ui.model.SensorRecordUIModel
import com.choi.sensorproject.ui.recyclerview.FocusedLayoutManager
import com.choi.sensorproject.ui.recyclerview.RecordsForHourAdapter
import com.choi.sensorproject.ui.viewmodel.AppInfoUIState
import com.choi.sensorproject.ui.viewmodel.ManageAppInfoViewModel
import com.choi.sensorproject.ui.viewmodel.ManageSensorRecordViewModel
import com.choi.sensorproject.ui.viewmodel.SensorRecordUIState
import com.example.sensorproject.R
import com.example.sensorproject.databinding.FragmentShowRecordBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.math.abs


@SuppressLint("SimpleDateFormat")
@AndroidEntryPoint
class ShowRecordFragment: Fragment() {
    private var _binding: FragmentShowRecordBinding? = null

    private val binding
        get() = checkNotNull(_binding) { "binding was accessed outside of view lifecycle" }

    private val manageSensorRecordViewModel: ManageSensorRecordViewModel by viewModels()
    private val manageAppInfoViewModel: ManageAppInfoViewModel by viewModels()

    private var allAppInfos: List<AppInfoUIModel> = mutableListOf()

    private val dayFormat = SimpleDateFormat("yyyy-MM-dd")
    private val hourFormat = SimpleDateFormat("HH")
    private val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    private val minuteFormat = SimpleDateFormat("mm")
    private val secondFormat = SimpleDateFormat("ss")

    private var curUIJob: Job? = null

    private lateinit var centerModel: RecordsForHourUIModel

    private lateinit var dataRefreshLoadingDialog: LoadingDialog
    private lateinit var dataAppendLoadingDialog: LoadingDialog
    private lateinit var dataPrependLoadingDialog: LoadingDialog
    private lateinit var clockViewLoadingDialog: LoadingDialog

    private var isNeedToScrollAfterUpdate = true

    // 폰을 보는 시점 결정 (앞/뒤)
    private var curPhoneViewPoint = PhoneViewPoint.FRONT

    enum class PhoneViewPoint{
        FRONT, BACK
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShowRecordBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 로딩 Dialog 객체 생성
        dataRefreshLoadingDialog = LoadingDialog(requireContext())
        dataAppendLoadingDialog = LoadingDialog(requireContext())
        dataPrependLoadingDialog = LoadingDialog(requireContext())
        clockViewLoadingDialog = LoadingDialog(requireContext())

        // recyclerview 스크롤 시 하나의 아이템이 반드시 중앙에 오도록 하는 PagerSnapHelper
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(binding.timeRecyclerView)

        val recordsForHourAdapter = RecordsForHourAdapter()

        val focusedLayoutManager = FocusedLayoutManager(requireActivity().baseContext, snapHelper, -200f)
        focusedLayoutManager.orientation = LinearLayoutManager.HORIZONTAL

        binding.timeRecyclerView.adapter = recordsForHourAdapter
        binding.timeRecyclerView.layoutManager = focusedLayoutManager
        binding.manageSensorRecordViewModel = manageSensorRecordViewModel
        binding.curRecordsForHourUIModel = null

        // customClockView에서 시점을 터치하면 해당 시점의 데이터부터 보여줌
        binding.customClockView.touchListener = object : TouchListener {
            override fun onSensorRecordTouch(
                sensorRecordUIModel: SensorRecordUIModel,
            ) {
                // 이전 coroutine job cancel 필수
                curUIJob?.let{ job ->
                    if(job.isActive) {
                        job.cancel()
                    }
                }

                // 새로운 coroutine job launch
                curUIJob = timeFormat.parse(sensorRecordUIModel.recordTime)
                    ?.let { runUIJobByRecordsForHour(centerModel, it) }

            }
        }

        // 데이터 로딩 상태에 따라 관리 (loadStateFlow(비동기)와 addLoadStateListener(동기)방식이 있음)
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            recordsForHourAdapter.loadStateFlow.collect(){
                val pagingLoadStates = it.source

                if(pagingLoadStates.refresh is LoadState.Loading){
                    if(dataRefreshLoadingDialog.isShowing.not()){
                        dataRefreshLoadingDialog.show() // 데이터를 받아서 스크롤 위치가 조정될 때까지 로딩 Dialog 띄움
                    }
                    isNeedToScrollAfterUpdate = true
                }
                else if(pagingLoadStates.refresh is LoadState.NotLoading){
                    // refresh 후 강제 스크롤이 필요한 경우 (처음 실행, 날짜 이동, 새로고침 등)
                    if(isNeedToScrollAfterUpdate){
                        forceScroll(manageSensorRecordViewModel.getInitPageDate())
                        isNeedToScrollAfterUpdate = false
                    }

                    if(dataRefreshLoadingDialog.isShowing){
                        dataRefreshLoadingDialog.cancel()
                    }
                }

                if(pagingLoadStates.append is LoadState.Loading) {
                    if(dataAppendLoadingDialog.isShowing.not()){
                        dataAppendLoadingDialog.show() // 데이터를 받을 때까지 로딩 Dialog 띄움
                    }
                }
                else if(pagingLoadStates.append is LoadState.NotLoading){
                    if(dataAppendLoadingDialog.isShowing){
                        dataAppendLoadingDialog.cancel()
                    }
                }

                if(pagingLoadStates.prepend is LoadState.Loading) {
                    if(dataPrependLoadingDialog.isShowing.not()){
                        dataPrependLoadingDialog.show() // 데이터를 받을 때까지 로딩 Dialog 띄움
                    }
                }
                else if(pagingLoadStates.prepend is LoadState.NotLoading){
                    if(dataPrependLoadingDialog.isShowing){
                        dataPrependLoadingDialog.cancel()
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            manageSensorRecordViewModel.uiState.collect(){ uiState ->
                if (uiState is SensorRecordUIState.Success) {
                    recordsForHourAdapter.submitData(uiState.records) // submitData 내부에서 PagingData를 collect
                }
                else{
                    requireActivity().finish()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            manageAppInfoViewModel.uiState.collect(){ uiState ->
                if (uiState is AppInfoUIState.Success) {
                    allAppInfos = uiState.appInfos
                }
                else{
                    requireActivity().finish()
                }
            }
        }

        binding.refreshButton.setOnClickListener(){
            manageSensorRecordViewModel.changeInitPageDate(dayFormat.format(System.currentTimeMillis()))
            recordsForHourAdapter.refresh()
        }

        // 날짜 변경
        binding.calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            manageSensorRecordViewModel.changeInitPageDate(
                year.toString() + "-" +
                        (month + 1).toString().padStart(2, '0') + "-" +
                        dayOfMonth.toString().padStart(2, '0')
            )
            recordsForHourAdapter.refresh()
        }

        binding.changePhoneViewPointImageButton.setOnClickListener(){

            when(curPhoneViewPoint){
                PhoneViewPoint.FRONT -> {
                    curPhoneViewPoint = PhoneViewPoint.BACK
                    it.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#D0FC5C"))
                }
                PhoneViewPoint.BACK -> {
                    curPhoneViewPoint = PhoneViewPoint.FRONT
                    it.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#495057"))
                }
            }
        }


        // 스크롤 할 때마다 중앙 View에 맞는 데이터를 불러와서 화면에 적용 (이전 coroutine job cancel 필수)
        binding.timeRecyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                when(newState){
                    // 스크롤이 멈췄을 때에만 화면을 업데이트
                    RecyclerView.SCROLL_STATE_IDLE -> {
                        recyclerView.layoutManager?.let{ layoutManager ->
                            val centerView = snapHelper.findSnapView(layoutManager)
                            centerView?.let{
                                val centerPosition = layoutManager.getPosition(it)
                                centerModel = recordsForHourAdapter.getRecordsForHourModel(centerPosition)
                                binding.curRecordsForHourUIModel = centerModel // 시간에 따라 배경 변경하는 데 사용

                                // 실시간으로 화면에 기록을 보여주던 이전 coroutine job cancel 필수
                                curUIJob?.let{ job ->
                                    if(job.isActive) {
                                        job.cancel()
                                    }
                                }

                                if(centerModel.records.isEmpty()){
                                    // 이전에 보던 Pin, Phone 초기화
                                    val initPinPoint = getPinPoint(0)
                                    binding.glSurfaceView.changePinLocation(initPinPoint[0], initPinPoint[1], 0f)
                                    binding.glSurfaceView.changeAppIcon(null)
                                    binding.glSurfaceView.changeAppPlayingImage(null)
                                    binding.timeTextView.text = ""
                                    binding.appNameTextView.text = ""
                                    binding.glSurfaceView.changePhoneAngle(0f, 0f, 0f)
                                }

                                // 현재 시간일 경우만 refresh 버튼 활성화
                                val curTime = System.currentTimeMillis()
                                if(centerModel.date == dayFormat.format(curTime) && centerModel.hour.toInt() == hourFormat.format(curTime).toInt()){
                                    binding.refreshButton.isEnabled = true
                                    binding.refreshButton.setImageAlpha(0xFF)
                                }
                                else{
                                    binding.refreshButton.isEnabled = false
                                    binding.refreshButton.setImageAlpha(0x3F)
                                }

                                // clockView를 다 그릴 때까지 dialog를 띄움
                                if(clockViewLoadingDialog.isShowing.not()){
                                    clockViewLoadingDialog.show()
                                }

                                // 변경된 데이터(centerModel)를 clockView에 적용
                                binding.customClockView.setCurModel(centerModel)

                                // clockView를 다 그렸을 때 dialog를 지우고 다음 과정으로 넘어감
                                binding.customClockView.drawSuccessListener = object : DrawSuccessListener{
                                    override fun onDrawSuccess() {
                                        if(clockViewLoadingDialog.isShowing){
                                            clockViewLoadingDialog.cancel()
                                        }

                                        // 실시간으로 화면에 기록을 보여주던 이전 coroutine job cancel 필수 (한번 더 확인)
                                        curUIJob?.let{ job ->
                                            if(job.isActive) {
                                                job.cancel()
                                            }
                                        }

                                        // 변경된 데이터(centerModel)를 balanceView에 적용
                                        binding.customBalanceView.setCurModel(centerModel)

                                        // 실시간으로 화면에 기록을 보여주는 새로운 coroutine job launch
                                        curUIJob = runUIJobByRecordsForHour(centerModel, null)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        })
    }

    private fun forceScroll(initPageDate: String){
        val adapter = binding.timeRecyclerView.adapter as RecordsForHourAdapter
        val layoutManager = binding.timeRecyclerView.layoutManager as FocusedLayoutManager
        val curTimeMillis = System.currentTimeMillis()

        // pagingSource의 INIT_PAGE_DATE에 따라 스크롤 위치가 달라짐
        when(initPageDate){
            // 오늘 날짜인 경우 현재 시간으로 스크롤함
            dayFormat.format(curTimeMillis) -> {
                for(index in 0 until adapter.itemCount){
                    val curModel = adapter.getRecordsForHourModel(index)
                    if(curModel.hour.toInt() == hourFormat.format(curTimeMillis).toInt()
                        && curModel.date == initPageDate) {
                        // position 번째 아이템을 offset 위치에 오도록 스크롤
                        layoutManager.scrollToPositionWithOffset(index, binding.timeRecyclerView.width / 2)
                        binding.timeRecyclerView.smoothScrollBy(1, 0) // snapHelper가 스냅 액션을 트리거하도록 함
                        break
                    }
                }
            }
            // 다른 날짜인 경우 해당 날짜로 스크롤함
            else -> {
                for(index in 0 until adapter.itemCount){
                    val curModel = adapter.getRecordsForHourModel(index)
                    if(curModel.date == initPageDate){
                        // position 번째 아이템을 offset 위치에 오도록 스크롤
                        layoutManager.scrollToPositionWithOffset(index + 12, binding.timeRecyclerView.width / 2)
                        binding.timeRecyclerView.smoothScrollBy(1, 0) // snapHelper가 스냅 액션을 트리거하도록 함
                        break
                    }
                }
            }
        }
    }

    private fun getPlayingImage(appName: String): Bitmap? {
        for(appInfo in allAppInfos){
            if(appInfo.appName == appName){
                return appInfo.appPlayingImage
            }
        }
        return null
    }

    private fun getAppIcon(appName: String): Bitmap? {
        for(appInfo in allAppInfos){
            if(appInfo.appName == appName){
                return appInfo.appIcon
            }
        }
        return null
    }

    // startTime 이후의 RecordsForHourUIModel 내부 기록을 보여줌
    private fun runUIJobByRecordsForHour(recordsForHourUIModel: RecordsForHourUIModel, startTime: Date?): Job{
        // 새로운 coroutine job launch
        // UI 조정하는 작업은 IO thread에서 할 수 없음 (launch(Dispatchers.IO) 하면 앱 crash)
        return viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            // job이 변경되면 관련된 변수도 초기화해야 하므로 지역 변수로 둠
            var lastXAngle: Float? = null
            var lastZAngle: Float? = null
            var lastYAngle: Float? = null
            for(record in recordsForHourUIModel.records){
                // 시작 시간보다 이르면 화면에 표시하지 않고 넘어감
                if((startTime != null) && (startTime > timeFormat.parse(record.recordTime))){
                    continue
                }

                val curDate = timeFormat.parse(record.recordTime)
                val curMinute = minuteFormat.format(curDate).toInt()
                val curSecond = secondFormat.format(curDate).toInt()
                val curTotalSeconds = curMinute * 60 + curSecond

                // 시간을 가리키는 Pin 업데이트
                //binding.customPinView.setPin(curTotalSeconds, getAppIcon(record.runningAppName))
                val pinPoint = getPinPoint(curTotalSeconds)
                binding.glSurfaceView.changePinLocation(pinPoint[0], pinPoint[1], 0f)
                binding.glSurfaceView.changeAppIcon(getAppIcon(record.runningAppName))

                // 화면이 켜진 기록일 경우 실행 중이었던 앱 별 미리 설정해 둔 이미지를 띄움
                if(record.isScreenOn == true){
                    binding.glSurfaceView.changeAppPlayingImage(getPlayingImage(record.runningAppName))
                }
                else{
                    binding.glSurfaceView.changeAppPlayingImage(BitmapFactory.decodeResource(resources, R.drawable.phone_off))
                }
                binding.timeTextView.text = record.recordTime
                binding.appNameTextView.text = record.runningAppName

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
                    val diffXAngle = curXAngle - lastXAngle!!
                    val diffYAngle = curYAngle - lastYAngle!!
                    val diffZAngle = curZAngle - lastZAngle!!
                    for(n in 1..10){
                        val splitedXAngle = lastXAngle!! + diffXAngle / 10 * n
                        val splitedYAngle = lastYAngle!! + diffYAngle / 10 * n
                        val splitedZAngle = lastZAngle!! + diffZAngle / 10 * n
                        binding.glSurfaceView.changePhoneAngle(-splitedZAngle / 180f * 200f, splitedYAngle / 180f * 200f, splitedXAngle / 180f * 200f)
                        delay(10)
                    }
                }
                else{
                    binding.glSurfaceView.changePhoneAngle(-curZAngle / 180f * 200f, curYAngle / 180f * 200f, curXAngle / 180f * 200f)
                    delay(100)
                }

                lastXAngle = curXAngle
                lastYAngle = curYAngle
                lastZAngle = curZAngle
            }
        }
    }

    private fun getPinPoint(sec: Int): FloatArray{
        val insideRecF = RectF()
        val min = Math.min(binding.glSurfaceView.width, binding.glSurfaceView.height)
        val radius = (min - binding.glSurfaceView.paddingLeft - 90) / 2
        val arcStrokeWidth = 100f
        val insideRadius = radius - arcStrokeWidth.toInt() / 2

        val centerX = (binding.glSurfaceView.width.div(2)).toFloat()
        val centerY = (binding.glSurfaceView.height.div(2)).toFloat()

        insideRecF.apply {
            set(centerX - insideRadius, centerY - insideRadius, centerX + insideRadius, centerY + insideRadius)
        }

        val startAngle = -90f + 0.1f * sec
        val sweepAngle = 0.1f

        val arcPath = Path()
        arcPath.arcTo(insideRecF, startAngle, sweepAngle)

        // 호 경로의 절반에 있는 지점의 좌표를 찾음
        val pm = PathMeasure(arcPath, false)
        val point = FloatArray(2)
        pm.getPosTan(pm.getLength() * 0.5f, point, null) // distance 만큼 시작 지점을 이동한 후의 위치를 point 변수에 담음

        // opengl 내부 좌표에 맞게 변환
        point[0] = (point[0] - centerX) / (binding.glSurfaceView.width / 2)
        point[1] = -(point[1] - centerY) / (binding.glSurfaceView.height / 2)

        return point
    }

}