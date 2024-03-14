package com.example.smuprlab.data

import androidx.annotation.Keep
import java.io.Serializable
import java.util.Vector

@Keep
data class MeasurementDTO(
    val rgbValues: MutableList<Vector<Float>>
): Serializable