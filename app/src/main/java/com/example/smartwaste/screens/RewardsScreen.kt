package com.example.smartwaste.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

data class RewardItem(val title: String, val cost: Int)

@Composable
fun RewardScreen(userPoints: Int) {
    val rewards = listOf(
        RewardItem("$5 FairPrice Voucher", 5000),
        RewardItem("$5 Capitaland Voucher", 5000),
        RewardItem("$10 FairPrice Voucher", 10000),
        RewardItem("$10 Capitaland Voucher", 10000)
    )

    var selectedReward by remember { mutableStateOf<RewardItem?>(null) }
    var showInsufficientPoints by remember {mutableStateOf(false)}
    val context = LocalContext.current

    Column(modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFFFAFCFA))
        .padding(16.dp)
    ){
        Text(text = "Available Rewards",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1B5E20),
            modifier = Modifier
                .padding(bottom=16.dp, top = 8.dp))

        rewards.forEach { reward ->
            Card(
                onClick = {
                    if (userPoints >= reward.cost) {
                        selectedReward = reward
                    } else {
                        showInsufficientPoints =true
                    }
                },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)

            ){
                Row(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f).padding(end=8.dp)
                    ){
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Reward",
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text= reward.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0XFF1B5E20),
                            maxLines = 2
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF2E7D32).copy(alpha=0.1f)
                    ){
                        Text(
                            text= "${reward.cost} pts",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
    if (selectedReward !=null) {
        AlertDialog(
            onDismissRequest = {selectedReward = null},
            title = {Text(text ="Claim Voucher?")},
            text = {Text(text= "Are you sure you want to sepnd ${selectedReward!!.cost} points on ${selectedReward!!.title}?")},
            confirmButton = {
                Button(onClick = {
                    val uid = Firebase.auth.currentUser?.uid
                    if(uid != null) {
                        val claimedRewardName = selectedReward!!.title
                        Firebase.firestore.collection("users").document(uid)
                            .update("points", com.google.firebase.firestore.FieldValue.increment(-selectedReward!!.cost.toLong()))
                            .addOnSuccessListener {
                                Toast.makeText(context, "$claimedRewardName claimed!", Toast.LENGTH_SHORT).show()
                            }

                        val logData = hashMapOf(
                            "title" to "Claimed ${selectedReward!!.title}",
                            "amount" to -selectedReward!!.cost,
                            "date" to System.currentTimeMillis()
                        )
                        Firebase.firestore.collection("users").document(uid).collection("logs").add(logData)
                    }
                    selectedReward = null
                }) { Text("Yes")}
            },
            dismissButton = {
                OutlinedButton(onClick={selectedReward = null}) { Text("No")}
            }
        )
    }
    if (showInsufficientPoints) {
        AlertDialog(
            onDismissRequest = {showInsufficientPoints = false},
            title = {Text("Not Enough Points")},
            text = {Text("You need to earn more points to claim this reward")},
            confirmButton = {Button(onClick= { showInsufficientPoints = false}) {Text("OK")}}
        )
    }
}