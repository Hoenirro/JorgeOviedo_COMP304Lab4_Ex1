package com.jorgeoviedolab4

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.gms.location.LocationServices
import com.jorgeoviedolab4.composables.NavigationScreen
import com.jorgeoviedolab4.composables.SavedLocationsScreen
import com.jorgeoviedolab4.viewModel.SavedLocationsViewModel
import com.jorgeoviedolab4.workers.SyncDataWorker

class MainActivity : ComponentActivity() {
    private lateinit var workManager: WorkManager
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true &&
            permissions[Manifest.permission.ACCESS_BACKGROUND_LOCATION] == true &&
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            // Permissions granted
        } else {
            Toast.makeText(this, "Permissions not granted", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        workManager = WorkManager.getInstance(applicationContext)
        requestLocationPermissions()
        setContent {
            MyApp()
        }
    }

    private fun requestLocationPermissions() {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    override fun onPause() {
        super.onPause()
        val request = OneTimeWorkRequestBuilder<SyncDataWorker>()
            .setConstraints(
                Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build() )
            .build()
        workManager.enqueue(request)
    }

}


@Composable
fun MyApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val locationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    val savedLocationsViewModel: SavedLocationsViewModel = viewModel()

    var isRequestingLocation by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            CustomTopBar(
                navController = navController,
                isRequestingLocation = isRequestingLocation,
                onToggleLocationRequest = { isRequestingLocation = !isRequestingLocation }
            )
        },
        content = { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("home") { HomeScreen(navController) }
                composable("navigation") {
                    NavigationScreen(
                        navController,
                        locationProviderClient,
                        savedLocationsViewModel,
                        isRequestingLocation
                    )
                }
                composable("saved_locations") { SavedLocationsScreen(navController, savedLocationsViewModel) }
            }
        }
    )
}




@Composable
fun CustomTopBar(
    navController: NavController,
    isRequestingLocation: Boolean,
    onToggleLocationRequest: () -> Unit
) {
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
        IconButton(onClick = { onToggleLocationRequest() }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                contentDescription = "Toggle Location Request",
                tint = if (isRequestingLocation) Color.Green else Color.Black
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




/*
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    JorgeOviedo_COMP304Lab4_Ex1Theme {
        MyApp()
    }
}*/
