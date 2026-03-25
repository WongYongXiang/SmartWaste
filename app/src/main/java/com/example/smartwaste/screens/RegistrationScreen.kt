package com.example.smartwaste.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.firestore

@Composable
fun RegistrationScreen(onRegistrationSuccess: () -> Unit){
    var email by remember { mutableStateOf("")}
    var username by remember { mutableStateOf("")}
    var password by remember { mutableStateOf("")}
    var passwordVisible by remember { mutableStateOf(false)}
    var confirmPassword by remember { mutableStateOf("")}
    var confirmPasswordVisible by remember { mutableStateOf(false)}
    var errorMessage by remember { mutableStateOf<String?>(null)}
    var isLoginMode by remember { mutableStateOf(true)}
    val auth = Firebase.auth
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFCFA))
            .pointerInput(Unit){
                detectTapGestures(onTap ={
                    focusManager.clearFocus()
                })
            }
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text =  if (isLoginMode) "Welcome Back!" else " Join SmartWaste today!",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1B5E20)
        )
        Text(
            text =  if (isLoginMode) "Login" else " Create an account",
            fontSize = 16.sp,
            color = Color(0xFF2E7D32),
            modifier = Modifier.padding(bottom = 32.dp)
        )

        val textFieldColors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF2E7D32),
            focusedLabelColor = Color(0xFF2E7D32),
            cursorColor = Color(0xFF2E7D32),
            unfocusedContainerColor = Color.White,
            focusedContainerColor = Color.White,
            focusedTextColor = Color(0xFF1B5E20),
            unfocusedTextColor = Color.DarkGray
        )

        if (!isLoginMode) {
            OutlinedTextField(
                value = username,
                onValueChange = { username = it},
                label = { Text("Username") },
                colors = textFieldColors,
                modifier = Modifier.fillMaxWidth().padding(bottom =8.dp)
            )
        }
        OutlinedTextField(
            value = email,
            onValueChange = { email = it},
            label = { Text("Email") },
            colors = textFieldColors,
            modifier = Modifier.fillMaxWidth().padding(bottom =8.dp)
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it},
            label = { Text("Password")},
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                TextButton(onClick = {passwordVisible = !passwordVisible}){
                    Text(text = if (passwordVisible) "Hide" else "Show", color =Color(0xFF2E7D32))
                }
            },
            colors = textFieldColors,
            modifier = Modifier.fillMaxWidth().padding(bottom =8.dp)
        )
        if (!isLoginMode) {
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {confirmPassword = it},
                label = {Text("Confirm Password")},
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    TextButton(onClick = {confirmPasswordVisible = !confirmPasswordVisible}){
                        Text(text = if (confirmPasswordVisible) "Hide" else "Show", color =Color(0xFF2E7D32))
                    }
                },
                colors = textFieldColors,
                modifier = Modifier.fillMaxWidth().padding(bottom =8.dp)
            )
        }

        if (errorMessage != null) {
            Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical =8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                focusManager.clearFocus()
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
                        else if (password != confirmPassword) {
                            errorMessage = "Passwords do not match"
                        }
                        else if (password.length < 6) {
                            errorMessage = "Password must be at least 6 characters"
                        } else {
                            auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val user = auth.currentUser
                                        val db = Firebase.firestore
                                        val userData = hashMapOf(
                                            "username" to username,
                                            "email" to email,
                                            "role" to "public" // Default the user role to public (can go to firebase console to change the roles)
                                        )

                                        user?.uid?.let { uid ->
                                            db.collection("users").document(uid)
                                                .set(userData)
                                                .addOnSuccessListener {
                                                    val profileUpdates = userProfileChangeRequest{
                                                        displayName = username
                                                    }
                                                    user.updateProfile(profileUpdates)
                                                        .addOnCompleteListener {
                                                            onRegistrationSuccess()// Go to Main Screen
                                                        }
                                                }
                                                .addOnFailureListener { e ->
                                                    errorMessage = "user data save failed: ${e.message}"
                                                }
                                        }


                                    } else {
                                        errorMessage = task.exception?.message
                                    }
                                }
                        }
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2E7D32),
                contentColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth().height(56.dp)
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
                fontSize = 16.sp,
                color = Color(0xFF2E7D32)
            )
        }
    }
}