package com.example.smuprlab.camera.core

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.smuprlab.R
import com.example.smuprlab.utils.composable.customButtonColor
import com.example.smuprlab.utils.composable.customFieldRoundCornerShape
import com.example.smuprlab.viewmodel.ViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kr.smu.prlab.Prlab

@Composable
fun MeasureView(
    modifier: Modifier,
    viewModel : ViewModel,
){
    Box(modifier = modifier){
        // bottom button for measurement
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(horizontal = 20.dp)
                ,
                colors = customButtonColor(),
                shape = customFieldRoundCornerShape(),

                onClick = {
                    Prlab.instance.reset()

                    viewModel.apply {
                        reset()

                        isEstimating.value.apply {
                            updateEstimateState(!this)

                            updateEstimateGenderAge(!this)
                        }

                        if(isEstimating.value && startEstimateTime.value == 0L){
                            updateStartEstimateTime(System.currentTimeMillis())
                        }
                    }
                }
            ){
                if(!viewModel.isEstimating.collectAsState().value) {
                    Text(
                        text = "측정",
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Visible,
                    )
                }
                else
                    CircularProgressIndicator(
                        modifier = Modifier.fillMaxHeight(),
                        color = Color.White,
                        trackColor = colorResource(id = R.color.disalbed_gray),
                        strokeCap = StrokeCap.Round,
                        strokeWidth = 5.dp
                    )
            }

            Spacer(modifier = Modifier.height(50.dp))
        } // bottom button - end
    }
}