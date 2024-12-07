package com.jorgeoviedolab4.roomDB

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface LocationDao {
    @Query("SELECT * FROM locations WHERE latitude = :latitude AND longitude = :longitude LIMIT 1")
    fun getLocationByCoordinates(latitude: Double, longitude: Double): MyLocation?

    @Query("SELECT * FROM locations")
    fun getAllLocationsSync(): List<MyLocation>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: MyLocation)

    @Delete
    suspend fun deleteLocation(location: MyLocation)

    @Update
    suspend fun updateLocation(location: MyLocation)

    @Query("SELECT * FROM locations")
    fun getAllLocations(): LiveData<List<MyLocation>>
}

