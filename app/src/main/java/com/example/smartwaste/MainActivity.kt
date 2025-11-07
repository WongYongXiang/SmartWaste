package com.example.smartwaste

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.smartwaste.ui.theme.SmartWasteTheme
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Objects

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartWasteTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        classifier = ImageClassifier(applicationContext)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(classifier: ImageClassifier) {
    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var classificationResult by remember { mutableStateOf<String?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                imageUri?.let { uri ->
                    // Convert the image URI to a Bitmap
                    val bitmap: Bitmap = if (Build.VERSION.SDK_INT <28) {
                        @Suppress("DEPRECATION") //This is deprecated, but we're using an old API
                        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                    } else {
                        val source = ImageDecoder.createSource(context.contentResolver, uri)
                        ImageDecoder.decodeBitmap(source)
                    }
                    // Since the bitmap used by phone camera is a hardware bitmap, we copy it to a software bitmap so that it can be read by CPU for processing
                    val softwareBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
                    // Run the classification on software copy
                    val result = classifier.classify(softwareBitmap)
                    classificationResult = result
                    showDialog = true

                }
            } else {
                // Handle failure or user cancellation
                Toast.makeText(context, "Camera cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    )
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Classification Result") },
            text = { Text(classificationResult ?: "No result") },
            confirmButton = {
                Button(onClick = { showDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    // This asks the user for permission to use the camera
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                imageUri = context.createImageUri() // Create a new file URI
                cameraLauncher.launch(imageUri)
            } else {
                Toast.makeText(context, "Camera permission is required", Toast.LENGTH_SHORT).show()
            }
        }
    )

    // Screen layout
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Smart Waste disposal app") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            Spacer(modifier = Modifier.height(32.dp))

            // Points display
            Text(
                text = "Your Points: 0",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.weight(1f))

            // Scan button
            Button(
                onClick = {
                    // Check if we have permission. If not, ask for it.
                    // If we do, launch the camera directly.
                    when (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)) {
                        PackageManager.PERMISSION_GRANTED -> {
                            imageUri = context.createImageUri() // Get a new URI
                            cameraLauncher.launch(imageUri)
                        }
                        else -> {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_camera_alt),
                    contentDescription = "Scan Icon",
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = " Scan Waste",
                    fontSize = 18.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // Guides Button -- For future implementation
            OutlinedButton(
                onClick = {
                    // Placeholder
                    Toast.makeText(context, "Guides coming soon!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                Text(text = "Browse Disposal Guides", fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Results
    // Dialog pops up when classification is complete
    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
                classificationResult = null
            },
            title = { Text("Classification Result") },
            text = {
                Text(
                    text = "Your waste is classified as: \n${classificationResult?.uppercase() ?: "Unknown"}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog = false
                        classificationResult = null
                        // TODO: Add points to user
                        // TODO: Show disposal guide
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }
}

// Helper function to create a new file URI for the camera
private fun Context.createImageUri(): Uri {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFile = File.createTempFile(
        "JPEG_${timeStamp}_",
        ".jpg",
        externalCacheDir
    )
    return FileProvider.getUriForFile(
        this,
        "${applicationContext.packageName}.provider",
        Objects.requireNonNull(imageFile)
    )
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    SmartWasteTheme {
        MainScreen(classifier = ImageClassifier(LocalContext.current))
    }
}