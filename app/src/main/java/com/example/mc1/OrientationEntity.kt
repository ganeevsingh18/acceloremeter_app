package com.example.mc1
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class OrientationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val x: Float,
    val y: Float,
    val z: Float
)
