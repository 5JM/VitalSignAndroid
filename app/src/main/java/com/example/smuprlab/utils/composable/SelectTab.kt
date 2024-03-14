package com.example.smuprlab.utils.composable

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.smuprlab.R

@Composable
fun SelectorTab(
    selectedItemIndex: Int,
    items: List<String>,
    modifier: Modifier = Modifier,
    tabWidth: Dp = 100.dp,
    bounded: Boolean = false,
    shape: Shape = CircleShape,
    onClick: (Int) -> Unit,
    onClick2: ()->Unit
){
    val color = colorResource(id = R.color.tab_backgroud_blue)
    val indicatorOffset: Dp by animateDpAsState(
        targetValue = tabWidth * selectedItemIndex,
        animationSpec = tween(
            durationMillis = 300,
            easing = EaseOutCubic
        ),
    )

    val boundModifier = if(bounded) modifier
        .clip(shape)
        .border(
            BorderStroke(3.dp, colorResource(id = R.color.main_blue)),
            shape
        )
        .background(color)
        .height(40.dp)
//        .height(intrinsicSize = IntrinsicSize.Min)
    else
        modifier
            .clip(shape)
            .background(color)
            .height(40.dp)
//            .height(intrinsicSize = IntrinsicSize.Min)

    Box(
        modifier = boundModifier
    ) {
        SelectTabIndicator(
            indicatorWidth = tabWidth,
            indicatorOffset = indicatorOffset,
            indicatorColor = colorResource(id = R.color.main_blue),
            shape = shape
        )
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(shape)
                .fillMaxHeight(),
        ) {
            items.mapIndexed { index, text ->
                val isSelected = index == selectedItemIndex
                TabItem(
                    isSelected = isSelected,
                    onClick = {
                        onClick(index)
                        if(!isSelected) onClick2()
                    },
                    tabWidth = tabWidth,
                    text = text,
                    shape = shape
                )
            }
        }
    }
}

@Composable
private fun SelectTabIndicator(
    indicatorWidth: Dp,
    indicatorOffset: Dp,
    indicatorColor: Color,
    shape: Shape
    ){
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(indicatorWidth)
            .offset(x = indicatorOffset)
            .padding(4.dp)
            .clip(shape)
            .background(indicatorColor),
    )
}

@Composable
private fun TabItem(
    isSelected: Boolean,
    onClick: () -> Unit,
    tabWidth: Dp,
    text: String,
    shape: Shape
){
    var multiplier by remember { mutableStateOf(1f) }

    val tabTextColor: Color by animateColorAsState(
        targetValue = if (isSelected) {
            Color.White
        } else {
            colorResource(id = R.color.disalbed_gray)
        },
        animationSpec = tween(easing = LinearEasing),
    )
    Text(
        modifier = Modifier
            .clip(shape)
            .noRippleClickable { onClick() }
            .width(tabWidth)
            .padding(
                vertical = 8.dp,
                horizontal = 12.dp,
            ),

        text = text,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.SemiBold,
        color = tabTextColor,
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

//@Preview
//@Composable
//private fun TabPreview(){
//    val item = listOf<String>("얼굴", "손가락")
//    val (selected, setSelected) = remember {
//        mutableStateOf(0)
//    }
//    CameraSelectorTab(
//        selectedItemIndex = selected,
//        items = item ,
//        modifier = Modifier,
//        tabWidth = 100.dp,
//        onClick = setSelected,
//    )
//}