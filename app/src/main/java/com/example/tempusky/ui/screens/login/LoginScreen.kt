package com.example.tempusky.ui.screens.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tempusky.domain.appNavigation.NavigationRoutes

@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isButtonEnabled by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()){
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceEvenly) {
            Box(modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .align(Alignment.CenterHorizontally), contentAlignment = Alignment.Center) {
                Text(text = "TEMPUSKY", fontSize = 40.sp, fontWeight = FontWeight.Black)
            }

            Column(modifier = Modifier
                .weight(1f)
                .fillMaxSize(), verticalArrangement = Arrangement.SpaceAround, horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Login with your credentials:", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = email,
                    leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = "emailIcon") },
                    onValueChange = {
                        email = it
                        isButtonEnabled = email.isNotEmpty() && password.isNotEmpty()
                    },
                    label = {"Email address" },
                    placeholder = { "Enter your e-mail" },
                )
                OutlinedTextField(
                    value = password,
                    leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = "passwordIcon") },
                    onValueChange = {
                        password = it
                        isButtonEnabled = email.isNotEmpty() && password.isNotEmpty()
                    },
                    label = {"Password" },
                    placeholder = { "Enter your password" },
                )
            }
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    OutlinedButton(onClick = { navController.navigate(NavigationRoutes.SIGNUP) },
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                        colors = ButtonDefaults.buttonColors(
                            contentColor = MaterialTheme.colorScheme.onBackground,
                            containerColor = Color.Transparent),
                        shape = MaterialTheme.shapes.medium) {
                        Text(text = "SIGNUP", fontSize = 20.sp)
                    }

                    Button(
                        onClick = { navController.navigate(NavigationRoutes.HOME) },
                        enabled = isButtonEnabled,
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(
                            contentColor = MaterialTheme.colorScheme.onBackground,
                            containerColor = MaterialTheme.colorScheme.primary),
                    ) {
                        Text(text = "LOGIN", fontSize = 20.sp)
                    }
                }
            }
        }
    }
}
