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
import kotlin.random.Random

class EnvironmentSensorsService : Service(), SensorEventListener {

    companion object {
        private const val TAG = "EnvironmentSensorsService"
        private const val CHANNEL_ID = "EnvironmentSensorsServiceChannel"
        private const val NOTIFICATION_ID = 1
        private lateinit var intent: Intent
    }

    private var sensorManager: SensorManager? = null
    private var temperatureUpdated: Boolean? = null
    private var pressureUpdated: Boolean? = null
    private var humidityUpdated: Boolean? = null
    private var temperatureReceived = 0.0f
    private var pressureReceived = 0.0f
    private var humidityReceived = 0.0f

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
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        sensors.second?.also {
            pressureUpdated = false
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        sensors.third?.also {
            humidityUpdated = false
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Companion.intent = intent!!
        val notification = buildNotification("Started Environment Sensors Service")
        startForeground(NOTIFICATION_ID, notification)
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
                    temperatureReceived = temperatureCelsius
                    Log.d(TAG, "Temperature: $temperatureCelsius")
                    SensorsDataHelper.updateTemperatureData(temperatureCelsius)
                    temperatureUpdated = true
                }
                Sensor.TYPE_PRESSURE -> {
                    val pressure = it.values[0]
                    pressureReceived = pressure
                    Log.d(TAG, "Pressure: $pressure")
                    SensorsDataHelper.updatePressureData(pressure)
                    pressureUpdated = true
                }
                Sensor.TYPE_RELATIVE_HUMIDITY -> {
                    val humidity = it.values[0]
                    humidityReceived = humidity
                    Log.d(TAG, "Humidity: $humidity")
                    SensorsDataHelper.updateHumidityData(humidity)
                    humidityUpdated = true
                }
                else -> {
                    Log.d(TAG, "Unknown sensor type")
                }
            }
            if (temperatureUpdated == true && pressureUpdated == true && humidityUpdated == true) {
                sensorManager?.unregisterListener(this)
                val latitude = intent.getDoubleExtra("latitude", 0.0)
                val longitude = intent.getDoubleExtra("longitude", 0.0)
                val updatedNotification = buildNotification("Sensors data updated and uploaded $latitude, $longitude, $temperatureReceived, $pressureReceived, $humidityReceived")
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(NOTIFICATION_ID, updatedNotification)
            }
        }
    }

    private fun buildNotification(text: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Environment Sensors Service")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
    }
}
