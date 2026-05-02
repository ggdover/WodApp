package com.ggdover.wodapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey val id: String,
    val name: String,
    val warmUpJson: String,
    val wodJson: String,
    val strengthJson: String,
    val searchText: String,
    val createdAt: Long,
    val updatedAt: Long,
)

@Entity(
    tableName = "workout_moments",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("workoutId"),
        Index("performedAt"),
    ],
)
data class WorkoutMomentEntity(
    @PrimaryKey val id: String,
    val workoutId: String,
    val performedAt: Long,
    val resultJson: String,
)

@Entity(tableName = "personal_bests")
data class PersonalBestEntity(
    @PrimaryKey val id: String,
    val exerciseName: String,
    val reps: Int?,
    val weightKg: Double?,
    val isBodyweight: Boolean,
    val extraNotes: String?,
    val achievedAt: Long,
)
