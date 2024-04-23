package com.example.mc1

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface OrientationDao {
    @Insert
    suspend fun insert(orientation: OrientationEntity)

    @Query("SELECT * FROM OrientationEntity ORDER BY timestamp DESC")
    fun getAllOrientations(): List<OrientationEntity>
}
