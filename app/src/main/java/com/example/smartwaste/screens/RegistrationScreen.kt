package com.example.smartwaste.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.auth.userProfileChangeRequest

@Composable
fun RegistrationScreen(onRegistrationSuccess: () -> Unit){
    var email by remember { mutableStateOf("")}
    var username by remember { mutableStateOf("")}
    var password by remember { mutableStateOf("")}
    var errorMessage by remember { mutableStateOf<String?>(null)}
    var isLoginMode by remember { mutableStateOf(true)}
    val auth = Firebase.auth

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text =  if (isLoginMode) "Login" else " Create an account",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (!isLoginMode) {
            OutlinedTextField(
                value = username,
                onValueChange = { username = it},
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        OutlinedTextField(
            value = email,
            onValueChange = { email = it},
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it},
            label = { Text("Password")},
            modifier = Modifier.fillMaxWidth()
        )

        if (errorMessage != null) {
            Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    errorMessage = "Please fill in all fields."
                } else {
                    if (isLoginMode) {
                        // Login
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    onRegistrationSuccess() // Go to Main Screen
                                } else {
                                    errorMessage = task.exception?.message
                                }
                            }
                    } else {
                        // Registration
                        if (username.isBlank()) {
                            errorMessage = "Please enter username"
                        }
                        else if (password.length < 6) {
                            errorMessage = "Password must be at least 6 characters."
                        } else {
                            auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val user = auth.currentUser
                                        val profileUpdates = userProfileChangeRequest{
                                            displayName = username
                                        }
                                        user?.updateProfile(profileUpdates)
                                            ?.addOnCompleteListener {
                                                onRegistrationSuccess// Go to Main Screen
                                            }
                                    } else {
                                        errorMessage = task.exception?.message
                                    }
                                }
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text(if (isLoginMode) "Login" else "Register")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = {
            isLoginMode = !isLoginMode // Switch modes
            errorMessage = null // Clear old errors
        }) {
            Text(
                text = if (isLoginMode) "Don't have an account? Register" else "Already have an account? Login",
                fontSize = 16.sp
            )
        }
    }
}