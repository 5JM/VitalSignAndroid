package com.example.smuprlab

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.smuprlab.camera.CameraView
import com.example.smuprlab.camera.core.FaceAnalyzer
import com.example.smuprlab.camera.core.FaceDetector
import com.example.smuprlab.ui.theme.SMUPrlabTheme
import com.example.smuprlab.utils.assetFilePath
import com.example.smuprlab.utils.composable.LoadingView
import com.example.smuprlab.utils.killApp
import com.example.smuprlab.viewmodel.ViewModel
import kr.smu.prlab.Prlab
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        // 얼굴 감지기 초기화
//        FaceDetector.instance.initFaceDetector(
//           this@MainActivity
//        )
        FaceDetector.instance.initFaceDetector(
            File(
                assetFilePath(this@MainActivity, "slim_320_without_postprocessing.onnx").toString()
            ).absolutePath
        )

        Prlab.instance.init(
            duration = 5f,
            hrvTime = 20,
            ageGenderModelPath = File(
                assetFilePath(this@MainActivity, "age_sex_pred_model.onnx").toString()
            ).absolutePath,
            bloodPressureModelPath = File(
                assetFilePath(this@MainActivity, "bp.onnx").toString()
            ).absolutePath
        )

        val viewModel by viewModels<ViewModel>()

        super.onCreate(savedInstanceState)
        setContent {
            var backPressedTime by remember{ mutableStateOf(0L) }

            val density = LocalDensity.current.density
            val configuration = LocalConfiguration.current

            val screenWidthPx = (configuration.screenWidthDp.dp * density).value
            val screeHeightPx = (configuration.screenHeightDp.dp * density).value

            val faceAnalyzer = remember{
                FaceAnalyzer(
                    viewModel,
                    screenWidthPx,
                    screeHeightPx
                )
            }


            SMUPrlabTheme {

                BackHandler(enabled = !viewModel.isEstimating.collectAsState().value) {
                    if (System.currentTimeMillis() > backPressedTime + 2000){
                        backPressedTime = System.currentTimeMillis()
                        Toast.makeText(this, "\'뒤로\' 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT)
                            .show()
                    }else {
                        killApp(this)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ){
                    CameraView(
                        viewModel = viewModel,
                        faceAnalyzer = faceAnalyzer
                    )
                }
            }
        }
    }
}