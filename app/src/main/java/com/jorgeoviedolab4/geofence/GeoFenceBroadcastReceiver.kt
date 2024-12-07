package com.jorgeoviedolab4.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent


class GeoFenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (GeofencingEvent.fromIntent(intent)?.hasError() == true) {
            Toast.makeText(context, "Geofence error occurred", Toast.LENGTH_SHORT).show()
            return
        }

        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        val transition = geofencingEvent?.geofenceTransition

        val locationName = intent.getStringExtra("location_name") ?: "Unknown Location"

        if (transition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            Log.d("app", "Entered geofence --- You Are Close To $locationName")
            Toast.makeText(context, "Entered $locationName", Toast.LENGTH_SHORT).show()
        } else if (transition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            Log.d("app", "Exited $locationName")
            Toast.makeText(context, "Exited geofence $locationName", Toast.LENGTH_SHORT).show()
        }
    }
}


