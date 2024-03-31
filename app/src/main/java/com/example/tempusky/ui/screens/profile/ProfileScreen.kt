package com.example.tempusky.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tempusky.R
import com.example.tempusky.domain.appNavigation.NavigationRoutes

@Composable
fun ProfileScreen() {
    Box(modifier = Modifier.fillMaxSize()){
        Column(modifier = Modifier.fillMaxHeight(), verticalArrangement = Arrangement.SpaceBetween, horizontalAlignment = Alignment.CenterHorizontally) {
            Column(horizontalAlignment = Alignment.End, modifier = Modifier
                .padding(10.dp)
                .weight(1f)) {
                Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings Icon", Modifier.size(30.dp))
                Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceAround) {
                    Image(painter = painterResource(id = R.drawable.pfp27), contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape), contentDescription = "profile picture")
                    Column(modifier = Modifier.fillMaxHeight(), verticalArrangement = Arrangement.Center) {
                        Text(text = "Username", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = "Email", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Row(modifier = Modifier
                .fillMaxWidth()
                .weight(0.5f), horizontalArrangement = Arrangement.SpaceAround) {
                Text(text = "Last Apportation: \n21/03/2024 - 14:23")
                Button(
                    onClick = {  },
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        contentColor = MaterialTheme.colorScheme.onBackground,
                        containerColor = MaterialTheme.colorScheme.primary),
                ) {
                    Text(text = "SEND DATA", fontSize = 20.sp)
                }
            }
            LazyColumn(modifier = Modifier
                .background(Color.Red)
                .weight(4f)){
                items(1){
                    Text(text = "Your Data Sent:", fontSize = 20.sp)
                }
                items(10){
                    Text(text = "Item $it", fontSize = 20.sp)
                }
            }
        }
    }
}