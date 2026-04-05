package com.depi.graduationproject.data.local

import android.content.Context
import androidx.annotation.UiContext
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.depi.graduationproject.data.local.dao.PlateDao
import com.depi.graduationproject.data.local.entity.PlateRecord

@Database(entities = [PlateRecord::class], version = 1, exportSchema = false)
abstract class AppDataBase: RoomDatabase() {
    abstract fun plateDao(): PlateDao

    companion object{
        @Volatile
        private var INSTANCE: AppDataBase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = INSTANCE ?: synchronized(LOCK) {
            INSTANCE ?:createDB(context).also { INSTANCE = it }
        }

        private fun createDB(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            AppDataBase::class.java,
            "plates_database"
        ).build()
    }
}