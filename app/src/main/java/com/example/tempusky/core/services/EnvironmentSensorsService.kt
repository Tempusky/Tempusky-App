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
import android.net.ConnectivityManager
import android.os.Handler
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.tempusky.R
import com.example.tempusky.core.helpers.SensorsDataHelper
import com.example.tempusky.data.SettingsDataStore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.properties.Delegates

class EnvironmentSensorsService : Service(), SensorEventListener {

    companion object {
        private const val TAG = "EnvironmentSensorsService"
        private const val CHANNEL_ID = "EnvironmentSensorsServiceChannel"
        private const val NOTIFICATION_ID = 1
        private lateinit var intent: Intent
        private const val MIN_TEMPERATURE = -100.0f
        private const val MAX_TEMPERATURE = 100.0f
        private const val SENSOR_TIMEOUT = 1200000L
    }

    private var sensorManager: SensorManager? = null
    private var temperatureReceived: Float? = null
    private var pressureReceived: Float? = null
    private var humidityReceived: Float? = null
    private var temperatureUpdated: Boolean = false
    private var pressureUpdated: Boolean = false
    private var humidityUpdated: Boolean = false
    private val auth: FirebaseAuth = Firebase.auth
    private val db: FirebaseFirestore = Firebase.firestore
    private var handler: Handler? = null
    private var timeoutRunnable: Runnable? = null

    private var temperatureAllowed by Delegates.notNull<Boolean>()
    private var pressureAllowed by Delegates.notNull<Boolean>()
    private var humidityAllowed by Delegates.notNull<Boolean>()
    private lateinit var settingsDataStore: SettingsDataStore

    private var wifiOnly by Delegates.notNull<Boolean>()
    private var isConnectedToWifi: Boolean = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        sensorManager = this.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        isConnectedToWifi = isConnectedToWifi(applicationContext)
        handler = Handler()
    }

    private fun getSensorUserPermissions() {
        runBlocking {
            temperatureAllowed = settingsDataStore.getTemperature.first()
            pressureAllowed = settingsDataStore.getPressure.first()
            humidityAllowed = settingsDataStore.getHumidity.first()
            wifiOnly = settingsDataStore.getNetwork.first() == "Wi-Fi"
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    /**
     * Get the sensors available on the device, depending on the user's permissions
     */
    private fun getSensors(): Triple<Sensor?, Sensor?, Sensor?> {
        val temperatureSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)
        val pressureSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_PRESSURE)
        val humiditySensor = sensorManager?.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY)
        disableSensorsIfNotFound(temperatureSensor, pressureSensor, humiditySensor)
        return Triple(
            if (temperatureAllowed) temperatureSensor else null,
            if (pressureAllowed) pressureSensor else null,
            if (humidityAllowed) humiditySensor else null
        )
    }

    private fun disableSensorsIfNotFound(
        temperatureSensor: Sensor?,
        pressureSensor: Sensor?,
        humiditySensor: Sensor?
    ) {
        runBlocking {
            if (temperatureSensor == null) {
                settingsDataStore.setTemperatureEnabled(false)
                temperatureAllowed = false
            }
            if (pressureSensor == null) {
                settingsDataStore.setPressureEnabled(false)
                pressureAllowed = false
            }
            if (humiditySensor == null) {
                settingsDataStore.setHumidityEnabled(false)
                humidityAllowed = false
            }
        }
    }

    private fun isConnectedToWifi(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected && networkInfo.type == ConnectivityManager.TYPE_WIFI
    }

    private fun registerSensors(sensors: Triple<Sensor?, Sensor?, Sensor?>) {
        sensors.first?.also {
            temperatureUpdated = false
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
            Log.d(TAG, "Temperature sensor registered")
        }
        sensors.second?.also {
            pressureUpdated = false
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
            Log.d(TAG, "Pressure sensor registered")
        }
        sensors.third?.also {
            humidityUpdated = false
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
            Log.d(TAG, "Humidity sensor registered")
        }

        // Set a timeout to ensure sensors don't wait indefinitely
        timeoutRunnable = Runnable {
            if (!(
                        (temperatureUpdated || !temperatureAllowed) &&
                                (pressureUpdated || !pressureAllowed) &&
                                (humidityUpdated || !humidityAllowed))
            ) {
                Log.d(TAG, "Sensor data timeout")
                handleTimeout()
            }
        }
        handler?.postDelayed(timeoutRunnable!!, SENSOR_TIMEOUT)
    }

    private fun handleTimeout() {
        val latitude = intent.getDoubleExtra("latitude", 0.0)
        val longitude = intent.getDoubleExtra("longitude", 0.0)
        uploadDataToCloud(
            latitude,
            longitude,
            temperatureReceived,
            pressureReceived,
            humidityReceived
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand()")
        val notification = buildNotification("Started Environment Sensors Service")
        startForeground(NOTIFICATION_ID, notification)
        settingsDataStore = SettingsDataStore(applicationContext)
        if (intent != null) {
            Companion.intent = intent
        }
        getSensorUserPermissions()
        if (!temperatureAllowed && !pressureAllowed && !humidityAllowed) {
            Log.d(TAG, "Sensors not allowed")
            stopSelf()
        }
        if (wifiOnly && !isConnectedToWifi) {
            Log.d(TAG, "Not connected to Wi-Fi")
            stopSelf()
        }
        val sensors = getSensors()
        Log.d(
            TAG,
            "Temperature: ${sensors.first}, Pressure: ${sensors.second}, Humidity: ${sensors.third}"
        )
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
                    if (temperatureCelsius in MIN_TEMPERATURE..MAX_TEMPERATURE) {
                        temperatureReceived = temperatureCelsius
                        SensorsDataHelper.updateTemperatureData(temperatureCelsius)
                        temperatureUpdated = true
                        Log.d(TAG, "Received temperature: $temperatureCelsius")
                        sensorManager?.unregisterListener(this, it.sensor)
                    } else {
                        Log.d(TAG, "Received invalid temperature: $temperatureCelsius")
                    }
                }

                Sensor.TYPE_PRESSURE -> {
                    val pressure = it.values[0]
                    pressureReceived = pressure
                    SensorsDataHelper.updatePressureData(pressure)
                    pressureUpdated = true
                    Log.d(TAG, "Received pressure: $pressure")
                    sensorManager?.unregisterListener(this, it.sensor)
                }

                Sensor.TYPE_RELATIVE_HUMIDITY -> {
                    val humidity = it.values[0]
                    humidityReceived = humidity
                    SensorsDataHelper.updateHumidityData(humidity)
                    humidityUpdated = true
                    Log.d(TAG, "Received humidity: $humidity")
                    sensorManager?.unregisterListener(this, it.sensor)
                }

                else -> {
                    Log.d(TAG, "Unknown sensor type")
                }
            }
            if (
                (temperatureUpdated || !temperatureAllowed) &&
                (pressureUpdated || !pressureAllowed) &&
                (humidityUpdated || !humidityAllowed)
            ) {
                val latitude = intent.getDoubleExtra("latitude", 0.0)
                val longitude = intent.getDoubleExtra("longitude", 0.0)
                uploadDataToCloud(
                    latitude,
                    longitude,
                    temperatureReceived,
                    pressureReceived,
                    humidityReceived
                )
                temperatureUpdated = false
                pressureUpdated = false
                humidityUpdated = false
            }
        }
    }

    private fun uploadDataToCloud(
        latitude: Double,
        longitude: Double,
        temperature: Float?,
        pressure: Float?,
        humidity: Float?
    ) {
        if (auth.currentUser == null) {
            Log.d(TAG, "User not authenticated")
            stopSelf()
        }
        val username = auth.currentUser!!.displayName ?: "Unknown user"
        // Upload data to cloud
        val data = hashMapOf(
            "sync_cords" to GeoPoint(latitude, longitude),
            "temperature" to temperature,
            "pressure" to pressure,
            "humidity" to humidity,
            "timestamp" to System.currentTimeMillis(),
            "username" to username,
            "uid" to auth.currentUser?.uid.toString(),
        )
        db.collection("environment_sensors_data")
            .add(data)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
                val updatedNotification = buildNotification("Data uploaded to cloud")
                val notificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(NOTIFICATION_ID, updatedNotification)
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
                val errorNotification = buildNotification("Error uploading data to cloud")
                val notificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(NOTIFICATION_ID, errorNotification)
            }
            .addOnCompleteListener {
                stopSelf()
            }
    }

    private fun buildNotification(text: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Environment Sensors Service")
            .setContentText(text)
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy()")
        sensorManager?.unregisterListener(this)
        handler?.removeCallbacks(timeoutRunnable!!)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Log.d(TAG, "onTaskRemoved()")
    }
}
