package com.choi.sensorproject.ui.setting

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.choi.sensorproject.ui.model.AppInfoUIModel
import com.choi.sensorproject.ui.viewmodel.ManageAppInfoViewModel
import com.example.sensorproject.R
import com.example.sensorproject.databinding.FragmentSettingBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingFragment: Fragment() {

    private var _binding: FragmentSettingBinding? = null

    private val binding
        get() = checkNotNull(_binding) { "binding was accessed outside of view lifecycle" }

    private val manageAppInfoViewModel: ManageAppInfoViewModel by viewModels()

    private var curBitmap: Bitmap? = null

    // registerForActivityResult는 Fragment의 전역 부분에 선언되어야 한다. (Activity가 Create 될 때 Callback이 정해져야 하기 때문)

    // 갤러리에서 이미지를 가져오기 위해 필요 (Gallery --[uri]-> this App --[uri]-> Data Storage --[src]-> this App)
    val galleryResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        if (result.resultCode == Activity.RESULT_OK){
            // Uri: 특정 리소스로 접근할 수 있는 경로
            result.data?.data?.let{ uri ->
                curBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
                    // ContentResolver는 Uri를 이용해서 ContentProvider에 연결하여 저장소 데이터에 접근해 CRUD를 할 수 있음
                    // this App <-> ContentResolver <-> ContentProvider <-> Data Storage
                    ImageDecoder.decodeBitmap(
                        ImageDecoder.createSource(
                            requireContext().contentResolver, uri
                        )
                    )
                }
                else{
                    MediaStore.Images.Media.getBitmap(
                        requireContext().contentResolver, uri
                    )
                }
            }
        }
    }

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
            getImageFromGallery()
            curBitmap?.let{
                val sampleAppInfo = AppInfoUIModel("com.sec.android.app.launcher", it)
                viewLifecycleOwner.lifecycleScope.launch{
                    manageAppInfoViewModel.insertAppInfo(sampleAppInfo)
                }
            }
            curBitmap = null
            findNavController().navigate(R.id.action_settingFragment_to_sensorRecordFragment)
        }
    }

    private fun getImageFromGallery(){
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NO_HISTORY
        galleryResultLauncher.launch(intent)
    }
}