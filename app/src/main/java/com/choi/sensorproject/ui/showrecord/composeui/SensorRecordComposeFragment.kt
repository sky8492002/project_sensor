package com.choi.sensorproject.ui.showrecord.composeui

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.choi.sensorproject.ui.opngl.CustomGLSurfaceView
import com.choi.sensorproject.ui.showrecord.CustomClockSurfaceView
import com.choi.sensorproject.ui.viewmodel.ManageAppInfoViewModel
import com.choi.sensorproject.ui.viewmodel.ManageSensorRecordViewModel
import com.example.sensorproject.R
import kotlinx.coroutines.flow.Flow

class SensorRecordComposeFragment: Fragment() {

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
        // hiltViewModel 대신 viewModel을 쓰면 App Crash

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center){
            Text(text = "기록 시간")
            Text(text = "앱 패키지명")

            Box{
                ClockView()
                OpenGLView()
            }

        }
    }

    @Composable
    fun ClockView(){
        // AndroidView를 사용하여 기존의 View를 호출할 수 있음
        AndroidView(
            factory = { context ->
                CustomClockSurfaceView(context)
            },
            modifier = Modifier.fillMaxWidth().height(400.dp),
            update = { view ->

            }
        )
    }

    @Composable
    fun OpenGLView(){
        // AndroidView를 사용하여 기존의 View를 호출할 수 있음
        AndroidView(
            factory = { context ->
                CustomGLSurfaceView(context)
            },
            modifier = Modifier.fillMaxWidth().height(400.dp),
            update = { view ->
            }
        )
    }

    @Composable
    fun PagingView(pagingDataFlow: Flow<PagingData<String>>) {
        val lazyPagingItems = pagingDataFlow.collectAsLazyPagingItems()

//        LazyColumn {
//            items(lazyPagingItems) { item ->
//                Text("Item: $item")
//            }
//        }

        // 로드 상태 관찰
        val loadState = lazyPagingItems.loadState
        when {
            loadState.refresh is LoadState.Loading -> {
                // 로딩 중일 때 UI 업데이트
            }
            loadState.refresh is LoadState.Error -> {
                // 에러 발생 시 UI 업데이트
            }
            loadState.refresh is LoadState.NotLoading -> {
                // 로딩 완료 시 UI 업데이트
            }
        }
    }

    @Composable
    fun LoadingDialog(){

        Dialog(
            onDismissRequest = {  },
            properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
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
            ){
                Image(
                    painter = painterResource(R.drawable.loading),
                    contentDescription = null
                )
                Text("Loading...")
            }
        }
    }
}