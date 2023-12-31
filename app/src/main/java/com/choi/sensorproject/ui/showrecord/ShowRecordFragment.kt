package com.choi.sensorproject.ui.showrecord

import android.annotation.SuppressLint
import android.graphics.Bitmap
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

@SuppressLint("SimpleDateFormat")
@AndroidEntryPoint
class ShowRecordFragment: Fragment() {
    private var _binding: FragmentShowRecordBinding? = null

    private val binding
        get() = checkNotNull(_binding) { "binding was accessed outside of view lifecycle" }

    private val manageSensorRecordViewModel: ManageSensorRecordViewModel by viewModels()
    private val manageAppInfoViewModel: ManageAppInfoViewModel by viewModels()

    private var allAppInfos: List<AppInfoUIModel> = mutableListOf()

    val dayFormat = SimpleDateFormat("yyyy-MM-dd")
    val hourFormat = SimpleDateFormat("HH")

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

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            manageSensorRecordViewModel.uiState.collect(){ uiState ->
                if (uiState is SensorRecordUIState.Success) {
                    recordsForHourAdapter.submitData(uiState.records)
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

        var curJob: Job? = null
        // 스크롤 할 때마다 중앙 View에 맞는 데이터를 불러와서 화면에 적용 (이전 coroutine job cancel 필수)
        binding.timeRecyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                recyclerView.layoutManager?.let{ layoutManager ->
                    val centerView = snapHelper.findSnapView(layoutManager)!!
                    val position = layoutManager.getPosition(centerView)
                    val centerModel = recordsForHourAdapter.getRecordsForHourModel(position)

                    // 현재 시간일 경우만 refresh 버튼 활성화
                    val curTime = System.currentTimeMillis()
                    if(centerModel.date == dayFormat.format(curTime) && centerModel.hour == hourFormat.format(curTime)){
                        binding.refreshButton.isEnabled = true
                    }
                    else{
                        binding.refreshButton.isEnabled = false
                    }

                    // 이전 coroutine job cancel 필수
                    curJob?.let{ job ->
                        if(job.isActive) {
                            job.cancel()
                        }
                    }

                    // 새로운 coroutine job launch
                    // UI 조정하는 작업은 IO thread에서 할 수 없음 (launch(Dispatchers.IO) 하면 앱 crash)
                    curJob = viewLifecycleOwner.lifecycleScope.launch {
                        for(record in centerModel.records){
                            // 실제 각도와 화면이 일치하게 조정
                            binding.surfaceView.changeAngle(50f, -record.zrAngle, record.xrAngle*2)
                            // 실행 중이었던 앱 별 미리 설정해 둔 이미지를 띄움 (없을 경우 기본 이미지)
                            binding.surfaceView.changeAppPlayingImage(getPlayingImage(record.runningAppName))
                            binding.timeTextView.text = record.recordTime
//                            binding.angleTextView.text =
//                                "각도: " + 50f.toString() + ", " + record.zrAngle.toString() + ", " + (record.xrAngle * 2).toString()
                            binding.angleTextView.text = record.runningAppName
                            delay(100)
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

}