package com.example.tempusky.ui.screens.profile

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import androidx.navigation.NavController
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.tempusky.MainActivity
import com.example.tempusky.R
import com.example.tempusky.core.broadcastReceivers.LocationUpdatesReceiver
import com.example.tempusky.core.broadcastReceivers.LocationUpdatesReceiver.Companion.ACTION_PROCESS_UPDATES
import com.example.tempusky.core.helpers.Utils
import com.example.tempusky.data.SearchDataResult
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@Composable
fun ProfileScreen(context: Context, navController: NavController) {
    val db = Firebase.firestore
    val auth = Firebase.auth
    val username = if (!auth.currentUser?.displayName.isNullOrBlank()) auth.currentUser?.displayName.toString() else "Unknown"
    var contributions by remember { mutableStateOf(listOf<SearchDataResult>())}
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    var showDialog by remember { mutableStateOf(false) }

    val photoUrl = if (auth.currentUser?.photoUrl != null) auth.currentUser?.photoUrl.toString() else stringResource(R.string.default_profile_picture)
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(photoUrl)
            .size(100, 100)
            .crossfade(true)
            .build()
    )

    LaunchedEffect(Unit) {
        db.collection("environment_sensors_data").get()
            .addOnSuccessListener { result ->
                val tempList = mutableListOf<SearchDataResult>()
                for (document in result) {
                    if (document.data["uid"].toString() == auth.currentUser?.uid) {
                        val tempData = SearchDataResult(
                            document.data["username"].toString(),
                            document.data["location"].toString(),
                            document.data["temperature"]?.toString()?.toDouble(),
                            document.data["humidity"]?.toString()?.toDouble(),
                            document.data["pressure"]?.toString()?.toDouble(),
                            Utils.timestampToDate(document.data["timestamp"].toString())
                        )
                        tempList.add(tempData)
                    }
                }
                tempList.sortByDescending { it.date }
                contributions = tempList
            }
    }

    Box(modifier = Modifier
        .fillMaxHeight(0.93f)
        .fillMaxWidth()){
        if (showDialog) {
            ProfilePictureUrlInput(
                onDismiss = { showDialog = false },
                onSubmit = { url ->
                    val profileUpdate = userProfileChangeRequest {
                        photoUri = url.toUri()
                    }
                    auth.currentUser?.updateProfile(profileUpdate)
                        ?.addOnSuccessListener {
                            Toast.makeText(context, "Profile picture updated", Toast.LENGTH_SHORT).show()
                        }
                        ?.addOnFailureListener {
                            Toast.makeText(context, "Failed to update profile picture", Toast.LENGTH_SHORT).show()
                        }
                        ?.addOnCompleteListener {
                            showDialog = false
                        }
                }
            )
        }
        Column(modifier = Modifier.fillMaxHeight(), verticalArrangement = Arrangement.SpaceBetween, horizontalAlignment = Alignment.CenterHorizontally) {
            Column(horizontalAlignment = Alignment.End, modifier = Modifier
                .padding(10.dp)
                .weight(1f)) {
                IconButton(onClick = { navController.navigate("settings") }) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings Icon", Modifier.size(30.dp))
                }
                Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceAround) {
                    when (painter.state) {
                        is AsyncImagePainter.State.Loading -> CircularProgressIndicator(modifier = Modifier.size(100.dp))
                        is AsyncImagePainter.State.Error -> Icon(imageVector = Icons.Default.Error, contentDescription = "Error profile picture", modifier = Modifier.size(100.dp).clip(CircleShape).clickable { showDialog = true })
                        else -> Image(painter = painter, contentScale = ContentScale.Fit, modifier = Modifier.size(100.dp).clickable { showDialog = true }, contentDescription = "Profile picture")
                    }
                    Column(modifier = Modifier.fillMaxHeight(), verticalArrangement = Arrangement.Center) {
                        Text(text = username, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        auth.currentUser?.email?.let { Text(text = it, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    }
                }
            }
            Row(modifier = Modifier
                .fillMaxWidth()
                .weight(0.5f), horizontalArrangement = Arrangement.SpaceAround) {
                Text(text = "Last Contribution: \n${contributions.firstOrNull()?.date}", fontSize = 17.sp, fontWeight = FontWeight.Normal)
                Button(
                    onClick = {
                        if (ActivityCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            ActivityCompat.requestPermissions(
                                context as MainActivity,
                                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                                1)
                        }else{
                            fusedLocationClient.lastLocation
                                .addOnSuccessListener { location: Location? ->
                                    if (location != null) {
                                        sendLocation(context as MainActivity, location)
                                        Toast.makeText(context, "Sending data started", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Failed to get location.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Log.e("ProfileScreen", "Failed to get location", e)
                                    Toast.makeText(context, "Failed to get location. Enable location on device.", Toast.LENGTH_SHORT).show()
                                }
                        }

                    },
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        contentColor = MaterialTheme.colorScheme.onBackground,
                        containerColor = MaterialTheme.colorScheme.primary),
                ) {
                    Text(text = "SEND DATA", fontSize = 20.sp)
                }
            }
            LazyColumn(modifier = Modifier
                .fillMaxWidth(0.95f)
                .weight(4f)) {
                item(1)
                {
                    Text(text = "Your contributions:", fontSize = 20.sp, fontWeight = FontWeight.Normal)
                }
                items(contributions.size) {
                    DataSentItem(contributions[it])
                }
            }
        }
    }
}

private fun sendLocation(context: MainActivity, location: Location) {
    Log.d("ProfileScreen", "Location: $location")
    val intent = Intent(context, LocationUpdatesReceiver::class.java)
    intent.action = ACTION_PROCESS_UPDATES
    intent.putExtra("location", location)

    try {
        context.sendBroadcast(intent)
    } catch (e: PendingIntent.CanceledException) {
        e.printStackTrace()
    }
}

@Composable
fun DataSentItem(contribution: SearchDataResult){
    Box(modifier = Modifier
        .fillMaxSize()
        .padding(10.dp)
        .border(2.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small)){
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = contribution.location, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            Text(text = "Temperature: ${contribution.temperature}", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(text = "Humidity: ${contribution.humidity}", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(text = "Pressure: ${contribution.pressure}", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(text = contribution.date, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun ProfilePictureUrlInput(
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
){
    var url by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Enter Profile Picture URL") },
        text = {
            Column {
                Text(text = "Enter the URL of the image you want to use as your profile picture.")
                Spacer(modifier = Modifier.size(10.dp))
                TextField(value = url, onValueChange = { url = it }, singleLine = true)
            }
        },
        confirmButton = {
            Button(onClick = {
                onSubmit(url)
            }, enabled = url.isValidUri()) {
                Text(text = "Submit")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        })

}

private fun String.isValidUri(): Boolean {
    return try {
        val uri = toUri()
        uri.scheme != null && uri.host != null && uri.path != null
    } catch (e: Exception) {
        false
    }
}
