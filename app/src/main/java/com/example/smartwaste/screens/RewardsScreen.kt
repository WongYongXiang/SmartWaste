package com.example.smartwaste.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
    ){
        Text(text = "Available Rewards", fontSize = 24.sp, modifier = Modifier.padding(bottom=16.dp))

        rewards.forEach { reward ->
            Card(
                onClick = {
                    if (userPoints >= reward.cost) {
                        selectedReward = reward
                    } else {
                        showInsufficientPoints =true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ){
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Text(text= reward.title, fontSize = 18.sp)
                    Text(text= "${reward.cost} pts", color = MaterialTheme.colorScheme.primary)
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
                        Firebase.firestore.collection("users").document(uid)
                            .update("points", com.google.firebase.firestore.FieldValue.increment(-selectedReward!!.cost.toLong()))

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