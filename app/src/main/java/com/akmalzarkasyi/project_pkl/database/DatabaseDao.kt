package com.akmalzarkasyi.project_pkl.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.akmalzarkasyi.project_pkl.model.ModelDatabase

@Dao
interface DatabaseDao {
    @Query("SELECT * FROM tbl_absensi")
    fun getAllHistory(): LiveData<List<ModelDatabase>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertData(vararg modelDatabases: ModelDatabase)

    @Query("DELETE FROM tbl_absensi WHERE uid= :uid")
    fun deleteHistoryById(uid: Int)

    @Query("DELETE FROM tbl_absensi")
    fun deleteAllHistory()
}