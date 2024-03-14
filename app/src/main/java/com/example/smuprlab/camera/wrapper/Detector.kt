package com.example.smuprlab.camera.wrapper

import android.graphics.Bitmap
import android.graphics.Rect
import androidx.annotation.Keep
import java.util.Vector

class Detector(
    model_path: String,
    model_weight_path: String,
    inWidth: Int,
    inHeight: Int,
    scoreThresh: Float,
    iouThresh: Float,
    useTracker: Boolean
) {
    @Keep
    private var selfAddr: Long = 0

    companion object{
        init {
            System.loadLibrary("prlab")
        }
    }

    init{
        selfAddr = this.newSelf(
            model_path,
            model_weight_path,
            inWidth,
            inHeight,
            scoreThresh,
            iouThresh,
            useTracker
        )
    }
    fun delete() {
        deleteSelf(selfAddr)
        selfAddr = 0
    }
    @Throws(Throwable::class)
    protected fun finalize() {
        delete()
    }

    fun run(input: Bitmap): Rect? = run(selfAddr, input)

    private external fun newSelf(
        model_path: String,
        model_weight_path: String,
        inWidth: Int,
        inHeight: Int,
        scoreThresh: Float,
        iouThresh: Float,
        useTracker: Boolean,
    ): Long

    private external fun deleteSelf(selfAddr: Long)

    private external fun run(selfAddr: Long, input: Bitmap): Rect?

}