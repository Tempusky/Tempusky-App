package com.example.tempusky.core.broadcastReceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.example.tempusky.core.helpers.SensorsDataHelper

class EnvironmentSensorsReceiver : BroadcastReceiver(), SensorEventListener {

    private var sensorManager: SensorManager? = null
    private var temperatureUpdated: Boolean? = null
    private var pressureUpdated: Boolean? = null
    private var humidityUpdated: Boolean? = null

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive")
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensors = getSensors()
        registerSensors(sensors)
    }

    private fun getSensors(): Triple<Sensor?, Sensor?, Sensor?> {
        val temperatureSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)
        val pressureSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_PRESSURE)
        val humiditySensor = sensorManager?.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY)
        return Triple(temperatureSensor, pressureSensor, humiditySensor)
    }

    private fun registerSensors(sensors: Triple<Sensor?, Sensor?, Sensor?>) {
        sensors.first?.also {
            temperatureUpdated = false
            sensorManager?.registerListener(this, it, 10000)
        }
        sensors.second?.also {
            pressureUpdated = false
            sensorManager?.registerListener(this, it, 10000)
        }
        sensors.third?.also {
            humidityUpdated = false
            sensorManager?.registerListener(this, it, 10000)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do nothing
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            when (it.sensor.type) {
                Sensor.TYPE_AMBIENT_TEMPERATURE -> {
                    val temperatureCelsius = it.values[0]
                    Log.d(TAG, "Temperature: $temperatureCelsius")
                    SensorsDataHelper.updateTemperatureData(temperatureCelsius)
                    temperatureUpdated = true
                }
                Sensor.TYPE_PRESSURE -> {
                    val pressure = it.values[0]
                    Log.d(TAG, "Pressure: $pressure")
                    SensorsDataHelper.updatePressureData(pressure)
                    pressureUpdated = true
                }
                Sensor.TYPE_RELATIVE_HUMIDITY -> {
                    val humidity = it.values[0]
                    Log.d(TAG, "Humidity: $humidity")
                    SensorsDataHelper.updateHumidityData(humidity)
                    humidityUpdated = true
                }
                else -> {
                    Log.d(TAG, "Unknown sensor type")
                }
            }
            if (temperatureUpdated != false && pressureUpdated != false && humidityUpdated != false) {
                sensorManager?.unregisterListener(this)
                Log.d(TAG, "All sensors updated")
            }
        }

    }
    companion object {
        private const val TAG = "EnvironmentSensorsReceiver"
    }
}