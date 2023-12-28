package com.choi.sensorproject.ui.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.sensorproject.databinding.FragmentSettingBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingFragment: Fragment() {

    private var _binding: FragmentSettingBinding? = null

    private val binding
        get() = checkNotNull(_binding) { "binding was accessed outside of view lifecycle" }

    private val manageAppInfoViewModel: ManageAppInfoViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.imageTestButon.setOnClickListener(){

        }
    }
}