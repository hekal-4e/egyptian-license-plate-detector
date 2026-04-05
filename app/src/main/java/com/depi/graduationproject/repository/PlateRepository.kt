package com.depi.graduationproject.repository

import android.util.Log
import com.depi.graduationproject.data.local.dao.PlateDao
import com.depi.graduationproject.data.local.entity.PlateRecord
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PlateRepository(private val plateDao: PlateDao) {

    // Function to retrieve all data (uses Flow for automatic updates)
    fun getAllPlates(): Flow<List<PlateRecord>> {
        return plateDao.getAllPlates()
    }

    // Insert function: Takes only the number, handles time and date internally
    suspend fun insertPlate(plateNumber: String) {
        try {
            val currentTime = System.currentTimeMillis()

            // Date formatting (Day/Month/Year - Hour:Minute AM/PM)
            // Using Locale.ENGLISH to ensure numbers are formatted consistently
            val dateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH)
            val dateDisplay = dateFormat.format(Date(currentTime))

            val plate = PlateRecord(
                plateNumber = plateNumber,
                timestamp = currentTime,
                dateDisplay = dateDisplay
            )

            plateDao.insertPlate(plate)
            Log.d("REPO", "✅ Record inserted successfully: $plate")
        } catch (e: Exception) {
            Log.e("REPO", "❌ Error inserting record into database", e)
            throw e // Re-throw to handle in ViewModel if needed
        }
    }

    // Delete function
    suspend fun deletePlate(id: Int) {
        plateDao.deletePlateById(id)
    }

    // Function to clear the entire history
    suspend fun clearAll() {
        plateDao.deleteAll()
    }
}