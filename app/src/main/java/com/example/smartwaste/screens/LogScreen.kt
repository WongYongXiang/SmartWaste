package com.example.smartwaste.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        .background(Color(0xFFFAFCFA))
        .padding(16.dp)
    ){
        Text(text= "Transaction History",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1B5E20),
            modifier = Modifier
                .padding(bottom=16.dp, top = 8.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn{
            items(logs) { log ->
                val formatter = SimpleDateFormat("dd MM yyyy, hh:mm a", Locale.getDefault())
                val dateString = formatter.format(Date(log.date))
                val amountColor = if (log.amount>0) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                val amountPrefix = if (log.amount>0) "+" else ""

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .shadow(2.dp, RoundedCornerShape(16.dp))
                ){
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Column(
                            modifier = Modifier.weight(1f).padding(end=8.dp)
                        ){
                            Text(
                                text=log.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF1B5E20),
                                maxLines =  2
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text=dateString, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Text(
                            text = "   $amountPrefix${log.amount} pts",
                            color = amountColor,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }

}