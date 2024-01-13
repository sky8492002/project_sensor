package com.choi.sensorproject.ui.showrecord

import android.annotation.SuppressLint
import android.graphics.Bitmap
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.choi.sensorproject.ui.model.AppInfoUIModel
import com.choi.sensorproject.ui.model.RecordsForHourUIModel
import com.choi.sensorproject.ui.model.SensorRecordUIModel
import com.choi.sensorproject.ui.recyclerview.FocusedLayoutManager
import com.choi.sensorproject.ui.recyclerview.RecordsForHourAdapter
import com.choi.sensorproject.ui.viewmodel.AppInfoUIState
import com.choi.sensorproject.ui.viewmodel.ManageAppInfoViewModel
import com.choi.sensorproject.ui.viewmodel.ManageSensorRecordViewModel
import com.choi.sensorproject.ui.viewmodel.SensorRecordUIState
import com.example.sensorproject.databinding.FragmentShowRecordBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date


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

    private lateinit var loadingDialog: LoadingDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShowRecordBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            manageSensorRecordViewModel.uiState.collect(){ uiState ->
                var timeRecyclerViewListener : View.OnLayoutChangeListener? = null
                if (uiState is SensorRecordUIState.Success) {
                    recordsForHourAdapter.submitData(uiState.records) // submitData 내부에서 PagingData를 collect
                    // 처음 받아오는 경우(오늘 날짜의 데이터) 현재 시간에 대한 데이터로 스크롤함
                    if(recordsForHourAdapter.itemCount == 0){
                        val curTime = System.currentTimeMillis()
                        // 첫 데이터가 들어오는 순간보다 스크롤이 빠를 수 있으므로 상태 변경을 확인할 수 있는 listener를 설정
                        timeRecyclerViewListener = View.OnLayoutChangeListener{ _, _, _, _, _, _, _, _, _ ->
                            binding.timeRecyclerView.smoothScrollToPosition(hourFormat.format(curTime).toInt() + 2)
                            // 첫 번째 이후 강제로 스크롤하지 않음
                            binding.timeRecyclerView.removeOnLayoutChangeListener(timeRecyclerViewListener)
                        }
                        binding.timeRecyclerView.addOnLayoutChangeListener(timeRecyclerViewListener)
                    }
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
            recordsForHourAdapter.refresh()
        }

        loadingDialog = LoadingDialog(requireContext())

        // 스크롤 할 때마다 중앙 View에 맞는 데이터를 불러와서 화면에 적용 (이전 coroutine job cancel 필수)
        binding.timeRecyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                when(newState){
                    // 스크롤이 멈췄을 때에만 화면을 업데이트
                    RecyclerView.SCROLL_STATE_IDLE -> {
                        recyclerView.layoutManager?.let{ layoutManager ->
                            val centerView = snapHelper.findSnapView(layoutManager)!!
                            val centerPosition = layoutManager.getPosition(centerView)
                            centerModel = recordsForHourAdapter.getRecordsForHourModel(centerPosition)
                            binding.curRecordsForHourUIModel = centerModel // 시간에 따라 배경 변경하는 데 사용

                            // 현재 시간일 경우만 refresh 버튼 활성화
                            val curTime = System.currentTimeMillis()
                            if(centerModel.date == dayFormat.format(curTime) && centerModel.hour == hourFormat.format(curTime)){
                                binding.refreshButton.isEnabled = true
                                binding.refreshButton.setImageAlpha(0xFF)
                            }
                            else{
                                binding.refreshButton.isEnabled = false
                                binding.refreshButton.setImageAlpha(0x3F)
                            }

                            // clockView를 다 그릴 때까지 dialog를 띄움
                            loadingDialog.show()

                            // 변경된 데이터(centerModel)를 clockView에 적용
                            binding.customClockView.setCurModel(centerModel)

                            // 실시간으로 화면에 기록을 보여주던 이전 coroutine job cancel 필수
                            curUIJob?.let{ job ->
                                if(job.isActive) {
                                    job.cancel()
                                }
                            }

                            // clockView를 다 그렸을 때 dialog를 지우고 다음 과정으로 넘어감
                            binding.customClockView.drawSuccessListener = object : DrawSuccessListener{
                                override fun onDrawClockViewSuccess() {
                                    if(loadingDialog.isShowing){
                                        loadingDialog.cancel()
                                    }

                                    // 실시간으로 화면에 기록을 보여주던 이전 coroutine job cancel 필수 (한번 더 확인)
                                    curUIJob?.let{ job ->
                                        if(job.isActive) {
                                            job.cancel()
                                        }
                                    }

                                    // 실시간으로 화면에 기록을 보여주는 새로운 coroutine job launch
                                    curUIJob = runUIJobByRecordsForHour(centerModel, null)
                                }
                            }
                        }
                    }
                }
            }
        })
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
        return viewLifecycleOwner.lifecycleScope.launch {
            // job이 변경되면 관련된 변수도 초기화해야 하므로 지역 변수로 둠
            var lastxAngle: Float? = null
            var lastzAngle: Float? = null
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

                // 실행 중이었던 앱 별 미리 설정해 둔 이미지를 띄움 (없을 경우 기본 이미지)
                binding.glSurfaceView.changeAppPlayingImage(getPlayingImage(record.runningAppName))
                binding.timeTextView.text = record.recordTime
                binding.angleTextView.text = record.runningAppName

                // 실제 각도와 화면이 일치하게 조정 (이전 각도와 비교 후 10밀리 간격으로 미세조정)
                if(lastxAngle != null && lastzAngle != null){
                    val diffxAngle = record.xAngle - lastxAngle!!
                    val diffzAngle = record.zAngle - lastzAngle!!
                    for(n in 1..10){
                        val xAngle = lastxAngle!! + diffxAngle / 10 * n
                        val zAngle = lastzAngle!! + diffzAngle / 10 * n
                        binding.glSurfaceView.changePhoneAngle(-zAngle / 180f * 250f, 0f, xAngle / 180f * 250f)
                        delay(10)
                    }
                }
                else{
                    binding.glSurfaceView.changePhoneAngle(-record.zAngle / 180f * 250f, 0f, record.xAngle / 180f * 250f)
                    delay(100)
                }

                lastxAngle = record.xAngle
                lastzAngle = record.zAngle
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