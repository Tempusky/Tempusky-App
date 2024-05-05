import android.content.Context
import android.location.Location
import androidx.core.app.ActivityCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.FusedLocationProviderClient
import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class DataCollectionWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(applicationContext)

    override suspend fun doWork(): Result {
        return if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                val location = getCurrentLocation()
                Log.d("DataCollectionWorker", "Location: $location")
                Result.success()
            } catch (e: Exception) {
                Result.failure()
            }
        } else {
            Log.d("DataCollectionWorker", "Location permission not granted")
            Result.failure()
        }
    }

    private suspend fun getCurrentLocation(): Location? = suspendCancellableCoroutine { continuation ->
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            continuation.resume(location)
        }.addOnFailureListener { exception ->
            continuation.resumeWithException(exception)
        }
    }
}
