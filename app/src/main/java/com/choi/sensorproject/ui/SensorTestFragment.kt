package com.choi.sensorproject.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.sensorproject.databinding.FragmentSensorTestBinding

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

}