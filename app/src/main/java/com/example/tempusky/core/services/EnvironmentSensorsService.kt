package com.example.tempusky.core.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.tempusky.R
import com.example.tempusky.core.helpers.SensorsDataHelper

class EnvironmentSensorsService : Service(), SensorEventListener {

    companion object {
        private const val TAG = "EnvironmentSensorsService"
        private const val CHANNEL_ID = "EnvironmentSensorsServiceChannel"
        private lateinit var intent: Intent
    }

    private var sensorManager: SensorManager? = null
    private var temperatureUpdated: Boolean? = null
    private var pressureUpdated: Boolean? = null
    private var humidityUpdated: Boolean? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        sensorManager = this.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Companion.intent = intent!!
        val notification = buildNotification()
        startForeground(1, notification)
        val sensors = getSensors()
        registerSensors(sensors)
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Environment Sensors Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
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
                val latitude = intent.getDoubleExtra("latitude", 0.0)
                val longitude = intent.getDoubleExtra("longitude", 0.0)
                Log.d(TAG, "Location in Environemt sensors Service: $latitude, $longitude")
                // UPload to cloud
                buildNotification()
                stopSelf()
            }
        }
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Environment Sensors Service")
            .setContentText("Service is running in the background")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
    }
}