package com.example.smartwaste.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun ProfileScreen(onLogout: () -> Unit) {
    val auth = Firebase.auth
    val user = auth.currentUser
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFCFA))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(Color(0xFFE8F5E9), CircleShape),
            contentAlignment = Alignment.Center
        ){
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Profile",
                modifier = Modifier.size(80.dp),
                tint = Color(0xFF2E7D32)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = user?.displayName ?: "Nameless Warrier",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1B5E20)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = user?.email ?: "No email provided",
            fontSize = 16.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(48.dp))

        OutlinedButton(
            onClick = {
                user?.email?.let { email ->
                    auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(context, "Password reset email sent", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF2E7D32))
        ) {
            Text("Change password", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onLogout,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFEBEE),
                contentColor = Color(0xFFC62828)
            )
        ) {
            Text("Logout", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }

}
