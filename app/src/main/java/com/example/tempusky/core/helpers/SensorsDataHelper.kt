package com.example.tempusky.core.helpers

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

object SensorsDataHelper {

    private var _tempData: MutableLiveData<Float> = MutableLiveData(0.0f)
    val tempData: LiveData<Float> = _tempData

    private var _pressureData: MutableLiveData<Float> = MutableLiveData(0.0f)
    val pressureData: LiveData<Float> = _pressureData

    private var _humidityData: MutableLiveData<Float> = MutableLiveData(0.0f)
    val humidityData: LiveData<Float> = _humidityData

    fun updateTemperatureData(temperature: Float) {
        _tempData.value = temperature
    }

    fun updatePressureData(pressure: Float) {
        _pressureData.value = pressure
    }

    fun updateHumidityData(humidity: Float) {
        _humidityData.value = humidity
    }
}