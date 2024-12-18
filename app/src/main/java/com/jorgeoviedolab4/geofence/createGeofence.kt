package com.jorgeoviedolab4.geofence

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng

fun createGeofence(geofenceId: String, latLng: LatLng, radius: Float): Geofence {
    return Geofence.Builder()
        .setRequestId(geofenceId)
        .setCircularRegion(latLng.latitude, latLng.longitude, radius)
        .setExpirationDuration(Geofence.NEVER_EXPIRE)
        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
        .build()
}

fun buildGeofencingRequest(geofence: Geofence): GeofencingRequest {
    return GeofencingRequest.Builder()
        .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
        .addGeofence(geofence)
        .build()
}

fun addGeofence(context: Context, geofence: Geofence, locationName: String) {
    val geofencingClient = LocationServices.getGeofencingClient(context)
    val geofencingRequest = buildGeofencingRequest(geofence)
    val intent = Intent(context, GeoFenceBroadcastReceiver::class.java).apply {
        action = "com.google.android.gms.location.Geofence.ACTION_GEOFENCE_EVENT"
        putExtra("location_name", locationName)
        putExtra("geofence_id", geofence.requestId)
    }
    val geofencePendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
    )

    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        return
    }
    geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)
        .addOnSuccessListener {
            Toast.makeText(context, "Geofence added", Toast.LENGTH_LONG).show()
            Log.d("app", "Geofence added")
        }
        .addOnFailureListener {
            Toast.makeText(context, "Failed to add geofence", Toast.LENGTH_LONG).show()
        }
}




fun removeGeofence(context: Context, geofenceId: String) {
    val geofencingClient = LocationServices.getGeofencingClient(context)
    geofencingClient.removeGeofences(listOf(geofenceId))
        .addOnSuccessListener {
            Toast.makeText(context, "Geofence removed", Toast.LENGTH_LONG).show()
            Log.d("Geofence", "Geofence removed with ID: $geofenceId")
        }
        .addOnFailureListener {
            Toast.makeText(context, "Failed to remove geofence", Toast.LENGTH_LONG).show()
            Log.e("Geofence", "Failed to remove geofence with ID: $geofenceId")
        }
}

