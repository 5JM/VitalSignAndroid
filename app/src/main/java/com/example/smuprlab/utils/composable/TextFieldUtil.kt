package com.example.smuprlab.utils.composable

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.example.smuprlab.R

@Composable
fun customFieldColor() =
    TextFieldDefaults.colors(
        cursorColor = colorResource(id = R.color.main_blue),

        focusedTextColor = Color.Black,
        focusedLabelColor = colorResource(id = R.color.main_blue),
        focusedContainerColor = Color.White,
        focusedIndicatorColor = colorResource(id = R.color.main_blue),

        errorContainerColor = Color.White,
        errorSupportingTextColor = Color.Red,
        errorCursorColor = Color.Red,
        errorIndicatorColor = Color.Red,
        errorTextColor = colorResource(id = R.color.disalbed_gray),

        unfocusedIndicatorColor = Color.White,
        unfocusedContainerColor = Color.White,

        disabledContainerColor = Color.White,
        disabledTextColor = colorResource(id = R.color.disalbed_gray),
        disabledIndicatorColor = Color.White,
    )
@Composable
fun customButtonColor() =
    ButtonDefaults.buttonColors(
        containerColor= colorResource(id = R.color.main_blue),
        disabledContainerColor = colorResource(id = R.color.disalbed_gray)
    )
fun customFieldRoundCornerShape() = RoundedCornerShape(10.dp)

fun customModalRoundCornerShape() = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)