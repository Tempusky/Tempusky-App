package com.example.tempusky.ui.screens.search

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tempusky.MainActivity
import com.example.tempusky.data.SearchDataResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(context: MainActivity, searchViewModel: SearchViewModel) {

    var inputData by remember { mutableStateOf("") }
    var results by remember { mutableStateOf(listOf<SearchDataResult>()) }
    searchViewModel.searchDataResult.observe(context) {
        results = it
    }

    LaunchedEffect(Unit) {
        searchViewModel.updateSearchDataResult("")
    }

    Box(
        modifier = Modifier
            .fillMaxHeight(0.93f)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Text(text = "Search for data here!", fontSize = 25.sp, fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .padding(top = 20.dp)
                    .fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    errorContainerColor = Color.Transparent,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    selectionColors = LocalTextSelectionColors.current,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                    focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onBackground,
                    focusedPlaceholderColor = Color.LightGray,
                    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onBackground,
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
                    searchViewModel.updateSearchDataResult(inputData)
                },
                placeholder = { Text(text = "Enter the location") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Text,
                    imeAction = androidx.compose.ui.text.input.ImeAction.Done
                )
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 10.dp)
            ) {
                item(1)
                {
                    Text(
                        text = "Data received near you:",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
                items(results.size)
                {
                    DataReceivedItem(results[it])
                }
            }
        }
    }
}

@Composable
fun DataReceivedItem(result: SearchDataResult) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
            .border(2.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = result.location, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            Text(
                text = "Temperature: ${result.temperature}",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Humidity: ${result.humidity}",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Pressure: ${result.pressure}",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(text = result.date, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}