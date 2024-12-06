package com.jorgeoviedolab4.RoomDB

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface LocationDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLocation(myLocation: MyLocation)

    @Query("SELECT * FROM locations")
    fun getAllLocations(): LiveData<List<MyLocation>>

    @Delete
    suspend fun deleteLocation(myLocation: MyLocation)

    @Update
    suspend fun updateLocation(myLocation: MyLocation)
}
