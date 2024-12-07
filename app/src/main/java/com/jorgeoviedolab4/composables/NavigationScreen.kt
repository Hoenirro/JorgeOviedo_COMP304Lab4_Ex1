package com.jorgeoviedolab4.composables

import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.jorgeoviedolab4.geofence.GeoFenceBroadcastReceiver
import com.jorgeoviedolab4.roomDB.MyLocation
import com.jorgeoviedolab4.viewModel.SavedLocationsViewModel
import com.jorgeoviedolab4.geofence.addGeofence
import com.jorgeoviedolab4.geofence.createGeofence
import com.jorgeoviedolab4.geofence.removeGeofence

@Composable
fun NavigationScreen(
    navController: NavController,
    locationProviderClient: FusedLocationProviderClient,
    savedLocationsViewModel: SavedLocationsViewModel,
    isRequestingLocation: Boolean
) {
    val context = LocalContext.current

    // Define the geofencePendingIntent
    val geofencePendingIntent = remember {
        PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, GeoFenceBroadcastReceiver::class.java).apply {
                action = "com.google.android.gms.location.Geofence.ACTION_GEOFENCE_EVENT"
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 15f)
    }

    LaunchedEffect(isRequestingLocation) {
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

        if (isRequestingLocation) {
            try {
                locationProviderClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    context.mainLooper
                )
            } catch (e: SecurityException) {
                Log.e("Geofence", "Permission error: ${e.message}")
            } catch (e: Exception) {
                Log.e("Geofence", "Error requesting location updates: ${e.message}")
            }
        } else {
            locationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    var markers by remember { mutableStateOf(listOf<LatLng>()) }
    var geofencedLocations by remember { mutableStateOf(setOf<String>()) }
    val (showDialog, setShowDialog) = remember { mutableStateOf(false) }
    val (currentGeofenceId, setCurrentGeofenceId) = remember { mutableStateOf("") }
    val (locationName, setLocationName) = remember { mutableStateOf("") }
    val (selectedLocation, setSelectedLocation) = remember { mutableStateOf<LatLng?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.weight(4f / 5f),
            cameraPositionState = cameraPositionState,
            onMapClick = { latLng ->
                markers = markers + latLng
                setSelectedLocation(latLng)
                setCurrentGeofenceId("GEOFENCE_ID_${latLng.latitude}_${latLng.longitude}")
                setShowDialog(true)
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
                val geofenceId = "GEOFENCE_ID_${location.latitude}_${location.longitude}"
                val isGeofenced = geofencedLocations.contains(geofenceId)

                val displayText = if (location.name == "Custom Location") {
                    "%.4f, %.4f".format(location.latitude, location.longitude)
                } else {
                    location.name
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(displayText)
                    Row {
                        IconButton(onClick = {
                            if (isGeofenced) {
                                // Remove geofence
                                removeGeofence(context, geofenceId)
                                geofencedLocations = geofencedLocations - geofenceId
                                savedLocationsViewModel.updateLocation(location.copy(name = "Custom Location"))
                            } else {
                                // Add geofence
                                val geofence = createGeofence(geofenceId, LatLng(location.latitude, location.longitude), 200f)
                                addGeofence(context, geofence, location.name)
                                geofencedLocations = geofencedLocations + geofenceId
                                Toast.makeText(context, "Geofence added", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Text(
                                "Geo",
                                color = if (isGeofenced) Color.Green else Color.Black
                            )
                        }
                        IconButton(onClick = {
                            if (isGeofenced) {
                                removeGeofence(context, geofenceId)
                                geofencedLocations = geofencedLocations - geofenceId
                            }
                            savedLocationsViewModel.deleteLocation(location)
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { setShowDialog(false) },
                title = { Text("Name Your Location") },
                text = {
                    TextField(
                        value = locationName,
                        onValueChange = { setLocationName(it) },
                        label = { Text("Location Name") }
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        selectedLocation?.let { latLng ->
                            val newLocation = MyLocation(
                                name = locationName,
                                latitude = latLng.latitude,
                                longitude = latLng.longitude
                            )
                            savedLocationsViewModel.insertLocation(newLocation)
                        }
                        setShowDialog(false)
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    Button(onClick = { setShowDialog(false) }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}