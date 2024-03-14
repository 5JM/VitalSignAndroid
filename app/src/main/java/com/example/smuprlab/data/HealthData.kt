package com.example.smuprlab.data

data class HealthData(
    var bpm: Int = 0,
    var stress: Int = 0,
    var bloodPressureData: BloodPressure = BloodPressure(),
)