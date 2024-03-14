package com.example.smuprlab.utils.composable.progressbar

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.smuprlab.utils.composable.customFieldRoundCornerShape

@Composable
fun LinearProgress(
    modifier: Modifier,
    animationDuration: Int = 700,
    indicatorValue: Float = 0f,
    maxIndicatorValue: Float = 15000f, // millisecond, 1000 -> 1 sec, total 15 sec
    backgroundIndicatorColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
    foregroundIndicatorColor: Color = MaterialTheme.colorScheme.primary,
){
    var allowedIndicatorValue by remember {
        mutableStateOf(maxIndicatorValue)
    }
    allowedIndicatorValue = if (indicatorValue <= maxIndicatorValue) {
        indicatorValue
    } else {
        maxIndicatorValue
    }

    var animatedIndicatorValue by remember { mutableStateOf(0f) }

    val percentage = (animatedIndicatorValue / maxIndicatorValue)// * 100

    val receivedValue by animateFloatAsState(
        targetValue = percentage, //allowedIndicatorValue,
        animationSpec = tween(
            animationDuration,
            easing = LinearOutSlowInEasing
        )
    )

    LaunchedEffect(key1 = allowedIndicatorValue) {
        animatedIndicatorValue = allowedIndicatorValue
    }

    Column(
        modifier = modifier
//        Modifier
//            .fillMaxSize()
//            .padding(top = 100.dp, start = 30.dp, end = 30.dp)
    ) {
        // Progress Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(17.dp)
        ) {
            // for the background of the ProgressBar
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(customFieldRoundCornerShape())
                    .background(backgroundIndicatorColor)
            )
            // for the progress of the ProgressBar
            Box(
                modifier = Modifier
                    .fillMaxWidth(receivedValue)
                    .fillMaxHeight()
                    .clip(customFieldRoundCornerShape())
                    .background(foregroundIndicatorColor)
                    .animateContentSize()
            )
        }
    }
}