package com.akmalzarkasyi.project_pkl.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.akmalzarkasyi.project_pkl.model.ModelDatabase

@Database(entities = [ModelDatabase::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun databaseDao(): DatabaseDao?
}