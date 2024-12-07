package com.jorgeoviedolab4.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.jorgeoviedolab4.roomDB.MyLocation
import com.jorgeoviedolab4.roomDB.LocationDatabase
import kotlinx.coroutines.launch

class SavedLocationsViewModel(application: Application) : AndroidViewModel(application) {
    private val locationDao = LocationDatabase.getDatabase(application).locationDao()
    val allLocations: LiveData<List<MyLocation>> = locationDao.getAllLocations()

    fun insertLocation(myLocation: MyLocation) {
        viewModelScope.launch {
            locationDao.insertLocation(myLocation)
        }
    }

    fun deleteLocation(myLocation: MyLocation) {
        viewModelScope.launch {
            locationDao.deleteLocation(myLocation)
        }
    }

    fun updateLocation(myLocation: MyLocation) {
        viewModelScope.launch {
            locationDao.updateLocation(myLocation)
        }
    }

    fun getLocationByGeofenceId(geofenceId: String): MyLocation? {
        val coordinates = geofenceId.removePrefix("GEOFENCE_ID_").split("_").map { it.toDouble() }
        return locationDao.getLocationByCoordinates(coordinates[0], coordinates[1])
    }
}
