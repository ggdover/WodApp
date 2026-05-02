package com.ggdover.wodapp.data.repository

import androidx.room.withTransaction
import com.ggdover.wodapp.data.local.WodDatabase
import com.ggdover.wodapp.data.local.entity.PersonalBestEntity
import com.ggdover.wodapp.data.local.entity.WorkoutEntity
import com.ggdover.wodapp.data.local.entity.WorkoutMomentEntity
import com.ggdover.wodapp.data.model.AppJson
import com.ggdover.wodapp.data.model.ExportMoment
import com.ggdover.wodapp.data.model.ExportPayload
import com.ggdover.wodapp.data.model.ExportPersonalBest
import com.ggdover.wodapp.data.model.ExportWorkout
import com.ggdover.wodapp.data.model.LoggedResult
import com.ggdover.wodapp.data.model.StrengthSection
import com.ggdover.wodapp.data.model.WarmUpSection
import com.ggdover.wodapp.data.model.WodSection
import com.ggdover.wodapp.data.model.buildSearchText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.UUID

data class MomentWithWorkoutName(
    val moment: WorkoutMomentEntity,
    val workoutName: String,
)

class WodRepository(private val db: WodDatabase) {
    private val workouts = db.workoutDao()
    private val moments = db.workoutMomentDao()
    private val pbs = db.personalBestDao()

    fun observeWorkouts(): Flow<List<WorkoutEntity>> = workouts.observeAll()

    fun observeWorkout(id: String): Flow<WorkoutEntity?> = workouts.observeById(id)

    fun observeMoments(): Flow<List<WorkoutMomentEntity>> = moments.observeAll()

    fun observeMomentsWithWorkoutNames(): Flow<List<MomentWithWorkoutName>> =
        combine(observeMoments(), observeWorkouts()) { momentRows, workoutRows ->
            val names = workoutRows.associateBy { it.id }
            momentRows.map { row ->
                MomentWithWorkoutName(
                    moment = row,
                    workoutName = names[row.workoutId]?.name ?: "Unknown workout",
                )
            }
        }

    fun observeMomentsForWorkout(workoutId: String): Flow<List<WorkoutMomentEntity>> =
        moments.observeForWorkout(workoutId)

    fun observePersonalBests(): Flow<List<PersonalBestEntity>> = pbs.observeAll()

    suspend fun getWorkout(id: String): WorkoutEntity? = workouts.getById(id)

    suspend fun getMoment(id: String): WorkoutMomentEntity? = moments.getById(id)

    suspend fun upsertWorkout(
        id: String,
        name: String,
        warmUp: WarmUpSection,
        wod: WodSection,
        strength: StrengthSection,
    ) {
        val now = System.currentTimeMillis()
        val warmJson = AppJson.encodeToString(WarmUpSection.serializer(), warmUp)
        val wodJson = AppJson.encodeToString(WodSection.serializer(), wod)
        val strJson = AppJson.encodeToString(StrengthSection.serializer(), strength)
        val existing = workouts.getById(id)
        val search = buildSearchText(name, warmUp, wod, strength)
        workouts.upsert(
            WorkoutEntity(
                id = id,
                name = name.trim(),
                warmUpJson = warmJson,
                wodJson = wodJson,
                strengthJson = strJson,
                searchText = search,
                createdAt = existing?.createdAt ?: now,
                updatedAt = now,
            ),
        )
    }

    suspend fun deleteWorkout(entity: WorkoutEntity) {
        workouts.delete(entity)
    }

    suspend fun upsertMoment(
        id: String,
        workoutId: String,
        performedAt: Long,
        result: LoggedResult,
    ) {
        moments.upsert(
            WorkoutMomentEntity(
                id = id,
                workoutId = workoutId,
                performedAt = performedAt,
                resultJson = AppJson.encodeToString(LoggedResult.serializer(), result),
            ),
        )
    }

    suspend fun deleteMoment(entity: WorkoutMomentEntity) {
        moments.delete(entity)
    }

    suspend fun upsertPersonalBest(entity: PersonalBestEntity) {
        pbs.upsert(entity)
    }

    suspend fun deletePersonalBest(entity: PersonalBestEntity) {
        pbs.delete(entity)
    }

    fun workoutsMatchingQuery(query: String): Flow<List<WorkoutEntity>> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return observeWorkouts()
        return observeWorkouts().map { list ->
            list.filter { w ->
                w.searchText.contains(q) || w.name.lowercase().contains(q)
            }
        }
    }

    suspend fun exportPayload(): ExportPayload {
        val w = workouts.getAllSnapshot()
        val m = moments.getAllSnapshot()
        val p = pbs.getAllSnapshot()
        return ExportPayload(
            version = 1,
            workouts = w.map {
                ExportWorkout(
                    id = it.id,
                    name = it.name,
                    warmUpJson = it.warmUpJson,
                    wodJson = it.wodJson,
                    strengthJson = it.strengthJson,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt,
                )
            },
            moments = m.map {
                ExportMoment(
                    id = it.id,
                    workoutId = it.workoutId,
                    performedAt = it.performedAt,
                    resultJson = it.resultJson,
                )
            },
            personalBests = p.map {
                ExportPersonalBest(
                    id = it.id,
                    exerciseName = it.exerciseName,
                    reps = it.reps,
                    weightKg = it.weightKg,
                    isBodyweight = it.isBodyweight,
                    extraNotes = it.extraNotes,
                    achievedAt = it.achievedAt,
                )
            },
        )
    }

    suspend fun exportJsonString(): String =
        AppJson.encodeToString(ExportPayload.serializer(), exportPayload())

    /**
     * Replaces all local data with the import payload (per design spec).
     */
    suspend fun importPayload(payload: ExportPayload) {
        db.withTransaction {
            workouts.deleteAll()
            moments.deleteAll()
            pbs.deleteAll()
            payload.workouts.forEach {
                workouts.upsert(
                    WorkoutEntity(
                        id = it.id,
                        name = it.name,
                        warmUpJson = it.warmUpJson,
                        wodJson = it.wodJson,
                        strengthJson = it.strengthJson,
                        searchText = recomputeSearchFromEntity(it),
                        createdAt = it.createdAt,
                        updatedAt = it.updatedAt,
                    ),
                )
            }
            payload.moments.forEach {
                moments.upsert(
                    WorkoutMomentEntity(
                        id = it.id,
                        workoutId = it.workoutId,
                        performedAt = it.performedAt,
                        resultJson = it.resultJson,
                    ),
                )
            }
            payload.personalBests.forEach {
                pbs.upsert(
                    PersonalBestEntity(
                        id = it.id,
                        exerciseName = it.exerciseName,
                        reps = it.reps,
                        weightKg = it.weightKg,
                        isBodyweight = it.isBodyweight,
                        extraNotes = it.extraNotes,
                        achievedAt = it.achievedAt,
                    ),
                )
            }
        }
    }

    suspend fun importJsonString(json: String) {
        val payload = AppJson.decodeFromString(ExportPayload.serializer(), json)
        importPayload(payload)
    }

    companion object {
        fun newId(): String = UUID.randomUUID().toString()

        fun recomputeSearchFromEntity(w: ExportWorkout): String {
            return try {
                val warm = AppJson.decodeFromString(WarmUpSection.serializer(), w.warmUpJson)
                val wod = AppJson.decodeFromString(WodSection.serializer(), w.wodJson)
                val str = AppJson.decodeFromString(StrengthSection.serializer(), w.strengthJson)
                buildSearchText(w.name, warm, wod, str)
            } catch (_: Exception) {
                w.name.lowercase()
            }
        }

        fun parseLoggedResult(json: String): LoggedResult =
            try {
                AppJson.decodeFromString(LoggedResult.serializer(), json)
            } catch (_: Exception) {
                LoggedResult(freeFormNotes = json)
            }

        fun parseWarmUp(json: String): WarmUpSection =
            try {
                AppJson.decodeFromString(WarmUpSection.serializer(), json)
            } catch (_: Exception) {
                WarmUpSection()
            }

        fun parseWod(json: String): WodSection =
            try {
                AppJson.decodeFromString(WodSection.serializer(), json)
            } catch (_: Exception) {
                WodSection()
            }

        fun parseStrength(json: String): StrengthSection =
            try {
                AppJson.decodeFromString(StrengthSection.serializer(), json)
            } catch (_: Exception) {
                StrengthSection()
            }
    }
}
