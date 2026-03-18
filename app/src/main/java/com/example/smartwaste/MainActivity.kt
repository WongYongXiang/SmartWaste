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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.smartwaste.screens.ProfileScreen
import com.example.smartwaste.screens.RegistrationScreen
import com.example.smartwaste.ui.theme.SmartWasteTheme
import com.google.android.gms.maps.model.CameraPosition
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Objects
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.URL
import androidx.compose.ui.graphics.Color
import com.google.maps.android.compose.Polyline
import java.net.HttpURLConnection
import com.example.smartwaste.screens.GuidesListScreen
import com.example.smartwaste.screens.GuideDetailScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartWasteTheme {
                var isLoggedIn by remember {
                    mutableStateOf(Firebase.auth.currentUser != null)
                }
                var userRole by remember {
                    mutableStateOf(if (isLoggedIn) "Loading" else "Unauthenticated")
                }

                LaunchedEffect(isLoggedIn) {
                    if (isLoggedIn) {
                        userRole = "Loading"
                        val uid = Firebase.auth.currentUser?.uid
                        if (uid != null) {
                            Firebase.firestore.collection("users").document(uid).get()
                                .addOnSuccessListener {document ->
                                    if (document != null && document.exists()) {
                                        userRole = document.getString("role")?:"public"
                                    } else {
                                        userRole = "public"
                                    }
                                }
                                .addOnFailureListener {
                                    userRole = "public"
                                }
                        }
                        else{
                            userRole = "Unauthenticated"
                        }
                    } else {
                        userRole = "Unauthenticated"
                    }
                }
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (userRole) {
                        "Loading" -> {
                            Box(modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                        "Unauthenticated" -> {
                            RegistrationScreen (onRegistrationSuccess = {
                                isLoggedIn = true
                            })
                        }
                        "staff" -> {
                            StaffDashBoard(
                                onLogout = {
                                    Firebase.auth.signOut()
                                    isLoggedIn = false
                                }
                            )
                        }
                        else -> {
                            MainScreen(
                                classifier = ImageClassifier(applicationContext),
                                onLogout = {
                                    Firebase.auth.signOut()
                                    isLoggedIn = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(classifier: ImageClassifier, onLogout: () -> Unit) {
    var currentScreen by remember {mutableStateOf("scanner")}

    // Screen layout
    Scaffold(
        topBar = {
            TopAppBar(
               title = {
                   Text(
                       when {
                           currentScreen == "scanner" -> "Smart Waste"
                           currentScreen == "profile" -> "My Profile"
                           currentScreen == "guides" -> "Disposal Guides"
                           currentScreen.startsWith("guideDetail_") -> "Guide Details"
                           else -> "App"
                       }
                   )
               },
                navigationIcon = {
                    if (currentScreen != "scanner") {
                        IconButton(onClick = {
                            if (currentScreen.startsWith("guideDetail_")) {
                                currentScreen = "guides"
                            } else {
                                currentScreen = "scanner"
                            }
                        }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    if (currentScreen == "scanner") {
                        IconButton(onClick = { currentScreen = "profile"}) {
                            Icon(Icons.Default.AccountCircle, contentDescription = "Profile")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when {
                currentScreen == "scanner" -> {
                    Scanner(
                        classifier = classifier,
                        onNavigateToGuides = { currentScreen = "guides"},
                        onNavigateToGuideDetail = { guideId ->
                            currentScreen = "guideDetail_$guideId"
                        }
                    )
                }
                currentScreen == "profile" -> {
                    ProfileScreen(onLogout = onLogout)
                }
                currentScreen == "guides" -> {
                    GuidesListScreen(onGuideClick = { guideId ->
                        currentScreen = "guideDetail_$guideId"
                    })
                }
                currentScreen.startsWith("guideDetail_") -> {
                    val id = currentScreen.removePrefix("guideDetail_")
                    GuideDetailScreen(guideId = id)
                }
            }
        }

    }
}

@Composable
fun Scanner(
    classifier: ImageClassifier,
    onNavigateToGuides: () -> Unit,
    onNavigateToGuideDetail: (String) -> Unit
) {
    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var classificationResult by remember { mutableStateOf<String?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                imageUri?.let { uri ->
                    // Convert the image URI to a Bitmap
                    val bitmap: Bitmap = if (Build.VERSION.SDK_INT < 28) {
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

    Column(
        modifier = Modifier
            .fillMaxSize()
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
            onClick = onNavigateToGuides,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        ) {
            Text(text = "Browse Disposal Guides", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // Results
    // Dialog pops up when classification is complete
    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
                classificationResult = null
            },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_camera_alt),
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("Classification Result") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    Text("Your waste is classified as:", fontSize = 16.sp)
                    Text(
                        text = classificationResult?.replace("_"," ")?.uppercase() ?: "Unknown", //This one is just to replace the labelling of underscore to space so it looks better
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val rawResult = classificationResult ?: ""
                        showDialog = false
                        classificationResult = null
                        // TODO: Add points to user
                        val guideID = when(rawResult.lowercase()) {
                            "food_waste" -> "food"  //This is because food_waste label does not match the ID in DisposalGuide
                            else -> rawResult.lowercase()
                        }
                        onNavigateToGuideDetail(guideID)
                    }
                ) {
                    Text("View Guide")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showDialog = false
                        classificationResult = null
                    }
                ){
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
// For simulation of waste bin
data class WasteBin(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val fillLevel: Int
)

fun generateSyntheticBins(): List<WasteBin> {
    val bins = mutableListOf<WasteBin>()
    val sgCenterLat = 1.3521 // The lat and lon is the general coordinates of SG
    val sgCenterLng = 103.8198

    for (i in 1..15) {
        val latOffset = (Math.random() - 0.5 ) * 0.1
        val lngOffset = (Math.random() - 0.5 ) * 0.1

        bins.add(
            WasteBin(
                id = "BIN_SG_${String.format("%03d", i)}",
                latitude = sgCenterLat + latOffset,
                longitude = sgCenterLng + lngOffset,
                fillLevel = (0..100).random()
            )
        )
    }
    return bins
}
// Suspend so as to run and fetch the data in background
suspend fun fetchRoute(criticalBins: List<WasteBin>): List<LatLng> = withContext(Dispatchers.IO) {
    if(criticalBins.size < 2) return@withContext emptyList()
    val sortedBins = criticalBins.sortedBy { it.longitude }
    val apikey = BuildConfig.ORS_API_KEY
    val url = URL("https://api.openrouteservice.org/v2/directions/driving-car/geojson")

    try{
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Authorization", apikey)
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
        conn.doOutput = true

        val coords = sortedBins.joinToString(",") {"[${it.longitude}, ${it.latitude}]"}
        val jsonPayload = """{"coordinates":[$coords]}"""
        OutputStreamWriter(conn.outputStream).use {it.write(jsonPayload)}

        val response = conn.inputStream.bufferedReader().use {it.readText()}
        val jsonResponse = JSONObject(response)

        val features = jsonResponse.getJSONArray("features")
        val geometry = features.getJSONObject(0).getJSONObject("geometry")
        val coordinates = geometry.getJSONArray("coordinates")

        val route = mutableListOf<LatLng>()
        for (i in 0 until coordinates.length()) {
            val point = coordinates.getJSONArray(i)
            route.add(LatLng(point.getDouble(1), point.getDouble(0)))
        }
        return@withContext route
    } catch(e:Exception){
        e.printStackTrace()
        return@withContext emptyList()
    }

}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffDashBoard(onLogout: () -> Unit){

    val simulatedBins by remember { mutableStateOf(generateSyntheticBins())}
    val singapore = LatLng(1.3521, 103.8198)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(singapore, 11f)
    }
    var routePoints by remember {mutableStateOf<List<LatLng>>(emptyList())}

    LaunchedEffect(simulatedBins) {
        val criticalBins = simulatedBins.filter {it.fillLevel > 80}
        routePoints = fetchRoute(criticalBins)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Staff Dashboard")},
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onTertiaryContainer
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
            //verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Fleet route", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                val criticalCount = simulatedBins.count {it.fillLevel > 80}
                Text("Requires Pickup: $criticalCount", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
            }

            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                simulatedBins.forEach { bin ->
                    val isCritical = bin.fillLevel >80
                    val binLocation = LatLng(bin.latitude, bin.longitude)
                    val markerColor = if (isCritical) {
                        BitmapDescriptorFactory.HUE_RED
                    } else {
                        BitmapDescriptorFactory.HUE_GREEN
                    }
                    Marker(
                        state = MarkerState(position = binLocation),
                        title = bin.id,
                        snippet = "Fill Level: ${bin.fillLevel}%",
                        icon = BitmapDescriptorFactory.defaultMarker(markerColor)
                    )
                }
                if (routePoints.isNotEmpty()) {
                    Polyline(
                        points = routePoints,
                        color = Color.Blue,
                        width = 12f
                    )
                }
            }
        }
    }
}


