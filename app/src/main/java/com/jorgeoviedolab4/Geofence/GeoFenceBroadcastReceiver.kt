package com.jorgeoviedolab4.Geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent != null) {
            if (geofencingEvent.hasError()) {
                Toast.makeText(context, "Geofence error occurred", Toast.LENGTH_SHORT).show()
                return
            }
        }

        val transition = geofencingEvent?.geofenceTransition
        if (transition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            Toast.makeText(context, "Entered geofence", Toast.LENGTH_SHORT).show()
        } else if (transition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            Toast.makeText(context, "Exited geofence", Toast.LENGTH_SHORT).show()
        }
    }
}
