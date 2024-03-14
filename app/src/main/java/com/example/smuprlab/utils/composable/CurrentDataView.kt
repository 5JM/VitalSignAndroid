package com.example.smuprlab.utils.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.smuprlab.R
import com.example.smuprlab.data.HealthData
import com.example.smuprlab.utils.diaToLevelWithValue
import com.example.smuprlab.utils.sysToLevelWithValue
import kotlinx.coroutines.launch

@Composable
fun TableRow(
    backgroundColor: Color,
    textColor: Color,
    firstText: String,
    secondText: String,
    isTitle: Boolean = false
){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(customModalRoundCornerShape())
            .background(backgroundColor),
        horizontalArrangement = Arrangement.SpaceEvenly
    ){
        var multiplier by remember { mutableStateOf(1f) }

        Text(
            text = firstText,
            textAlign = TextAlign.Center,
            fontWeight = if(isTitle) FontWeight.SemiBold else FontWeight.Normal,
            color = textColor,
            modifier = Modifier
                .padding(vertical = 10.dp)
                .fillMaxWidth(0.5f)
                .height(20.dp),
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

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = secondText,
            textAlign = TextAlign.Center,
            fontWeight = if(isTitle) FontWeight.SemiBold else FontWeight.Normal,
            color = textColor,
            modifier = Modifier
                .padding(vertical = 10.dp)
                .fillMaxWidth()
                .height(20.dp),
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
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color.LightGray)
    )
}