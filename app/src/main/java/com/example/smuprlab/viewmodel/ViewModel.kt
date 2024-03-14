package com.example.smuprlab.viewmodel

import android.annotation.SuppressLint
import android.graphics.RectF
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Vector

class ViewModel: ViewModel(){
    private val _sharedPermissionState = MutableStateFlow(false)
    val sharedPermissionState: StateFlow<Boolean> = _sharedPermissionState

    private val _startEstimateTime = mutableStateOf(0L)
    val startEstimateTime: State<Long> = _startEstimateTime

    private val _isEstimating = MutableStateFlow(false)
    val isEstimating: StateFlow<Boolean> = _isEstimating

    private val _estimatingGenderAge =  MutableStateFlow(false)
    val estimatingGenderAge: StateFlow<Boolean> = _estimatingGenderAge

    private val _estimatingBP =  MutableStateFlow(false)
    val estimatingBP: StateFlow<Boolean> = _estimatingBP

    private val _genderAgeTrigger =  mutableStateOf(0)
    val genderAgeTrigger: State<Int> = _genderAgeTrigger

    private val _bPTrigger =  mutableStateOf(0)
    val bPTrigger: State<Int> = _bPTrigger

//    private val _exposureEnable = MutableStateFlow(false)
//    val exposureEnable: StateFlow<Boolean> = _exposureEnable
//
//    private val _exposureMax = MutableStateFlow(20)
//    val exposureMax: StateFlow<Int> = _exposureMax
//
//    private val _exposureMin = MutableStateFlow(-20)
//    val exposureMin: StateFlow<Int> = _exposureMin
//
//    private val _exposureIndex = MutableStateFlow(0)
//    val exposureIndex: StateFlow<Int> = _exposureIndex

    private val _sharedFaceBoxState = mutableStateOf(
        RectF(0f,0f,0f, 0f)
    )
    val sharedFaceBoxState: State<RectF> = _sharedFaceBoxState

    @SuppressLint("MutableCollectionMutableState")
    private val _sharedSignal = mutableStateOf<Vector<Float>>(
        Vector()
    )
    val sharedSignal: State<Vector<Float>> = _sharedSignal

    @SuppressLint("MutableCollectionMutableState")
    private val _sharedWholeSignal = mutableStateOf<Vector<Float>>(
        Vector()
    )
    val sharedWholeSignal = _sharedWholeSignal

    private val _sharedBpm = MutableStateFlow(0)
    val sharedBpm : StateFlow<Int> = _sharedBpm

    private val _sharedFps = MutableStateFlow(0)
    val sharedFps : StateFlow<Int> = _sharedFps

    private val _sharedConfidence = MutableStateFlow(0f)
    val sharedConfidence : StateFlow<Float> = _sharedConfidence

    private val _sharedHrvConfidence = MutableStateFlow(0f)
    val sharedHrvConfidence : StateFlow<Float> = _sharedHrvConfidence

    private val _sharedStress = MutableStateFlow(0)
    val sharedStress : StateFlow<Int> = _sharedStress

    private val _sharedSBP = MutableStateFlow(0f)
    val sharedSBP : StateFlow<Float> = _sharedSBP

    private val _sharedDBP = MutableStateFlow(0f)
    val sharedDBP : StateFlow<Float> = _sharedDBP

    private val _sharedVLF = MutableStateFlow(0f)
    val sharedVLF : StateFlow<Float> = _sharedVLF

    private val _sharedLF = MutableStateFlow(0f)
    val sharedLF : StateFlow<Float> = _sharedLF

    private val _sharedHF = MutableStateFlow(0f)
    val sharedHF : StateFlow<Float> = _sharedHF

    private val _sharedGender = MutableStateFlow(0f)
    val sharedGender: StateFlow<Float> = _sharedGender

    private val _sharedAge = MutableStateFlow(0f)
    val sharedAge: StateFlow<Float> = _sharedAge

    fun reset(){
        _sharedSignal.value = Vector()
        _sharedWholeSignal.value = Vector()
        _startEstimateTime.value = 0L
        _genderAgeTrigger.value = 0
        _bPTrigger.value = 0

        viewModelScope.launch {
            _estimatingGenderAge.emit(false)
            _estimatingBP.emit(false)

            _sharedAge.emit(0f)
            _sharedGender.emit(0f)

            _sharedBpm.emit(0)
            _sharedFps.emit(0)
            _sharedConfidence.emit(0.0f)

            _sharedHrvConfidence.emit(0f)
            _sharedDBP.emit(0f)
            _sharedSBP.emit(0f)
            _sharedStress.emit(0)

            _sharedVLF.emit(0f)
            _sharedLF.emit(0f)
            _sharedHF.emit(0f)
        }
    }

//    fun updateExposureEnable(enable: Boolean){
//        viewModelScope.launch {
//            _exposureEnable.emit(enable)
//        }
//    }
//
//    fun updateExposureRange(min: Int, max: Int){
//        viewModelScope.launch {
//            _exposureMin.emit(min)
//            _exposureMax.emit(max)
//        }
//    }
//
//    fun updateExposureIndex(index: Int){
//        viewModelScope.launch {
//            _exposureIndex.emit(index)
//        }
//    }

    fun updateGenderAgeTrigger(value: Int){
        _genderAgeTrigger.value = value
    }

    fun updateStartEstimateTime(time: Long){
        _startEstimateTime.value = time
    }

    fun updateEstimateGenderAge(state: Boolean){
        viewModelScope.launch {
            _estimatingGenderAge.emit(state)
        }
    }

    fun updateGender(probability: Float){
        viewModelScope.launch {
            _sharedGender.emit(probability)
        }
    }

    fun updateAge(probability: Float){
        viewModelScope.launch {
            _sharedAge.emit(probability)
        }
    }

    fun updateEstimateState(state: Boolean){
        viewModelScope.launch{
            _isEstimating.emit(state)
        }
    }
    fun updatePermissionState(state: Boolean){
        viewModelScope.launch{
            _sharedPermissionState.emit(state)
        }
    }

    fun updateSignal(signal:Vector<Float>){
        _sharedSignal.value = signal
    }

    fun updateWholeSignal(signal: Vector<Float>){
        _sharedWholeSignal.value = signal
    }

    fun updateBpm(bpm:Int){
        viewModelScope.launch {
            _sharedBpm.emit(bpm)
        }
    }

    fun updateFps(fps:Int){
        viewModelScope.launch {
            _sharedFps.emit(fps)
        }
    }

    fun updateConfidence(confidence:Float){
        viewModelScope.launch {
            _sharedConfidence.emit(confidence)
        }
    }

    fun updateHrvConfidence(confidence:Float){
        viewModelScope.launch {
            _sharedHrvConfidence.emit(confidence)
        }
    }

    fun updateStress(stress:Int){
        viewModelScope.launch {
            _sharedStress.emit(stress)
        }
    }

    fun updateSBP(sbp: Float){
        viewModelScope.launch {
            _sharedSBP.emit(sbp)
        }
    }

    fun updateDBP(dbp:Float){
        viewModelScope.launch {
            _sharedDBP.emit(dbp)
        }
    }

    fun updateHRVFeatures(vlf:Float, lf:Float, hf:Float){
        viewModelScope.launch {
            if(vlf.isFinite())
                _sharedVLF.emit(vlf)

            if(lf.isFinite())
                _sharedLF.emit(lf)

            if(hf.isFinite())
                _sharedHF.emit(hf)
        }
    }

    internal fun update(box: RectF){
        _sharedFaceBoxState.value = box
    }
}
