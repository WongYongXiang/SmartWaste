package com.example.smartwaste

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.smartwaste.ui.theme.SmartWasteTheme
import com.example.smartwaste.R

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartWasteTheme {
                MainUserScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainUserScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Smart Waste disposal") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // placeholder
            Text(
                text = "Your Points: 0",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Message
            Text(
                text = "Scan waste to earn points and see disposal guides",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Scan Button
            val context = LocalContext.current
            Button(
                onClick = {
                    // Future implementation: Launch camera to scan waste
                    Toast.makeText(context, "Scan feature coming soon!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .height(IntrinsicSize.Min)
                    .width(IntrinsicSize.Min),
                contentPadding = PaddingValues(horizontal = 48.dp, vertical = 24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_camera_alt),
                        contentDescription = "Scan Icon",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Scan Waste", style = MaterialTheme.typography.titleMedium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Disposal Guide
            OutlinedButton(
                onClick = {
                    // Future Implementation: Launch a new screen and show disposal guides
                    Toast.makeText(context, "Guide feature coming soon!", Toast.LENGTH_SHORT).show()
                }
            ) {
                Text(text = "Browse Disposal Guides")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SmartWasteTheme {
        MainUserScreen()
    }
}