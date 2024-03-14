package com.example.smuprlab.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.shouldShowRationale
import com.example.smuprlab.viewmodel.ViewModel


@ExperimentalPermissionsApi
@Composable
fun HandleRequest(
    multiplePermissionState: MultiplePermissionsState, //PermissionState,
    deniedContent: @Composable (Boolean) -> Unit,
    content: @Composable () -> Unit,
    viewModel: ViewModel
){
    var shouldShowRationale by remember { mutableStateOf(false) }
    val result = multiplePermissionState.permissions.all {
        shouldShowRationale = it.status.shouldShowRationale
        it.status == PermissionStatus.Granted
    }

    viewModel.updatePermissionState(result)

    if (result) content() else deniedContent(shouldShowRationale)
}

@ExperimentalPermissionsApi
@Composable
fun PermissionDeniedContent(
    context: Context,
    permissionState: MultiplePermissionsState, //PermissionState,
    rationaleMessage: String,
    shouldShowRationale: Boolean,
    onRequestPermission: () -> Unit,
    deniedPermission: () -> Unit
) {
    var backPressedTime by remember{ mutableStateOf(0L) }

    BackHandler() {
        if (System.currentTimeMillis() > backPressedTime + 2000){
            backPressedTime = System.currentTimeMillis()
            Toast.makeText(context as Activity, "\'뒤로\' 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT)
                .show()
        }else {
            killApp(context as Activity)
        }
    }


    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray)
        ,
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.Center
    ) {
        if (shouldShowRationale) {
            AlertDialog(
                onDismissRequest = {},

                title = {
                    Text(
                        text = "권한 요청",
                        style = TextStyle(
                            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                text = {
                    Text(rationaleMessage)
                },
                confirmButton = {
                    Button(onClick = { onRequestPermission() }) {
                        Text("동의")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { deniedPermission() }
                    ) {
                        Text("취소")
                    }
                }
            )
        } else {
            // 권한 재요청 -> 수락
            LaunchedEffect(true){
                permissionState.launchMultiplePermissionRequest()
            }
            // 권한 재요청 -> 거절
            Card(
                modifier = Modifier
                    .padding(vertical = 55.dp)
                    .fillMaxWidth(0.4f)
                    .height(200.dp)
//                    .shadow(
//                        elevation = 15.dp,
//                        ambientColor = Color.White,
//                        spotColor = Color.White,
//                        shape = RoundedCornerShape(20.dp)
//                    )
                ,
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(20.dp),
            ){
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.5f))

                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                    ,
                    text = "권한 요청 알람이 표시되지 않았다면\n\n[설정] 버튼을 누르고 권한을 허용한 후,\n\n앱을 다시 시작해주세요.",
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        fontSize = MaterialTheme.typography.labelLarge.fontSize,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.5f))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                    ,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ){
                    // denied button
                    Button(
                        colors = ButtonDefaults.buttonColors(Color.White),
                        onClick = { run { killApp(context as Activity) } }) {
                        Text(text = "종료")
                    }

                    // confirm button
                    Button(
                        modifier = Modifier
                            .shadow(
                                elevation = 4.dp,
                                ambientColor = Color.DarkGray,
                                spotColor = Color.White,
                                shape = RoundedCornerShape(20.dp)
                            ),
                        colors = ButtonDefaults.buttonColors(Color.Gray),
                        onClick = {
                            val intent = Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.parse("package:"+context.packageName)
                            )

                            if(context.packageManager != null){
                                context.startActivity(intent)
                            }
                        }) {
                        Text("설정", color = Color.White)
                    }
                }
            }
        }
    }
}