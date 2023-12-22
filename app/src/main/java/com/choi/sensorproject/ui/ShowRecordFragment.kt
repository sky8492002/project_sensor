package com.choi.sensorproject.ui

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
import com.choi.sensorproject.ui.recyclerview.FocusedLayoutManager
import com.choi.sensorproject.ui.recyclerview.RecordsForHourAdapter
import com.example.sensorproject.databinding.FragmentShowRecordBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ShowRecordFragment: Fragment() {
    private var _binding: FragmentShowRecordBinding? = null

    private val binding
        get() = checkNotNull(_binding) { "binding was accessed outside of view lifecycle" }

    private val showRecordViewModel: ShowRecordViewModel by viewModels()

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
        binding.showRecordViewModel = showRecordViewModel

        viewLifecycleOwner.lifecycleScope.launch {
            showRecordViewModel.uiState.collect(){ uiState ->
                if (uiState is ShowRecordUIState.Success) {
                    recordsForHourAdapter.submitData(uiState.records)
                }
            }
        }

        var lastJob: Job? = null
        // 스크롤 할 때마다 중앙 View에 맞는 데이터를 불러와서 화면에 적용 (이전 coroutine job cancel 필수)
        binding.timeRecyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                recyclerView.layoutManager?.let{ layoutManager ->
                    val centerView = snapHelper.findSnapView(layoutManager)!!
                    val position = layoutManager.getPosition(centerView)
                    val centerModel = recordsForHourAdapter.getRecordsForHourModel(position)

                    // 이전 coroutine job cancel 필수
                    lastJob?.let{ job ->
                        if(job.isActive) {
                            job.cancel()
                            binding.surfaceView.changeAngle(50f, 0f, 0f)
                        }
                    }

                    // 새로운 coroutine job launch
                    lastJob = viewLifecycleOwner.lifecycleScope.launch {
                        for(record in centerModel.records){
                            // 실제 각도와 화면이 일치하게 조정
                            binding.surfaceView.changeAngle(50f, -record.zrAngle, record.xrAngle*2)
                            binding.timeTextView.text = record.recordTime
                            binding.angleTextView.text =
                                "각도: " + 50f.toString() + ", " + record.zrAngle.toString() + ", " + (record.xrAngle * 2).toString()
                            delay(100)
                        }
                    }
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()

//        val testAngleQueue: ArrayDeque<Triple<Float, Float, Float>> = ArrayDeque()
//
//        testAngleQueue.addLast(Triple(50f, 0f, 0f))
//        testAngleQueue.addLast(Triple(50f, 10f, 0f))
//        testAngleQueue.addLast(Triple(50f, 20f, 0f))
//        testAngleQueue.addLast(Triple(50f, 30f, 0f))
//        testAngleQueue.addLast(Triple(50f, 40f, 0f))
//        testAngleQueue.addLast(Triple(50f, 50f, 0f))
//
//        testAngleQueue.addLast(Triple(50f, 50f, -20f))
//        testAngleQueue.addLast(Triple(50f, 50f, -40f))
//        testAngleQueue.addLast(Triple(50f, 50f, -60f))
//        testAngleQueue.addLast(Triple(50f, 50f, -80f))
//        testAngleQueue.addLast(Triple(50f, 50f, -100f))
//        testAngleQueue.addLast(Triple(50f, 50f,  20f))
//        testAngleQueue.addLast(Triple(50f, 50f,  40f))
//        testAngleQueue.addLast(Triple(50f, 50f,  60f))
//        testAngleQueue.addLast(Triple(50f, 50f,  80f))
//        testAngleQueue.addLast(Triple(50f, 50f,  100f))
//
//        testAngleQueue.addLast(Triple(50f, -30f, -20f))
//        testAngleQueue.addLast(Triple(50f, -30f, -40f))
//        testAngleQueue.addLast(Triple(50f, -30f, -60f))
//        testAngleQueue.addLast(Triple(50f, -30f, -80f))
//        testAngleQueue.addLast(Triple(50f, -30f, -100f))
//        testAngleQueue.addLast(Triple(50f, -30f, -120f))
//        testAngleQueue.addLast(Triple(50f, -30f, -140f))
//        testAngleQueue.addLast(Triple(50f, -30f, -160f))
//        testAngleQueue.addLast(Triple(50f, -30f, -180f))
//        testAngleQueue.addLast(Triple(50f, -30f, -200f))
//
//
//        val scope = GlobalScope // 비동기 함수 진행
//        scope.launch {
//            while (true) {
//                delay(500)
//                val cur = testAngleQueue.first()
//                testAngleQueue.removeFirst()
//                testAngleQueue.addLast(cur)
//                val dx = cur.first
//                val dy = cur.second
//                val dz = cur.third
//                binding.surfaceView.changeAngle(dx, dy, dz)
//                binding.angleTextView.text = "각도: " + dx.toString() + ", " + dy.toString() + ", " + dz.toString()
//            }
//        }

    }

}