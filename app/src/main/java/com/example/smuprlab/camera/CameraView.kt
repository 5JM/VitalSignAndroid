package com.example.smuprlab.camera

import android.Manifest
import android.app.Activity
import android.content.Context
import android.graphics.Paint
import android.graphics.RectF
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.smuprlab.R
import com.example.smuprlab.camera.core.FaceAnalyzer
import com.example.smuprlab.camera.core.FaceRecognitionScreenContent
import com.example.smuprlab.camera.core.MeasureView
import com.example.smuprlab.utils.HandleRequest
import com.example.smuprlab.utils.PermissionDeniedContent
import com.example.smuprlab.utils.createCornersPath
import com.example.smuprlab.utils.diaToLevelWithValue
import com.example.smuprlab.utils.sysToLevelWithValue
import com.example.smuprlab.viewmodel.ViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

private val graphHeightRatio = 0.8f // 1에 가까울수록 원본 크기랑 같아짐
private val graphBottomMargin = 1.1f // 커질수록 아래에서 더 떨어짐

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraView(
    viewModel: ViewModel,
    faceAnalyzer: FaceAnalyzer
){
    val animateSpec: TweenSpec<Float> = tween(
        durationMillis = 400,
        delayMillis = 0,
//        easing = EaseOutExpo
    )
    var animationTrigger by remember { mutableStateOf(false) }
    val animateBox = getAnimationBox( viewModel.sharedFaceBoxState.value, animateSpec, animationTrigger)

    val signal = viewModel.sharedSignal.value

    val paint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.Green.toArgb()
        strokeWidth = 10f
    }

    LaunchedEffect(key1 = true){
        animationTrigger = true
    }

    Box(modifier = Modifier.fillMaxSize()){
        // for camera permission
        RequestPermissions(
            context = LocalContext.current,
            permissions = listOf(Manifest.permission.CAMERA),
            func = {
                FaceRecognitionScreenContent(faceAnalyzer)
            },
            viewModel = viewModel
        )

        if(viewModel.sharedPermissionState.collectAsState().value){

            MeasureView(
                modifier = Modifier.fillMaxSize(),
                viewModel = viewModel,
//                activity = activity
            )

            Canvas(modifier = Modifier.fillMaxSize()){
                drawContext.canvas.nativeCanvas.drawPath(
                    createCornersPath(
                        animateBox.left,
                        animateBox.top,
                        animateBox.right,
                        animateBox.bottom,
                        50f,
                        50f
                    ),
                    Paint().apply {
                        style = Paint.Style.STROKE
                        color = Color.White.toArgb()
                        strokeWidth = 15f
                        strokeCap = Paint.Cap.ROUND
                    }
                )
            } // draw face box - end

            Canvas(
                modifier = Modifier
                    .fillMaxWidth(0.3f)
                    .fillMaxHeight(0.2f)
                    .background(Color.Black.copy(alpha = 0.7f))
                    .align(Alignment.TopEnd)
            ){
                if(signal.size > 2){
                    val graphH = this.size.height * graphHeightRatio
                    val stride = this.size.width / signal.size
                    var p1X = 0.0f
                    var p1Y = this.size.height - graphH * graphBottomMargin + signal[0] * graphH

                    for (i in 1 until signal.size) {
                        val p2X = (i * stride).toFloat()
                        val p2Y = this.size.height - graphH * graphBottomMargin + signal[i] * graphH

                        drawContext.canvas.nativeCanvas.drawLine(p1X, p1Y, p2X, p2Y, paint)

                        p1X = p2X
                        p1Y = p2Y
                    }
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.TopStart),
                verticalArrangement = Arrangement.SpaceAround
            ) {

//                Slider(
//                    enabled = viewModel.exposureEnable.collectAsState().value,
//                    value = viewModel.exposureIndex.collectAsState().value.toFloat(),
//                    valueRange = viewModel.exposureMin.collectAsState().value.toFloat()..viewModel.exposureMax.collectAsState().value.toFloat(),
//                    onValueChange = {viewModel.updateExposureIndex(it.toInt())},
//                )

                Text(
                    text = "BPM: ${viewModel.sharedBpm.collectAsState().value}",
                    color = Color.White,
                )

                Text(
                    text = "FPS: ${viewModel.sharedFps.collectAsState().value}",
                    color = Color.White,
                )

                Text(
                    text = "Confidence: ${viewModel.sharedConfidence.collectAsState().value}",
                    color = Color.White,
                )

                Text(
                    text = "HrvConfidence: ${viewModel.sharedHrvConfidence.collectAsState().value}",
                    color = Color.White,
                )

                Text(
                    text = "stress: ${viewModel.sharedStress.collectAsState().value}",
                    color = Color.White,
                )

                Text(
                    text = "SBP: ${sysToLevelWithValue(viewModel.sharedSBP.collectAsState().value.toInt())}, " +
                            "DBP: ${diaToLevelWithValue(viewModel.sharedDBP.collectAsState().value.toInt())}",
                    color = Color.White,
                )


                Text(
                    text = "VLF: ${viewModel.sharedVLF.collectAsState().value}",
                    color = Color.White,
                )

                Text(
                    text = "LF: ${viewModel.sharedLF.collectAsState().value}",
                    color = Color.White,
                )

                Text(
                    text = "HF: ${viewModel.sharedHF.collectAsState().value}",
                    color = Color.White,
                )

//                Text(
//                    text = "Whole Signal: ${viewModel.sharedWholeSignal.value.size}",
//                    color = Color.White,
//                )

                Text(
                    text = "Gender: ${viewModel.sharedGender.collectAsState().value.SoftmaxForGenderProbability()}(${viewModel.sharedGender.collectAsState().value}%)",
                    color = Color.White,
                )

                Text(
                    text = "Age: ${viewModel.sharedAge.collectAsState().value}",
                    color = Color.White,
                )
            }
        }
    }
}

private fun Float.SoftmaxForGenderProbability(): String =
    if(this == 0f) "-"
    else if (this >= 0.5f) "Male"
    else "Female"

@ExperimentalPermissionsApi
@Composable
private fun RequestPermissions(
    context: Context,
    permissions: List<String>,
    rationalMsg: String = stringResource(R.string.rational_msg),
    func: @Composable () -> Unit,
    viewModel: ViewModel
){
    val permissionState = rememberMultiplePermissionsState(permissions = permissions)

    HandleRequest(
        multiplePermissionState = permissionState,

        deniedContent = { shouldShowRationale ->
            PermissionDeniedContent(
                context = LocalContext.current,
                permissionState = permissionState,
                rationaleMessage = rationalMsg,
                shouldShowRationale = shouldShowRationale,
                onRequestPermission = { permissionState.launchMultiplePermissionRequest() },
                deniedPermission = {
//                    killApp(context as Activity)
                    (context as Activity).finish()
               },
            )
        },

        content = { func() },
        viewModel = viewModel
    )
}

@Composable
private fun getAnimationBox(box: RectF, animateSpec: TweenSpec<Float>, animationTrigger: Boolean): RectF {

    val animatedLeft by animateFloatAsState(
        targetValue = if(animationTrigger) box.left else 0f,
        animationSpec = animateSpec
    )
    val animatedRight by animateFloatAsState(
        targetValue = if(animationTrigger) box.right else 0f,
        animationSpec = animateSpec
    )
    val animatedTop by animateFloatAsState(
        targetValue = if(animationTrigger) box.top else 0f,
        animationSpec = animateSpec
    )
    val animatedBottom by animateFloatAsState(
        targetValue = if(animationTrigger) box.bottom else 0f,
        animationSpec = animateSpec
    )

    return RectF(animatedLeft, animatedTop, animatedRight, animatedBottom)
}