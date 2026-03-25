package com.example.smartwaste.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.*

data class TransactionLog(val title:String, val amount: Int, val date: Long)

@Composable
fun LogsScreen(){
    var logs by remember {mutableStateOf<List<TransactionLog>>(emptyList())}

    LaunchedEffect(Unit){
        val uid = Firebase.auth.currentUser?.uid
        if(uid != null){
            Firebase.firestore.collection("users").document(uid).collection("logs")
                .orderBy("date", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot,  _ ->
                    if (snapshot != null){
                        logs = snapshot.documents.map { doc ->
                            TransactionLog(
                                title = doc.getString("title") ?: "",
                                amount = doc.getLong("amount")?.toInt() ?: 0,
                                date = doc.getLong("date") ?: 0L
                            )
                        }
                    }
                }
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
    ){
        Text(text= "Transaction History", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn{
            items(logs) { log ->
                val formatter = SimpleDateFormat("dd MM yyyy, hh:mm a", Locale.getDefault())
                val dateString = formatter.format(Date(log.date))
                val amountColor = if (log.amount>0) Color(0xFF4CAF50) else Color(0xFFD32F2F)
                val amountPrefix = if (log.amount>0) "+" else ""

                Card(modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                ){
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ){
                        Column{
                            Text(text=log.title, fontWeight = FontWeight.Bold)
                            Text(text=dateString, style = MaterialTheme.typography.bodySmall)
                        }
                        Text(
                            text = "$amountPrefix${log.amount} pts",
                            color = amountColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

}