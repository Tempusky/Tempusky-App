package com.example.tempusky.core

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.example.tempusky.core.helpers.SensorsDataHelper

class EnvironmentSensorManager(private val context: Context) : SensorEventListener {

    private val sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var temperatureSensor: Sensor? = null
    private var pressureSensor: Sensor? = null
    private var humiditySensor: Sensor? = null

    init {
        temperatureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)
        pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)
        humiditySensor = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY)
    }

    fun startListening() {
        temperatureSensor?.also { sensorManager.registerListener(this, it, 10000) }
        pressureSensor?.also { sensorManager.registerListener(this, it, 10000) }
        humiditySensor?.also { sensorManager.registerListener(this, it, 10000) }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do nothing
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            when (it.sensor.type) {
                Sensor.TYPE_AMBIENT_TEMPERATURE -> {
                    val temperatureCelsius = it.values[0]
                    //Log.d(TAG, "Temperature: $temperatureCelsius")
                    SensorsDataHelper.updateTemperatureData(temperatureCelsius)
                }
                Sensor.TYPE_PRESSURE -> {
                    val pressure = it.values[0]
                    //Log.d(TAG, "Pressure: $pressure")
                    SensorsDataHelper.updatePressureData(pressure)
                }
                Sensor.TYPE_RELATIVE_HUMIDITY -> {
                    val humidity = it.values[0]
                    //Log.d(TAG, "Humidity: $humidity")
                    SensorsDataHelper.updateHumidityData(humidity)
                }

                else -> {
                    Log.d(TAG, "Unknown sensor type")
                }
            }
        }
    }

    companion object {
        private const val TAG = "EnvironmentSensorManager"
    }
}
