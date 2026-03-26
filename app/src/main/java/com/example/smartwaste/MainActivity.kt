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
import android.content.Intent
import android.location.Location
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
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
import com.example.smartwaste.screens.QuizScreen
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.RoundCap
import com.google.maps.android.compose.rememberMarkerState
import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.example.smartwaste.screens.LogsScreen
import com.example.smartwaste.screens.RewardScreen


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
    var userPoints by remember { mutableStateOf(0)}
    var triggerCamera by remember {mutableStateOf(false)}

    //Real time listener for firestore to check the user points
    LaunchedEffect(Unit) {
        val uid = Firebase.auth.currentUser?.uid
        if (uid != null) {
            Firebase.firestore.collection("users").document(uid)
                .addSnapshotListener { snapshot,  error ->
                    if (error == null && snapshot != null && snapshot.exists()) {
                        userPoints = snapshot.getLong("points")?.toInt() ?: 0 //Set points to 0 if it does not exist (maybe its a new user)
                    }
                }
        }
    }

    // Screen layout
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(
                    text= buildAnnotatedString {
                        withStyle(style = SpanStyle(color = Color(0xFF1B55E20), fontWeight = FontWeight.ExtraBold)){
                            append("Smart")
                        }
                        withStyle(style = SpanStyle(color = Color(0xFF66BB6A), fontWeight = FontWeight.ExtraBold)){
                            append("Waste")
                        }
                    },
                    fontSize = 26.sp
                )
            },
            navigationIcon = {
                if (currentScreen.startsWith("guideDetail_")){
                    IconButton(onClick = {currentScreen = "guides"}) {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back")
                    }
                }
            },
            actions ={
                IconButton(onClick = {currentScreen = "profile"}) {
                    Icon(Icons.Default.AccountCircle, contentDescription = "Profile")
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color(0xFFFAFCFA)
                )
            )
        },
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            if (currentScreen =="scanner") {
                ExtendedFloatingActionButton(
                    onClick = {
                        currentScreen = "scanner"
                        triggerCamera = true
                    },
                    containerColor = Color(0xFF2E7D32),
                    contentColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(8.dp),
                    icon = {Icon(
                            painterResource(id = R.drawable.ic_camera_alt),
                    contentDescription = "Scan"
                    )},
                    text = {Text("Scan Waste", fontSize = 16.sp, fontWeight = FontWeight.Bold)}
                )
            }
        },
        bottomBar = {
            if(currentScreen != "quiz") {
                BottomAppBar(
                    containerColor = Color.White,
                    contentColor = Color(0xFF2E7D32),
                    actions = {

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clickable{currentScreen = "quiz"}
                                .padding(vertical = 6.dp)

                        ){
                            Icon(Icons.Default.Create, contentDescription = "Quiz")
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Quiz", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clickable{currentScreen = "guides"}
                                .padding(vertical = 6.dp)

                        ){
                            Icon(Icons.Default.Info, contentDescription = "Guides")
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Guides", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clickable{currentScreen = "scanner"}
                                .padding(vertical = 6.dp)

                        ){
                            Icon(Icons.Default.Home, contentDescription = "Home")
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Home", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clickable{currentScreen = "rewards"}
                                .padding(vertical = 6.dp)

                        ){
                            Icon(Icons.Default.Star, contentDescription = "Rewards")
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Rewards", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clickable{currentScreen = "logs"}
                                .padding(vertical = 6.dp)

                        ){
                            Icon(Icons.Default.List, contentDescription = "Logs")
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Logs", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when {
                currentScreen == "scanner" -> {
                    Scanner(
                        classifier = classifier,
                        points = userPoints,
                        triggerCamera = triggerCamera,
                        onCameraTriggered = {triggerCamera = false},
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
                currentScreen == "quiz" -> {
                    val context = LocalContext.current
                    QuizScreen(
                        onQuizComplete = {pointsEarned ->
                            val sharedPrefs = context.getSharedPreferences("SmartWastePrefs", Context.MODE_PRIVATE)
                            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                            sharedPrefs.edit().putString("lastQuizDate", currentDate).apply()
                            val uid = Firebase.auth.currentUser?.uid
                            if (uid != null) {
                                Firebase.firestore.collection("users").document(uid)
                                    .update("points", com.google.firebase.firestore.FieldValue.increment(pointsEarned.toLong()))
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Earned $pointsEarned points!",Toast.LENGTH_SHORT).show()
                                    }
                                val logData = hashMapOf(
                                    "title" to "Completed Quiz",
                                    "amount" to pointsEarned,
                                    "date" to System.currentTimeMillis()
                                )
                                Firebase.firestore.collection("users").document(uid)
                                    .collection("logs").add(logData)
                            }


                            currentScreen = "scanner"
                        },
                            onNavigateBack = {currentScreen = "scanner"}
                        )
                }
                currentScreen == "rewards" -> RewardScreen(userPoints = userPoints)
                currentScreen == "logs" -> LogsScreen()
            }
        }

    }
}

@Composable
fun Scanner(
    classifier: ImageClassifier,
    points: Int,
    triggerCamera: Boolean,
    onCameraTriggered: () -> Unit,
    onNavigateToGuideDetail: (String) -> Unit,
) {
    val context = LocalContext.current
    //Quiz
    val sharedPrefs = context.getSharedPreferences("SmartWastePrefs", Context.MODE_PRIVATE)
    val lastPlayedDate = sharedPrefs.getString("lastQuizDate", "")
    val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    //val canPlayQuiz = lastPlayedDate != currentDate //Uncomment for actual testing because of the date
    val canPlayQuiz = true // To remove after done with testing quiz because the quiz is supposed to be once a day

    //For scanning of waste
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
                        @Suppress("DEPRECATION")
                        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                    } else {
                        val source = ImageDecoder.createSource(context.contentResolver, uri)
                        ImageDecoder.decodeBitmap(source)
                    }
                    // Since the bitmap used by phone camera is a hardware bitmap, we copy it to a software bitmap so that it can be read by CPU for processing
                    val softwareBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
                    // Run the classification on software copy

                    // Logging of timer to check how long the classification actually take in real time
                    val startTime = System.currentTimeMillis()

                    val result = classifier.classify(softwareBitmap)
                    val endTime = System.currentTimeMillis() //End timer
                    val latency = endTime - startTime
                    Log.d("CategorisationLatency", "Inference time: $latency ms")

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
    LaunchedEffect(triggerCamera) {
        if (triggerCamera) {
            onCameraTriggered()
            when (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)){
                PackageManager.PERMISSION_GRANTED -> {
                    imageUri = context.createImageUri()
                    cameraLauncher.launch(imageUri)
                }
                else -> permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFCFA))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        AnimatedPointsBadge(points)

        Spacer(modifier = Modifier.height(56.dp))

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
            modifier = Modifier.fillMaxWidth(0.85f)
        ){
            Row(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ){
                Icon(
                    imageVector = Icons.Default.AddTask,
                    contentDescription = "Quiz",
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Take daily quiz to get more points!",
                    fontSize = 16.sp,
                    textAlign = TextAlign.Start,
                    color = Color(0xFF1B5E20),
                    fontWeight = FontWeight.Bold
                )
            }
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
            containerColor = Color.White,
            iconContentColor = Color(0xFF2E7D32),
            titleContentColor = Color(0xFF1B5E20),
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_camera_alt),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = { Text("Classification Result", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    Text("Your waste is classified as:", color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = classificationResult?.replace("_"," ")?.uppercase() ?: "Unknown", //This one is just to replace the labelling of underscore to space so it looks better
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF2E7D32)
                    )
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ){
                    OutlinedButton(
                        onClick = {
                            showDialog = false
                            classificationResult = null
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end=8.dp),
                        colors= ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF2E7D32)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E7D32)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp)
                    ){
                        Text("OK", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            val rawResult = classificationResult ?: ""
                            showDialog = false
                            classificationResult = null
                            val guideID = when(rawResult.lowercase()) {
                                "food_waste" -> "food"  //This is because food_waste label does not match the ID in DisposalGuide
                                else -> rawResult.lowercase()
                            }
                            onNavigateToGuideDetail(guideID)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(start=8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2E7D32),
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp)
                    ) {
                        Text("View Guide", fontWeight = FontWeight.Bold)
                    }
                }
            },
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
    val locations = listOf(
        LatLng(1.309039, 103.880682), //Guillemard Cres near Mountbatten Rd
        LatLng(1.312422, 103.889680), //Lor 40 Geyland area
        LatLng(1.310474, 103.927666), // Jln Kampung Siglap
        LatLng(1.327967, 103.928696), // Bedok area
        LatLng(1.371037, 103.957664), // Pasir Ris area
        LatLng(1.399084, 103.902373), // Punggol area
        LatLng(1.354056, 103.865489), // Lorong Chuan
        LatLng(1.434018, 103.799525), // Woodlands Drive
        LatLng(1.378091, 103.768164), // Bukit Panjang
        LatLng(1.337382, 103.743049), // Jurong East
        LatLng(1.336899, 103.697514), // Jurong West
        LatLng(1.337741, 103.721641), // Corporation Dr

    )

    locations.forEachIndexed { index,  location ->
        bins.add(
            WasteBin(
                id = "BIN_${String.format("%03d", index + 1)}",
                latitude = location.latitude,
                longitude = location.longitude,
                fillLevel = (0..100).random()
            )
        )
    }
    return bins
}
fun sortBinsByNearestNeighbour(startLocation:LatLng, binsToVisit: List<WasteBin>): List<WasteBin> {
    val unvisited = binsToVisit.toMutableList()
    val optimisedRoute = mutableListOf<WasteBin>()
    var currentLocation = startLocation

    while (unvisited.isNotEmpty()){
        val nearestBin = unvisited.minByOrNull { bin ->
            val results = FloatArray(1)
            Location.distanceBetween(
                currentLocation.latitude, currentLocation.longitude,
                bin.latitude, bin.longitude,
                results
            )
            results[0]
        }
        if (nearestBin != null) {
            optimisedRoute.add(nearestBin)
            unvisited.remove(nearestBin)
            currentLocation = LatLng(nearestBin.latitude, nearestBin.longitude)
        }
    }
    return optimisedRoute
}
// Suspend so as to run and fetch the data in background
suspend fun fetchRoute(currentLocation: LatLng, criticalBins: List<WasteBin>): List<LatLng> = withContext(Dispatchers.IO) {
    if(criticalBins.isEmpty()) return@withContext emptyList()
    val sortedBins = sortBinsByNearestNeighbour(currentLocation, criticalBins)
    val apikey = BuildConfig.ORS_API_KEY
    val url = URL("https://api.openrouteservice.org/v2/directions/driving-car/geojson")

    try{
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Authorization", apikey)
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
        conn.doOutput = true

        val driverCords = "[${currentLocation.longitude}, ${currentLocation.latitude}]"
        val binCords = sortedBins.joinToString(",") {"[${it.longitude}, ${it.latitude}]"}
        val jsonPayload = """{"coordinates":[$driverCords, $binCords]}"""
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
fun StaffDashBoard(onLogout: () -> Unit) {
    val context = LocalContext.current
    var simulatedBins by remember { mutableStateOf(generateSyntheticBins()) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val defaultLocation = LatLng(1.3521, 103.8198) //just in case gps not working then we fallback to the general location of SG
    var currentLocation by remember { mutableStateOf<LatLng?>(null) } // this is set to null such that we wait for the gps to locate our location while being null
    var routePoints by remember{ mutableStateOf<List<LatLng>>(emptyList())}
    var sortedCriticalBins by remember { mutableStateOf<List<WasteBin>>(emptyList()) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 11f)
    }

    //If the user never give permission or permission denied for location services then it fallsback to default location
    val locationPermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true|| permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (isGranted) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    currentLocation = if (location != null) {
                        LatLng(location.latitude, location.longitude)
                    } else defaultLocation
                }
            } catch (e: SecurityException){
                currentLocation = defaultLocation
            }
        } else {
            currentLocation = defaultLocation
        }
    }
    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (hasPermission){
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    currentLocation = if (location != null){
                        LatLng(location.latitude, location.longitude)
                    } else defaultLocation
                }
            } catch (e: SecurityException){
                currentLocation = defaultLocation
            }
        } else {
            locationPermissionsLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    LaunchedEffect(currentLocation,simulatedBins) {
        if (currentLocation != null){
            cameraPositionState.position = CameraPosition.fromLatLngZoom(currentLocation!!, 13f)
            val criticalBins = simulatedBins.filter { it.fillLevel > 80 }
            sortedCriticalBins = sortBinsByNearestNeighbour(currentLocation!!, criticalBins)
            routePoints = fetchRoute(
                currentLocation = currentLocation!!,
                criticalBins = criticalBins
            )
        }

    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Staff Dashboard") },
                actions = {
                    IconButton(onClick = {
                        simulatedBins = generateSyntheticBins()
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Route")
                    }
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
                .padding(16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            //verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom= 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Fleet route", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                val criticalCount = simulatedBins.count { it.fillLevel > 80 }
                Text(
                    "Requires Pickup: $criticalCount",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState
                ) {
                    simulatedBins.forEach { bin ->
                        val isCritical = bin.fillLevel > 80
                        val binLocation = LatLng(bin.latitude, bin.longitude)
                        val markerColor = if (isCritical) {
                            BitmapDescriptorFactory.HUE_RED
                        } else {
                            BitmapDescriptorFactory.HUE_GREEN
                        }
                        val binMarkerState = rememberMarkerState(position = binLocation)
                        Marker(
                            state = binMarkerState,
                            title = bin.id,
                            snippet = "Fill Level: ${bin.fillLevel}%",
                            icon = BitmapDescriptorFactory.defaultMarker(markerColor)
                        )
                    }
                    if (currentLocation != null) {
                        val driverState = rememberMarkerState(position = currentLocation!!)
                        Marker(
                            state = driverState,
                            title = "Your Current Location",
                            snippet = "Starting point",
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                        )

                    }

                    if (routePoints.isNotEmpty()) {
                        Polyline(
                            points = routePoints,
                            color = Color.Blue,
                            width = 12f,
                            pattern = listOf(Dash(30f), Gap(20f)),
                            endCap = RoundCap()
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
            if (sortedCriticalBins.isNotEmpty()){
                Text(text = "Pickup Sequence", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(sortedCriticalBins) {index, bin ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                                    contentAlignment = Alignment.Center
                                ){
                                    Text("${index + 1}", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column{
                                    Text(text=bin.id, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text(text="Fill Level: ${bin.fillLevel}%", color = MaterialTheme.colorScheme.error, fontSize=14.sp)
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                if (routePoints.isNotEmpty() && currentLocation != null) {
                    Button(
                        onClick = {
                            val criticalBins = simulatedBins.filter{it.fillLevel >80}
                            var sortedBins = sortBinsByNearestNeighbour(currentLocation!!, criticalBins)
                            if (sortedBins.isNotEmpty()){
                                val origin = currentLocation!!
                                val destination = sortedBins.last()
                                val middleStops = sortedBins.dropLast(1)
                                val waypointsStr = middleStops.joinToString("|") {"${it.latitude},${it.longitude}"}
                                var mapUri = "https://www.google.com/maps/dir/?api=1" +
                                            "&origin=${origin.latitude},${origin.longitude}" +
                                            "&destination=${destination.latitude},${destination.longitude}" +
                                            "&travelmode=driving"

                                if(waypointsStr.isNotEmpty()){
                                    mapUri += "&waypoints=$waypointsStr"
                                }
                                val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(mapUri))
                                mapIntent.setPackage("com.google.android.apps.maps")
                                context.startActivity(mapIntent)
                            }

                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)

                    ) {
                        Text(text ="Start Navigation", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                    Text(text = "All bins are operating at normal level", textAlign = TextAlign.Center, color =Color.Gray)
                }
            }
        }
    }
}
}

data class TrashItem(
    val emoji:String,
    val offsetX: Float,
    val yOffset: Float,
    val startRotation: Float,
    val spinSpeed: Float
)

@Composable
fun AnimatedPointsBadge(currentPoints: Int){
    var isInitialLoad by remember {mutableStateOf(true)}
    var displayedPoints by remember { mutableStateOf(currentPoints)}
    var trashSwarm by remember { mutableStateOf<List<TrashItem>>(emptyList())}

    val animatedScore = remember {Animatable(currentPoints.toFloat())}
    val lidAngle = remember {Animatable(0f)}
    val trashOffsetY = remember {Animatable(-200f)}
    val trashAlpha = remember {Animatable(0f)}
    val textAlpha = remember {Animatable(1f)}

    LaunchedEffect(currentPoints) {
        if(isInitialLoad){
            animatedScore.snapTo(currentPoints.toFloat())
            displayedPoints = currentPoints
            isInitialLoad = false
        } else if(currentPoints>displayedPoints){
            val emojiPool = listOf("\uD83D\uDCC4", "\uD83E\uDD64","\uD83E\uDDC3","\uD83D\uDCC3","\uD83E\uDD6B","\uD83D\uDCE6", "\uD83D\uDDDE\uFE0F")
            trashSwarm = List(10){
                TrashItem(
                    emoji = emojiPool.random(),
                    offsetX = (-60..60).random().toFloat(),
                    yOffset = (0..100).random().toFloat(),
                    startRotation = (0..360).random().toFloat(),
                    spinSpeed = listOf(-3f, -2f,2f,3f).random()
                )
            }

            textAlpha.animateTo(0f, animationSpec =tween(400)) //make the points dissapear first
            lidAngle.animateTo(55f, animationSpec = tween(600, easing = FastOutSlowInEasing)) //open lid

            //make sure the waste dissapear in the point badge
            trashOffsetY.snapTo(-250f)
            trashAlpha.snapTo(1f)
            trashOffsetY.animateTo(60f, animationSpec = tween(700, easing = LinearOutSlowInEasing))
            trashAlpha.animateTo(0f, animationSpec = tween(300))

            lidAngle.animateTo(0f, animationSpec = tween(600, easing = FastOutSlowInEasing)) //close lid

            //bring points text back animation of score counting up after trash drop
            textAlpha.animateTo(1f, animationSpec =tween(400))
            animatedScore.animateTo(
                targetValue = currentPoints.toFloat(),
                animationSpec = tween(1500, easing = FastOutSlowInEasing)
            )
            displayedPoints =currentPoints
        }
    }

    Box(
        modifier = Modifier
            .size(240.dp)
            .shadow(16.dp, CircleShape, ambientColor = Color(0xFF2E7D32), spotColor= Color(0xFF2E7D32)),
        contentAlignment = Alignment.Center
    ){
        if (trashAlpha.value >0f) {
            trashSwarm.forEach { item ->
                Text(
                    text = item.emoji,
                    fontSize = 24.sp,
                    modifier = Modifier
                        .graphicsLayer{
                            translationX =item.offsetX.dp.toPx() //random speed that they fall
                            translationY = trashOffsetY.value.dp.toPx() -item.yOffset.dp.toPx() // so they dont fall tgr
                            rotationZ = item.startRotation + (trashOffsetY.value*item.spinSpeed) //rotate
                            alpha = trashAlpha.value
                        }
                )
            }
        }

        Canvas(modifier = Modifier.fillMaxSize()){
            val radius = size.minDimension /2
            val centerOffset = Offset(size.width/2, size.height/2)
            val circleGradient = Brush.linearGradient(
                colors = listOf(Color(0xFF66BB6A), Color(0xFF2E7D32)),
                start = Offset.Zero,
                end = Offset(size.width, size.height)
            )
            //bottom half of lid
            drawArc(
                brush = circleGradient,
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = true,
                size = Size(radius*2,radius*2),
                topLeft = Offset(centerOffset.x-radius, centerOffset.y-radius),
                style = Fill
            )
            //top half
            rotate(
                degrees = lidAngle.value,
                pivot = Offset(centerOffset.x+radius, centerOffset.y)
            ){
                drawArc(
                    brush = circleGradient,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = true,
                    size = Size(radius*2,radius*2),
                    topLeft = Offset(centerOffset.x-radius, centerOffset.y-radius),
                    style = Fill
                )
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.graphicsLayer{alpha=textAlpha.value}
        ){
            Text(
                text = animatedScore.value.toInt().toString(),
                fontSize = 60.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                text = "Points",
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFE8F5E9)
            )
        }
    }
}

