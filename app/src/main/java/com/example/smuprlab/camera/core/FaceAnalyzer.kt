package com.example.smuprlab.camera.core

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RectF
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.core.graphics.toRectF
import com.example.smuprlab.viewmodel.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kr.smu.prlab.Prlab
import java.io.ByteArrayOutputStream

class FaceAnalyzer(
    private val faceBoxViewModel: ViewModel,
    private val width: Float,
    private val height: Float
) : ImageAnalysis.Analyzer {
    // 얼굴 검출
    private val translation = 60f // faceDetection 결과로 얼굴 box가 너무 딱 맞게 나와서 마진을 줌

    override fun analyze(imageProxy: ImageProxy) {
        val bitmap = Bitmap.createBitmap(
            imageProxy.toBitmap(),
            0,
            0,
            imageProxy.width,
            imageProxy.height,
            getRotation(
                imageProxy.imageInfo.rotationDegrees,
                imageProxy.width
            ),
            false
        )

        val ratioX = width / bitmap.width.toFloat()
        val ratioY = height / bitmap.height.toFloat()

        var _box = FaceDetector.instance.faceDetector.run(bitmap)

        if (_box == null) _box = Rect(0, 0, 0, 0)

        var box = RectF()

        Matrix().apply {
            setScale(ratioX, ratioY)
            mapRect(box, _box.toRectF())
        }

        box = if (box.left == 0f && box.top == 0f) {
            RectF(
                -100f,
                -100f,
                width + 100f,
                height + 100f
            )
        } else {
            RectF(
                box.left - translation,
                box.top - translation,
                box.right + translation,
                box.bottom + translation
            ) // adjust face box - end
        }

        faceBoxViewModel.update(box)

        // if not detect -> box = (0,0,1,1)
        if (_box.width() > 1 && _box.height() > 1){
            try {
                if (faceBoxViewModel.isEstimating.value) {
                    val faceBitmap = Bitmap.createBitmap(
                        bitmap,
                        _box.left,
                        _box.top,
                        _box.width(),
                        _box.height()
                    )

                    // for ByteArray Version
//                    val barr = bitmapToByteArray(faceBitmap)

                    Prlab.instance.apply {

                        // input: Bitmap
                        processFrame(faceBitmap, System.currentTimeMillis())

                        // input: ByteArray
//                        processFrame(getSkin(faceBitmap), System.currentTimeMillis())

                        // for ByteArray Version
//                        processFrame(
//                            barr,
//                            System.currentTimeMillis()
//                        )


                        faceBoxViewModel.apply {
                            getBandpassedSignal()?.let { updateSignal(it) }

                            updateBpm(getAverageBpm())
                            updateFps(getFps())

                            updateConfidence(getConfidence())
                            updateHrvConfidence(getHrvConfidence())

                            updateStress(getStress())

                            updateHRVFeatures(getVLF(), getLF(), getHF())

                            if(estimatingGenderAge.value){
                                // 1초마다 input을 주는 트릭
                                val time = ((System.currentTimeMillis() - startEstimateTime.value) / 1000).toInt()

                                if(time != genderAgeTrigger.value){
                                    updateGenderAgeTrigger(time)

                                    CoroutineScope(Dispatchers.Default).launch{
                                        val ageGenderVector = runAgeGenderModel(faceBitmap)

                                        updateAge(ageGenderVector[0])
                                        updateGender(ageGenderVector[1])

                                    }

                                    CoroutineScope(Dispatchers.Default).launch {
                                        runBP()

                                        updateSBP(getSBP())
                                        updateDBP(getDBP())
                                    }

                                }
                            }
                        }
                    }

//                    faceBitmap.recycle()
                }

            } catch (e: Exception) {
                Log.e("Face>>", e.stackTrace.toString())
            }
        }

        bitmap.recycle()

        imageProxy.close()
    }
}
private fun getRotation(rotation: Int, width : Int)
        = Matrix().apply {
    setScale(-1f,1f)
    postTranslate(width.toFloat(),0f)
    postRotate(360 - rotation.toFloat())
}

private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
    return outputStream.toByteArray()
}

private fun byteArrayToHex(a: ByteArray): String {
    val sb = StringBuilder()

    for (b in a) sb.append(String.format("%02x ", b.toInt() and 0xff))
    return sb.toString()
}