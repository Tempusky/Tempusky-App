package com.example.tempusky.ui.screens.search

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen() {

    var inputData by remember { mutableStateOf("") }

    Box(modifier = Modifier
        .fillMaxHeight(0.93f)
        .fillMaxWidth()){
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)) {
            Text(text = "Search for data here!", fontSize = 25.sp, fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .padding(top = 20.dp)
                    .fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onBackground,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onBackground,
                    focusedPlaceholderColor = Color.LightGray
                ),
                value = inputData,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "locationIcon"
                    )
                },
                onValueChange = {
                    inputData = it
                },
                placeholder = { Text(text = "Enter the location") },
            )
            LazyColumn(modifier = Modifier.fillMaxSize().padding(10.dp)) {
                item(1)
                {
                    Text(text = "Data received near you:", fontSize = 20.sp, fontWeight = FontWeight.Normal)
                }
                items(20)
                {
                    DataReceivedItem()
                }
            }
        }
    }
}

@Composable
fun DataReceivedItem(){
    Box(modifier = Modifier.fillMaxSize().padding(10.dp).border(2.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small)){
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = "25ºC / 70%", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            Text(text = "Location: Roselló, Lleida", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(text = "Date: 02-04-2023 | 12:34", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}