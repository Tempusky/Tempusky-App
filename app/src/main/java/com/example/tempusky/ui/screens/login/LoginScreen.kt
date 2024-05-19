package com.example.tempusky.ui.screens.login

import android.Manifest
import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tempusky.MainActivity
import com.example.tempusky.MainViewModel
import com.example.tempusky.R
import com.example.tempusky.data.SettingsDataStore
import com.example.tempusky.domain.appNavigation.NavigationRoutes
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(context: MainActivity, navController: NavController, mainViewModel: MainViewModel) {
    val auth: FirebaseAuth = Firebase.auth
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isButtonEnabled by remember { mutableStateOf(false) }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var wifiOnly by remember {
        mutableStateOf(false)
    }
    var isConnectedToWifi by remember {
        mutableStateOf(false)
    }
    LaunchedEffect(Unit) {
        val settingsDataStore = SettingsDataStore(context)
        wifiOnly = settingsDataStore.getNetwork.first() == "Wi-Fi"
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        isConnectedToWifi =
            networkInfo != null && networkInfo.isConnected && networkInfo.type == android.net.ConnectivityManager.TYPE_WIFI
        if (wifiOnly && !isConnectedToWifi) {
            Toast.makeText(context, "Please connect to Wi-Fi to login", Toast.LENGTH_SHORT).show()
        } else {
            auth.currentUser?.let {
                mainViewModel.setBottomBarVisible(true)
                navController.navigate(NavigationRoutes.HOME)
            }
        }
    }
    val googleSignInLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    if (wifiOnly && !isConnectedToWifi) {
                        Toast.makeText(
                            context,
                            "Please connect to Wi-Fi to login",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@rememberLauncherForActivityResult
                    }
                    // Google Sign In was successful, authenticate with Firebase
                    task.result?.idToken?.let { idToken ->
                        val credential = GoogleAuthProvider.getCredential(idToken, null)
                        auth.signInWithCredential(credential)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Log.d(TAG, "signInWithCredential:success")
                                    val user = auth.currentUser!!
                                    if (user.displayName == null) {
                                        Toast.makeText(
                                            MainActivity.context,
                                            "No user found, use another account or signup first",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            MainActivity.context,
                                            "Hello again ${user.displayName}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        MainActivity.locationPermissionLauncher.launch(
                                            arrayOf(
                                                Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.ACCESS_COARSE_LOCATION
                                            )
                                        )
                                        mainViewModel.setBottomBarVisible(true)
                                        navController.navigate(NavigationRoutes.HOME)
                                    }
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                                    Toast.makeText(
                                        MainActivity.context,
                                        "Authentication failed.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    }
                } catch (e: ApiException) {
                    // Google Sign In failed, update UI appropriately
                    Log.w(TAG, "Google sign in failed", e)
                    Toast.makeText(
                        MainActivity.context,
                        "Google sign in failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .align(Alignment.CenterHorizontally), contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Tempusky Logo"
                    )
                    Spacer(modifier = Modifier.fillMaxSize(0.1f))
                    Text(text = "TEMPUSKY", fontSize = 40.sp, fontWeight = FontWeight.Black)
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(0.8f),
                verticalArrangement = Arrangement.SpaceAround,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Login with your credentials:",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                OutlinedTextField(
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
                        isButtonEnabled = email.isNotEmpty() && password.isNotEmpty()
                    },
                    label = { Text("Email address") },
                    placeholder = { Text(text = "Enter your e-mail") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
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
                    value = password,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "passwordIcon"
                        )
                    },
                    onValueChange = {
                        password = it
                        isButtonEnabled = email.isNotEmpty() && password.isNotEmpty()
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ),
                    label = { Text(text = "Password") },
                    maxLines = 1,
                    placeholder = { Text(text = "Enter your password") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        val image = if (passwordVisible)
                            Icons.Filled.Visibility
                        else Icons.Filled.VisibilityOff

                        // Please provide localized description for accessibility services
                        val description = if (passwordVisible) "Hide password" else "Show password"

                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, description)
                        }
                    }
                )
            }
            Spacer(modifier = Modifier.fillMaxSize(0.05f))
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    modifier = Modifier.fillMaxWidth(0.8f),
                    onClick = {
                        val signInIntent = GoogleSignIn.getClient(
                            MainActivity.context,
                            MainActivity.gso
                        ).signInIntent
                        googleSignInLauncher.launch(signInIntent)
                    },
                    shape = MaterialTheme.shapes.medium,
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                    colors = ButtonDefaults.buttonColors(
                        contentColor = MaterialTheme.colorScheme.onBackground,
                        containerColor = Color.Transparent
                    ),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "CONTINUE WITH",
                            fontSize = 20.sp,
                            modifier = Modifier.padding(end = 10.dp)
                        )
                        Image(
                            painter = painterResource(id = R.drawable.googlecon),
                            contentDescription = "Google Icon",
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp), horizontalArrangement = Arrangement.SpaceAround
                ) {
                    OutlinedButton(
                        onClick = { navController.navigate(NavigationRoutes.SIGNUP) },
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                        colors = ButtonDefaults.buttonColors(
                            contentColor = MaterialTheme.colorScheme.onBackground,
                            containerColor = Color.Transparent
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(text = "SIGNUP", fontSize = 20.sp)
                    }

                    Button(
                        onClick = {
                            if (wifiOnly && !isConnectedToWifi) {
                                Toast.makeText(
                                    context,
                                    "Please connect to Wi-Fi to login",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                auth.signInWithEmailAndPassword(email, password)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            val user = auth.currentUser!!
                                            Toast.makeText(
                                                MainActivity.context,
                                                "Hello again ${user.displayName}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            MainActivity.locationPermissionLauncher.launch(
                                                arrayOf(
                                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                                )
                                            )
                                            mainViewModel.setBottomBarVisible(true)
                                            navController.navigate(NavigationRoutes.HOME)
                                        } else {
                                            // If sign in fails, display a message to the user.
                                            Toast.makeText(
                                                MainActivity.context,
                                                "Authentication failed.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                            }
                        },
                        enabled = isButtonEnabled,
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(
                            contentColor = MaterialTheme.colorScheme.onBackground,
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                    ) {
                        Text(text = "LOGIN", fontSize = 20.sp)
                    }
                }
                OutlinedButton(
                    onClick = { navController.navigate(NavigationRoutes.FORGOT_PASSWORD) },
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        contentColor = MaterialTheme.colorScheme.onBackground,
                        containerColor = Color.Transparent
                    ),
                ) {
                    Text(
                        text = "Forgot password?",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier.padding(start = 10.dp)
                    )
                }
            }
        }
    }
}

private const val TAG = "LoginScreen"
