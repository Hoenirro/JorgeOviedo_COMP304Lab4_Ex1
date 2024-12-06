package com.jorgeoviedolab4.ViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.jorgeoviedolab4.RoomDB.MyLocation
import com.jorgeoviedolab4.RoomDB.LocationDatabase
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
}
