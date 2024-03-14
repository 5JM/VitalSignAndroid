package com.example.smuprlab.utils.composable

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.delay

enum class ButtonState {
    Pressed,
    Idle
}

fun Modifier.bounceClick(strength: Float = 0.97f) = composed{
    var buttonState by remember { mutableStateOf(ButtonState.Idle) }
    val scale by animateFloatAsState(if (buttonState == ButtonState.Pressed) strength else 1f)
//    val color by animateColorAsState(if (buttonState == ButtonState.Pressed) Color.LightGray else Color.Red)

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = {},
        )
        .pointerInput(buttonState) {
            awaitPointerEventScope {
                buttonState = if (buttonState == ButtonState.Idle) {
                    awaitFirstDown(false)
//                    waitForUpOrCancellation()
                    ButtonState.Pressed
                }
                else {
                    waitForUpOrCancellation()
//                    awaitFirstDown(false)
                    ButtonState.Idle
                }
            }
        }
}

inline fun Modifier.noRippleClickable(crossinline onClick: ()->Unit): Modifier = composed {
    clickable(
        indication = null,
        interactionSource = remember { MutableInteractionSource() }
    ) {
        onClick()
    }
}

fun Modifier.clickableOnce(onClick: () -> Unit): Modifier = composed(
    inspectorInfo = {
        name = "clickableOnce"
        value = onClick
    }
) {
    var enableAgain by remember { mutableStateOf(true) }
    LaunchedEffect(enableAgain, block = {
        if (enableAgain) return@LaunchedEffect
        delay(timeMillis = 500L)
        enableAgain = true
    })
    clickable(
        indication = null,
        interactionSource = remember { MutableInteractionSource() }
    ){
        if (enableAgain) {
            enableAgain = false
            onClick()
        }
    }

//    Modifier.clickable {
//        if (enableAgain) {
//            enableAgain = false
//            onClick()
//        }
//    }
}