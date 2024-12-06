package com.jorgeoviedolab4.RoomDB

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "locations")
data class MyLocation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val latitude: Double,
    val longitude: Double
)
