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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import com.example.tempusky.MainActivity
import com.example.tempusky.R
import com.example.tempusky.core.broadcastReceivers.LocationUpdatesReceiver
import com.example.tempusky.core.broadcastReceivers.LocationUpdatesReceiver.Companion.ACTION_PROCESS_UPDATES
import com.example.tempusky.core.helpers.Utils
import com.example.tempusky.data.SearchDataResult
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@Composable
fun ProfileScreen(context: Context, navController: NavController) {
    val db = Firebase.firestore
    val auth = Firebase.auth
    val username = auth.currentUser?.displayName ?: "Display Name not set"
    var contributions by remember { mutableStateOf(listOf<SearchDataResult>())}
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

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
        Column(modifier = Modifier.fillMaxHeight(), verticalArrangement = Arrangement.SpaceBetween, horizontalAlignment = Alignment.CenterHorizontally) {
            Column(horizontalAlignment = Alignment.End, modifier = Modifier
                .padding(10.dp)
                .weight(1f)) {
                IconButton(onClick = { navController.navigate("settings") }) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings Icon", Modifier.size(30.dp))
                }
                Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceAround) {
                    Image(painter = painterResource(id = R.drawable.pfp27), contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape), contentDescription = "profile picture")
                    Column(modifier = Modifier.fillMaxHeight(), verticalArrangement = Arrangement.Center) {
                        Text(text = username, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        auth.currentUser?.email?.let { Text(text = it, fontSize = 15.sp, fontWeight = FontWeight.SemiBold) }
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