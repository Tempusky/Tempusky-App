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
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.tempusky.R
import com.example.tempusky.core.helpers.SensorsDataHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

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
    private val auth: FirebaseAuth = Firebase.auth
    private val db: FirebaseFirestore = Firebase.firestore

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
        if (auth.currentUser == null) {
            stopSelf()
        }
        Companion.intent = intent!!
        val notification = buildNotification("Started Environment Sensors Service")
        startForeground(NOTIFICATION_ID, notification)
        val sensors = getSensors()
        registerSensors(sensors)
        return START_STICKY
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            "Environment Sensors Service Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)
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
                uploadDataToCloud(latitude, longitude, temperatureReceived, pressureReceived, humidityReceived)
            }
        }
    }

    private fun uploadDataToCloud(latitude: Double, longitude: Double, temperature: Float, pressure: Float, humidity: Float) {
        // Upload data to cloud
        val data = hashMapOf(
            "latitude" to latitude,
            "longitude" to longitude,
            "temperature" to temperature,
            "pressure" to pressure,
            "humidity" to humidity,
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("users").document("${auth.currentUser?.uid}")
            .collection("environment_sensors_data")
            .add(data)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
                val updatedNotification = buildNotification("Data uploaded to cloud")
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(NOTIFICATION_ID, updatedNotification)
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
                val errorNotification = buildNotification("Error uploading data to cloud")
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(NOTIFICATION_ID, errorNotification)
            }
    }

    private fun buildNotification(text: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Environment Sensors Service")
            .setContentText(text)
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()
    }
}
