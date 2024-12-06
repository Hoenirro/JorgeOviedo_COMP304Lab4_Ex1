package com.jorgeoviedolab4

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.jorgeoviedolab4.RoomDB.MyLocation
import com.jorgeoviedolab4.ViewModel.SavedLocationsViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.livedata.observeAsState
import com.jorgeoviedolab4.ui.theme.JorgeOviedo_COMP304Lab4_Ex1Theme

class MainActivity : ComponentActivity() {
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true &&
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            //&& permissions[Manifest.permission.ACCESS_BACKGROUND_LOCATION] == true
        ) {
            requestBackgroundLocationPermission()
        } else {
            Toast.makeText(this, "Permissions not granted", Toast.LENGTH_SHORT).show()
        }
    }

    private val backgroundLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted -> if (isGranted) { // Background location permission granted
    } else {
        Toast.makeText(this, "Background location permission not granted", Toast.LENGTH_SHORT).show() }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestLocationPermissions()
        setContent {
            MyApp()
        }
    }

    private fun requestLocationPermissions() {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                //Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        )
    }
    private fun requestBackgroundLocationPermission() {
        backgroundLocationPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    }
}


@Composable
fun MyApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val locationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    val savedLocationsViewModel: SavedLocationsViewModel = viewModel()

    Scaffold(
        topBar = { CustomTopBar(navController) },
        content = { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("home") { HomeScreen(navController) }
                composable("navigation") { NavigationScreen(navController, locationProviderClient, savedLocationsViewModel) }
                composable("saved_locations") { SavedLocationsScreen(navController, savedLocationsViewModel) }
            }
        }
    )
}



@Composable
fun CustomTopBar(navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicText(text = "JEOSmap - dev by Jorge")
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = { navController.navigate("home") }) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = "Home"
            )
        }
    }
}

@Composable
fun HomeScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { navController.navigate("navigation") }) {
            Text("Go to Navigation Screen")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.navigate("saved_locations") }) {
            Text("Go to Saved Locations Screen")
        }
    }
}

@Composable
fun NavigationScreen(
    navController: NavController,
    locationProviderClient: FusedLocationProviderClient,
    savedLocationsViewModel: SavedLocationsViewModel
) {
    val context = LocalContext.current
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 15f)
    }

    LaunchedEffect(Unit) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 10000 // Update every 10 seconds
            fastestInterval = 5000
        }

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    val newLocation = LatLng(location.latitude, location.longitude)
                    userLocation = newLocation
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(newLocation, 15f)
                }
            }
        }

        try {
            locationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                context.mainLooper
            )
        } catch (e: SecurityException) {
            // Log or handle permissions error
        } catch (e: Exception) {
            // Log or handle other errors
        }
    }

    var markers by remember { mutableStateOf(listOf<LatLng>()) }

    Column(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.weight(4f / 5f),
            cameraPositionState = cameraPositionState,
            onMapClick = { latLng ->
                markers = markers + latLng
                val newLocation = MyLocation(
                    name = "Custom Location",
                    latitude = latLng.latitude,
                    longitude = latLng.longitude
                )
                savedLocationsViewModel.insertLocation(newLocation)
            }
        ) {
            userLocation?.let {
                Marker(
                    state = MarkerState(position = it),
                    title = "You are here"
                )
            }
            markers.forEach { latLng ->
                Marker(
                    state = MarkerState(position = latLng),
                    title = "Selected Location"
                )
            }
        }

        val allLocations by savedLocationsViewModel.allLocations.observeAsState(emptyList())

        LazyColumn(modifier = Modifier.weight(1f / 5f)) {
            items(allLocations) { location ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${location.id}, ${location.latitude}, ${location.longitude}")
                    Row {
                        IconButton(onClick = { savedLocationsViewModel.deleteLocation(location) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                        IconButton(onClick = {
                            val geofenceId = "GEOFENCE_ID_${location.latitude}_${location.longitude}"
                            val geofence = createGeofence(geofenceId, LatLng(location.latitude, location.longitude), 200f)
                            addGeofence(context, geofence)
                        }) {
                            Text("Geo")
                        }
                    }
                }
            }
        }
    }
}

/*
@Composable
fun NavigationScreen(
    navController: NavController,
    locationProviderClient: FusedLocationProviderClient,
    savedLocationsViewModel: SavedLocationsViewModel
) {
    val context = LocalContext.current
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 15f)
    }

    LaunchedEffect(Unit) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 10000 // Update every 10 seconds
            fastestInterval = 5000
        }

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    val newLocation = LatLng(location.latitude, location.longitude)
                    userLocation = newLocation
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(newLocation, 15f)
                }
            }
        }

        try {
            locationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                context.mainLooper
            )
        } catch (e: SecurityException) {
            // Log or handle permissions error
        } catch (e: Exception) {
            // Log or handle other errors
        }
    }

    var markers by remember { mutableStateOf(listOf<LatLng>()) }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        onMapClick = { latLng ->
            markers = markers + latLng
            val geofenceId = "GEOFENCE_ID_${latLng.latitude}_${latLng.longitude}"
            val geofence = createGeofence(geofenceId, latLng, 200f)
            addGeofence(context, geofence)
            Log.d("NavigationScreen", "Geofence added at: $latLng")
        }
    ) {
        userLocation?.let {
            Marker(
                state = MarkerState(position = it),
                title = "You are here"
            )
        }
        markers.forEach { latLng ->
            Marker(
                state = MarkerState(position = latLng),
                title = "Selected Location"
            )
        }
    }
}
*/


/*
@Composable
fun NavigationScreen(
    navController: NavController,
    locationProviderClient: FusedLocationProviderClient,
    savedLocationsViewModel: SavedLocationsViewModel
) {
    val context = LocalContext.current
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 15f)
    }

    LaunchedEffect(Unit) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 10000 // Update every 10 seconds
            fastestInterval = 5000
        }

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    val newLocation = LatLng(location.latitude, location.longitude)
                    userLocation = newLocation
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(newLocation, 15f)
                }
            }
        }

        locationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            context.mainLooper
        )
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        userLocation?.let {
            Marker(
                state = MarkerState(position = it),
                title = "You are here"
            )
        }

        val markers = remember { mutableStateListOf<LatLng>() }
        markers.forEach { latLng ->
            Marker(
                state = MarkerState(position = latLng),
                title = "Selected Location"
            )
        }

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            onMapClick = { latLng ->
                markers.add(latLng)
            }
        )
    }
}
*/
private fun saveLocationToDatabase(location: LatLng, savedLocationsViewModel: SavedLocationsViewModel) {
    val newLocation = MyLocation(name = "Custom Location", latitude = location.latitude, longitude = location.longitude)
    savedLocationsViewModel.insertLocation(newLocation)
}

private fun defineGeofence(context: Context, location: LatLng) {
    // Implement geofencing logic here
}

/*
@Composable
fun NavigationScreen(navController: NavController) {
    val mapView = rememberMapViewWithLifecycle()
    AndroidView({ mapView }) { mapView ->
        mapView.getMapAsync { googleMap ->
            val location = LatLng(-34.0, 151.0)  // Example location
            googleMap.addMarker(MarkerOptions().position(location).title("Marker in Sydney"))
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(location))
        }
    }
}
*/
@Composable
fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val observer = object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                mapView.onCreate(Bundle())
            }

            override fun onStart(owner: LifecycleOwner) {
                mapView.onStart()
            }

            override fun onResume(owner: LifecycleOwner) {
                mapView.onResume()
            }

            override fun onPause(owner: LifecycleOwner) {
                mapView.onPause()
            }

            override fun onStop(owner: LifecycleOwner) {
                mapView.onStop()
            }

            override fun onDestroy(owner: LifecycleOwner) {
                mapView.onDestroy()
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }
    return mapView
}



/*
@Composable
fun NavigationScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("This is the Navigation Screen")
    }
}
*/

/*
@Composable
fun SavedLocationsScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("This is the Saved Locations Screen")
    }
}*/
/*
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    JorgeOviedo_COMP304Lab4_Ex1Theme {
        MyApp()
    }
}*/
