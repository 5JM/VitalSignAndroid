package kr.smu.prlab

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.util.Arrays
import java.util.Vector

class Prlab{
    private var selfAddr: Long = 0
    private var selfAddrONNX: Long = 0
    private var selfAddrONNXBP: Long = 0
    private val fps: Float = 30f
    private val minBpm: Int= 40
    private val maxBpm: Int= 180
    private val bpmUpdatePeriod: Float= 1f
//    private val modelPath: String = ""
    private val imgHeight: Int = 64
    private val imgWidth: Int = 64
    private var sbp: Float = 0f
    private var dbp: Float = 0f

    companion object{
        @JvmStatic
        val instance = Prlab()

        init{
            System.loadLibrary("prlab")
        }
    }

    fun init(
        duration: Float = 5f,
        hrvTime: Int = 20,
        ageGenderModelPath: String,
        bloodPressureModelPath: String
    ){

        selfAddr = nativeCreateObject(
            fps = fps,
            duration = duration,
            minBpm = minBpm,
            maxBpm = maxBpm,
            bpmUpdatePeriod = bpmUpdatePeriod,
            hrvTime = hrvTime
        )

        selfAddrONNX = newSelf(
            model_path = ageGenderModelPath,
            img_height = imgHeight,
            img_width = imgWidth
        )

        selfAddrONNXBP = newSelfBP(
            model_path = bloodPressureModelPath
        )
    }

    fun init(
        duration: Float = 5f,
        hrvTime: Int = 20,
        ageGenderModelData: ByteArray,
        ageGenderModelSize: Int,
        bloodPressureModelData: ByteArray,
        bloodPressureModelSize: Int,
    ){
        selfAddr = nativeCreateObject(
            fps = fps,
            duration = duration,
            minBpm = minBpm,
            maxBpm = maxBpm,
            bpmUpdatePeriod = bpmUpdatePeriod,
            hrvTime = hrvTime
        )

        selfAddrONNX = newSelfByteArray(
            model_byte_data = ageGenderModelData,
            img_height = imgHeight,
            img_width = imgWidth,
            model_size = ageGenderModelSize
        )

        selfAddrONNXBP = newSelfBPByteArray(
            model_byte_data = bloodPressureModelData,
            model_size = bloodPressureModelSize
        )
    }

    private fun delete() {
        nativeDestroyObject(selfAddr)
        deleteSelf(selfAddrONNX)
        deleteSelfBP(selfAddrONNXBP)

        selfAddr = 0
        selfAddrONNX = 0
        selfAddrONNXBP = 0
    }

    fun runAgeGenderModel(bitmap: Bitmap): Vector<Float> =
        run(selfAddrONNX, resize(bitmap, 64))

    fun runAgeGenderModel(input: ByteArray): Vector<Float> =
        run(selfAddrONNX, resize(byteArrayToBitmap(input), 64))

    fun runBPModel(): Vector<Float> {
        val _tmp = getBandpassedSignal()

        return if(_tmp != null){
            val tmp = _tmp.toFloatArray()
            runBP(selfAddrONNXBP, tmp)
        }else{
            Vector<Float>(listOf(0f,0f))
        }
        
    }

    fun runBP() {
        val tmp = getBandpassedSignal()

        if(tmp != null){
            val t = runBP(selfAddrONNXBP, tmp.toFloatArray())
            sbp = t[0]
            dbp = t[1]
        }
    }

    fun getSBP() = sbp

    fun getDBP() = dbp


    fun processFrame(skinDataArray: Array<Float>, time: Long){
        val _skinDataArray = skinDataArray.toFloatArray()

        nativeProcessFrameWithSkinArray(selfAddr, _skinDataArray, time)
    }

    fun processFrame(image: Bitmap, time: Long) {
        nativeProcessFrame(selfAddr, image, time)
    }

    fun processFrame(image: ByteArray, time: Long){
        byteArrayToBitmap(image).also {
            nativeProcessFrame(selfAddr, it, time)
        }
    }

    fun getSkin(): Array<Float>{
        var segmented = getSkinArray(selfAddr)
        for(i in segmented){
            if(i.isNaN() || i  < 0.0f){
                segmented = Vector<Float>(3)
                break
            }
        }
        return arrayOf(segmented[0], segmented[1], segmented[2])
    }

    fun getSkin(input: Bitmap): Array<Float> {
        var segmented = getSkinDataArray(selfAddr, input)
        for(i in segmented){
            if(i.isNaN() || i  < 0.0f){
                segmented = Vector<Float>(3)
                break
            }
        }
        return arrayOf(segmented[0], segmented[1], segmented[2])
    }

    fun getAverageBpm(): Int {
        return nativeGetHeartrate(selfAddr)
    }

    fun getFps(): Int {
        return nativeGetFps(selfAddr)
    }

    fun getConfidence(): Float {
        return nativeGetConfidence(selfAddr)
    }

    fun getHrvConfidence(): Float {
        return nativeGetHrvConfidence(selfAddr)
    }

    fun getStress(): Int = nativeGetBPStress(selfAddr)

    fun getVLF(): Float = nativeGetVLF(selfAddr)

    fun getLF(): Float = nativeGetLF(selfAddr)

    fun getHF(): Float = nativeGetHF(selfAddr)

    fun getBandpassedSignal(): Vector<Float>? =
        nateGetBandpassedSignal(selfAddr)


    fun getHrvBandpassedSignal(): Vector<Float> =
        nateGetHrvBandpassedSignal(selfAddr)

    fun reset() {
        nativeReset(selfAddr)
    }

    private external fun newSelf(
        model_path: String,
        img_height: Int,
        img_width: Int
    ): Long

    private external fun newSelfByteArray(
        model_byte_data: ByteArray,
        model_size: Int,
        img_height: Int,
        img_width: Int
    ): Long

    private external fun newSelfBP(
        model_path: String,
    ): Long

    private external fun newSelfBPByteArray(
        model_byte_data: ByteArray,
        model_size: Int,
    ): Long

    private external fun deleteSelf(selfAddr: Long)

    private external fun deleteSelfBP(selfAddr: Long)

    private external fun run(selfAddr: Long, inbitmap: Bitmap): Vector<Float>

    private external fun runBP(selfAddr: Long, signal: FloatArray): Vector<Float>

    private external fun nativeProcessFrameWithSkinArray(mNativeObj: Long, skinDataArray: FloatArray, time: Long)

    private external fun nativeProcessFrame(mNativeObj: Long, image: Bitmap, time: Long)

    private external fun nativeCreateObject(
        fps: Float,
        duration: Float,
        minBpm: Int,
        maxBpm: Int,
        bpmUpdatePeriod: Float,
        hrvTime: Int
    ): Long

    private external fun getSkinDataArray(selfAddr: Long, input: Bitmap): Vector<Float>

    private external fun getSkinArray(selfAddr: Long): Vector<Float>

    private external fun nateGetBandpassedSignal(mNativeObj: Long): Vector<Float>?

    private external fun nateGetHrvBandpassedSignal(mNativeObj: Long): Vector<Float>
//    private external fun nativeGetSignal(mNativeObj: Long): Vector<Float>

    private external fun nativeGetHeartrate(mNativeObj: Long): Int

    private external fun nativeGetFps(mNativeObj: Long): Int

    private external fun nativeGetConfidence(mNativeObj: Long): Float

    private external fun nativeGetHrvConfidence(mNativeObj: Long): Float

    private external fun nativeGetBPStress(mNativeObj: Long): Int

    private external fun nativeGetVLF(mNativeObj: Long): Float

    private external fun nativeGetLF(mNativeObj: Long): Float

    private external fun nativeGetHF(mNativeObj: Long): Float

    private external fun nativeReset(mNativeObj: Long)

    private external fun nativeDestroyObject(mNativeObj: Long)

    private fun byteArrayToBitmap(byteArray: ByteArray): Bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
}

private fun resize(bitmap: Bitmap, resizedSize: Int): Bitmap =
    Bitmap.createScaledBitmap(bitmap, resizedSize, resizedSize, false)
