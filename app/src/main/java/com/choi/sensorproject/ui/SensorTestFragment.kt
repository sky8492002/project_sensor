package com.choi.sensorproject.ui

import android.graphics.PixelFormat
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.sensorproject.R
import com.example.sensorproject.databinding.FragmentSensorTestBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SensorTestFragment: Fragment() {
    private var _binding: FragmentSensorTestBinding? = null

    private val binding
        get() = checkNotNull(_binding) { "binding was accessed outside of view lifecycle" }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSensorTestBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()

        val testAngleQueue: ArrayDeque<Triple<Float, Float, Float>> = ArrayDeque()

        testAngleQueue.addLast(Triple(50f, 0f, 0f))
        testAngleQueue.addLast(Triple(50f, 10f, 0f))
        testAngleQueue.addLast(Triple(50f, 20f, 0f))
        testAngleQueue.addLast(Triple(50f, 30f, 0f))
        testAngleQueue.addLast(Triple(50f, 40f, 0f))
        testAngleQueue.addLast(Triple(50f, 50f, 0f))

        testAngleQueue.addLast(Triple(50f, 50f, -20f))
        testAngleQueue.addLast(Triple(50f, 50f, -40f))
        testAngleQueue.addLast(Triple(50f, 50f, -60f))
        testAngleQueue.addLast(Triple(50f, 50f, -80f))
        testAngleQueue.addLast(Triple(50f, 50f, -100f))
        testAngleQueue.addLast(Triple(50f, 50f, -120f))
        testAngleQueue.addLast(Triple(50f, 50f, -140f))
        testAngleQueue.addLast(Triple(50f, 50f, -160f))
        testAngleQueue.addLast(Triple(50f, 50f, -180f))
        testAngleQueue.addLast(Triple(50f, 50f, -200f))

        testAngleQueue.addLast(Triple(50f, -30f, -20f))
        testAngleQueue.addLast(Triple(50f, -30f, -40f))
        testAngleQueue.addLast(Triple(50f, -30f, -60f))
        testAngleQueue.addLast(Triple(50f, -30f, -80f))
        testAngleQueue.addLast(Triple(50f, -30f, -100f))
        testAngleQueue.addLast(Triple(50f, -30f, -120f))
        testAngleQueue.addLast(Triple(50f, -30f, -140f))
        testAngleQueue.addLast(Triple(50f, -30f, -160f))
        testAngleQueue.addLast(Triple(50f, -30f, -180f))
        testAngleQueue.addLast(Triple(50f, -30f, -200f))


        val scope = GlobalScope // 비동기 함수 진행
        scope.launch {
            while (true) {
                delay(500)
                val cur = testAngleQueue.first()
                testAngleQueue.removeFirst()
                testAngleQueue.addLast(cur)
                val dx = cur.first
                val dy = cur.second
                val dz = cur.third
                binding.surfaceView.changeAngle(dx, dy, dz)
                binding.angleTextView.text = "각도: " + dx.toString() + ", " + dy.toString() + ", " + dz.toString()
            }
        }

    }

}