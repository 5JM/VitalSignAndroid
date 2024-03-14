package com.example.smuprlab.utils.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smuprlab.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomBottomSheet(
    openBottomSheet: MutableState<Boolean>,
    buttonsText: List<String>,
    buttonsColor: List<Color>,
    textColor: List<Color>,
    button1Func: () -> Unit,
    button2Func: () -> Unit,
    button3Func: () -> Unit,
    height: Float = 0.4f
){
    val skipPartiallyExpanded by remember { mutableStateOf(false) }

    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = skipPartiallyExpanded
    )
    val coroutineScope = rememberCoroutineScope()

    if(openBottomSheet.value) {
        ModalBottomSheet(
            onDismissRequest = { openBottomSheet.value = false },
            sheetState = bottomSheetState,
            shape = customModalRoundCornerShape(),
            containerColor = colorResource(id = R.color.background_light_gray),
            dragHandle = {},
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(height)
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(start = 15.dp, end = 15.dp, top = 40.dp, bottom = 40.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        coroutineScope.launch { bottomSheetState.hide() }.invokeOnCompletion {
                            if (!bottomSheetState.isVisible) {
                                openBottomSheet.value = false
                                button1Func()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor= buttonsColor[0],
                        disabledContainerColor = colorResource(id = R.color.disalbed_gray)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 2.dp
                    ),
                    shape = customFieldRoundCornerShape(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) { Text(text = buttonsText[0], fontSize = 20.sp, color = textColor[0]) }

                Button(
                    onClick = {
                        coroutineScope.launch { bottomSheetState.hide() }.invokeOnCompletion {
                            if (!bottomSheetState.isVisible) {
                                openBottomSheet.value = false
                                button2Func()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor= buttonsColor[1],
                        disabledContainerColor = colorResource(id = R.color.disalbed_gray)
                    ),
                    shape = customFieldRoundCornerShape(),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 2.dp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) { Text(text = buttonsText[1], fontSize = 20.sp, color = textColor[1]) }

                Button(
                    onClick = {
                        coroutineScope.launch { bottomSheetState.hide() }.invokeOnCompletion {
                            if (!bottomSheetState.isVisible) {
                                openBottomSheet.value = false
                                button3Func()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor= buttonsColor[2],
                        disabledContainerColor = colorResource(id = R.color.disalbed_gray)
                    ),
                    shape = customFieldRoundCornerShape(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) { Text(text = buttonsText[2], fontSize = 20.sp, color = textColor[2]) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomBottomSheet(
    openBottomSheet: MutableState<Boolean>,
    titleContent:  @Composable () -> Unit,
    bodyContent: @Composable () -> Unit,
    buttonText:  String,
    buttonFunc: () -> Unit
){
    val skipPartiallyExpanded by remember { mutableStateOf(false) }

    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = skipPartiallyExpanded
    )
    val coroutineScope = rememberCoroutineScope()

    if(openBottomSheet.value) {
        ModalBottomSheet(
            onDismissRequest = {
                openBottomSheet.value = false
                buttonFunc()
            },
            sheetState = bottomSheetState,
            shape = customModalRoundCornerShape(),
            containerColor = colorResource(id = R.color.background_light_gray),
            dragHandle = {},
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max)
//                .fillMaxHeight() //0.3f
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(start = 15.dp, end = 15.dp, bottom = 40.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(){
                    Spacer(modifier = Modifier.height(17.dp))

                    titleContent()

                    Spacer(modifier = Modifier.height(20.dp))

                    Spacer(
                        modifier = Modifier
                            .height(1.dp)
                            .fillMaxWidth()
                            .background(colorResource(id = R.color.disalbed_gray))
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    bodyContent()

                    Spacer(modifier = Modifier.height(10.dp))
                }

                Button(
                    onClick = {
                        coroutineScope.launch { bottomSheetState.hide() }.invokeOnCompletion {
                            if (!bottomSheetState.isVisible) {
                                openBottomSheet.value = false
                                buttonFunc()
                            }
                        }
                    },
                    colors = customButtonColor(),
                    shape = customFieldRoundCornerShape(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    var multiplier by remember { mutableStateOf(1f) }

                    Text(
                        text = buttonText,
//                        fontSize = 20.sp,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Visible,
                        style = LocalTextStyle.current.copy(
                            fontSize = LocalTextStyle.current.fontSize * multiplier
                        ),
                        onTextLayout = {
                            if (it.hasVisualOverflow) {
                                multiplier *= 0.99f
                            }
                        }
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun BottomSheetContentPreview(){
    Column(
        Modifier
            .fillMaxSize()
            .padding(start = 30.dp, end = 30.dp, top = 17.dp, bottom = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(17.dp))
        Text(text = "아이디 찾기", fontSize = 20.sp, color = Color.Black)

        Spacer(modifier = Modifier.height(10.dp))
        Spacer(
            modifier = Modifier
                .height(1.dp)
                .fillMaxWidth()
                .background(colorResource(id = R.color.disalbed_gray))
        )
        Spacer(modifier = Modifier.height(10.dp))

        Text(text = "로그인하기", fontSize = 20.sp, color = Color.Black)

        Button(
            // Note: If you provide logic outside of onDismissRequest to remove the sheet,
            // you must additionally handle intended state cleanup, if any.
            onClick = {},
            colors = customButtonColor(),
            shape = customFieldRoundCornerShape(),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(text = "로그인하기", fontSize = 20.sp, color = Color.White)
        }
    }
}

//@Preview
//@Composable
//private fun BottomSheetPreview(){
//    CustomBottomSheet(
//        remember{mutableStateOf(true)},
//        "아이디 찾기",
//        "로그인하기",
//        "로그인하기"
//    )
//}