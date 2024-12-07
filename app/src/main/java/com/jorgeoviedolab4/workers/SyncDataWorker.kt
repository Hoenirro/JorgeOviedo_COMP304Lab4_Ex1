package com.jorgeoviedolab4.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.jorgeoviedolab4.roomDB.LocationDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class SyncDataWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    private val locationDao = LocationDatabase.getDatabase(context).locationDao()

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val locations = locationDao.getAllLocationsSync()
                val json = Gson().toJson(locations)
                val file = File(applicationContext.filesDir, "locations.json")

                if (file.exists()) {
                    file.delete()
                }

                file.writeText(json)

                Result.success()
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure()
            }
        }
    }
}
