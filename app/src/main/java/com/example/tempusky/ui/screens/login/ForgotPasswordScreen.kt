package com.example.tempusky.ui.screens.login

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.lang.Thread.sleep

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(navController: NavController) {

    var email by remember { mutableStateOf("") }
    var confirmEmail by remember { mutableStateOf("") }
    var isConfirmEmailEnabled by remember { mutableStateOf(false) }
    var isButtonEnabled by remember { mutableStateOf(false) }

    fun sendPasswordResetEmail() {
        Firebase.auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Email sent.
                    Toast.makeText(
                        navController.context,
                        "Password reset email sent",
                        Toast.LENGTH_SHORT
                    ).show()
                    sleep(1000)
                    navController.popBackStack()
                } else {
                    Toast.makeText(
                        navController.context,
                        "Error sending email: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Forgot Password", fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.fillMaxSize(0.1f))
            Text(text = "Enter your email:", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    errorContainerColor = Color.Transparent,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                    focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onBackground,
                    focusedPlaceholderColor = Color.LightGray,
                    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onBackground,
                ),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next
                ),
                maxLines = 1,
                value = email,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "emailIcon"
                    )
                },
                onValueChange = {
                    email = it
                    isConfirmEmailEnabled =
                        email.isNotEmpty() && email.contains("@") && email.contains(".")
                },
                label = { Text("Email address") },
                placeholder = { Text(text = "Enter your e-mail") },
                modifier = Modifier.fillMaxWidth(0.9f)
            )
            Spacer(modifier = Modifier.fillMaxSize(0.05f))
            OutlinedTextField(
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    errorContainerColor = Color.Transparent,
                    selectionColors = LocalTextSelectionColors.current,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                    focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onBackground,
                    focusedPlaceholderColor = Color.LightGray,
                    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onBackground,
                ),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                maxLines = 1,
                value = confirmEmail,
                enabled = isConfirmEmailEnabled,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "emailIcon"
                    )
                },
                onValueChange = {
                    confirmEmail = it
                    isButtonEnabled = confirmEmail == email
                },
                label = { Text("Confirm Email address") },
                placeholder = { Text(text = "Confirm the email address") },
                modifier = Modifier.fillMaxWidth(0.9f)
            )
            Spacer(modifier = Modifier.fillMaxSize(0.05f))
            Button(
                onClick = { sendPasswordResetEmail() },
                enabled = isButtonEnabled,
            ) {
                Text(text = "Send Password Reset Email", fontSize = 20.sp)
            }

        }
    }
}