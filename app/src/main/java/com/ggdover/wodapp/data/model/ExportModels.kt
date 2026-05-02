package com.ggdover.wodapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ExportPayload(
    val version: Int = 1,
    val workouts: List<ExportWorkout> = emptyList(),
    val moments: List<ExportMoment> = emptyList(),
    val personalBests: List<ExportPersonalBest> = emptyList(),
)

@Serializable
data class ExportWorkout(
    val id: String,
    val name: String,
    val warmUpJson: String,
    val wodJson: String,
    val strengthJson: String,
    val createdAt: Long,
    val updatedAt: Long,
)

@Serializable
data class ExportMoment(
    val id: String,
    val workoutId: String,
    val performedAt: Long,
    val resultJson: String,
)

@Serializable
data class ExportPersonalBest(
    val id: String,
    val exerciseName: String,
    val reps: Int? = null,
    val weightKg: Double? = null,
    val isBodyweight: Boolean = true,
    val extraNotes: String? = null,
    val achievedAt: Long,
)
