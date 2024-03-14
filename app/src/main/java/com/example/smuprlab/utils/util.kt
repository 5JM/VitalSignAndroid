package com.example.smuprlab.utils

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Path
import android.graphics.RectF
import android.util.Log
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

// 혈압
// 수축기 SYS
// 정상: ~ 120, 고혈압 전단계: 121 ~ 139, 1단계: 140 ~ 159, 2단계: 160 ~ 179, 고혈압성 위기: 180 ~

fun sysToLevelWithValue(sys: Int) = when{
    sys < 1 -> "-"
    sys in 1 until 120 -> "(정상:${sys})"
    sys in 120..179 -> "(주의:${sys})"
    else -> "(위험:${sys})"
}

fun diaToLevelWithValue(dia: Int) = when{
    dia < 1 -> "-"
    dia in 1 until 80 -> "(정상:${dia})"
    dia in 80..95 -> "(주의:${dia})"
    else -> "(위험:${dia})"
}

fun killApp(activity: Activity){
    activity.moveTaskToBack(true); // 태스크를 백그라운드로 이동
    activity.finishAndRemoveTask(); // 액티비티 종료 + 태스크 리스트에서 지우기
    android.os.Process.killProcess(android.os.Process.myPid()); // 앱 프로세스 종료
}

fun assetFilePath(context: Context, assetName: String): String? {
    val file = File(context.filesDir, assetName)
    if (file.exists() && file.length() > 0) {
        return file.absolutePath
    }
    try {
        context.assets.open(assetName).use { `is` ->
            FileOutputStream(file).use { os ->
                val buffer = ByteArray(4 * 1024)
                var read: Int
                while (`is`.read(buffer).also { read = it } != -1) {
                    os.write(buffer, 0, read)
                }
                os.flush()
            }
            return file.absolutePath
        }
    } catch (e: IOException) {
        Log.e("FaceDetector>>", "Error process asset $assetName to file path")
    }
    return null
}

@Throws(IOException::class)
fun bitmap2ByteArray(bitmap: Bitmap, compressionPercent: Int = 100): ByteArray {
    val byteStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, compressionPercent, byteStream)
    val resultByteArr = byteStream.toByteArray()
    byteStream.close()
    return resultByteArr
}

fun bitmap2List(bitmap: Bitmap): List<List<List<Int>>>{
    val width = bitmap.width
    val height = bitmap.height
    val resultList = arrayListOf<List<List<Int>>>()

    for(col in 0 until height){
        val rowList = arrayListOf<List<Int>>()
        for(row in 0 until width){
            rowList.add(
                listOf(
                    bitmap.getPixel(row, col).red,
                    bitmap.getPixel(row, col).green,
                    bitmap.getPixel(row, col).blue
                )
            )
        }
        resultList.add(rowList)
    }

    return resultList
}

fun createCornersPath(
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    cornerRadius: Float,
    cornerLength: Float
): Path {
    val path = Path()
// top left
    path.moveTo(left, (top + cornerRadius))
    path.arcTo(
        RectF(left,top, left + cornerRadius,top + cornerRadius),
        180f,
        90f,
        true
    )

    path.moveTo(left + (cornerRadius / 2f), top)
    path.lineTo(left + (cornerRadius / 2f) + cornerLength, top)

    path.moveTo(left, top + (cornerRadius / 2f))
    path.lineTo(left, top + (cornerRadius / 2f) + cornerLength)

    // top right
    path.moveTo(right - cornerRadius, top)
    path.arcTo(
        RectF(right - cornerRadius, top, right,top + cornerRadius),
        270f,
        90f,
        true
    )

    path.moveTo(right - (cornerRadius / 2f), top)
    path.lineTo(right - (cornerRadius / 2f) - cornerLength, top)

    path.moveTo(right, top + (cornerRadius / 2f))
    path.lineTo(right, top + (cornerRadius / 2f) + cornerLength)

    // bottom left
    path.moveTo(left, bottom - cornerRadius)
    path.arcTo(
        RectF(left, bottom - cornerRadius,left+cornerRadius, bottom),
        90f,
        90f,
        true
    )

    path.moveTo(left + (cornerRadius / 2f), bottom)
    path.lineTo(left + (cornerRadius / 2f) + cornerLength, bottom)

    path.moveTo(left, bottom - (cornerRadius / 2f))
    path.lineTo(left, bottom - (cornerRadius / 2f) - cornerLength)

    // bottom right
    path.moveTo(left, bottom - cornerRadius)
    path.arcTo(
        RectF(right - cornerRadius,bottom - cornerRadius, right,  bottom),
        0f,
        90f,
        true
    )

    path.moveTo(right - (cornerRadius / 2f), bottom)
    path.lineTo(right - (cornerRadius / 2f) - cornerLength, bottom)

    path.moveTo(right, bottom - (cornerRadius / 2f))
    path.lineTo(right, bottom - (cornerRadius / 2f) - cornerLength)

    return path
}