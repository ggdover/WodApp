package com.ggdover.wodapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ggdover.wodapp.data.local.entity.PersonalBestEntity
import com.ggdover.wodapp.data.local.entity.WorkoutEntity
import com.ggdover.wodapp.data.local.entity.WorkoutMomentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workouts ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts")
    suspend fun getAllSnapshot(): List<WorkoutEntity>

    @Query("SELECT * FROM workouts WHERE id = :id")
    suspend fun getById(id: String): WorkoutEntity?

    @Query("SELECT * FROM workouts WHERE id = :id")
    fun observeById(id: String): Flow<WorkoutEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: WorkoutEntity)

    @Delete
    suspend fun delete(entity: WorkoutEntity)

    @Query("DELETE FROM workouts")
    suspend fun deleteAll()
}

@Dao
interface WorkoutMomentDao {
    @Query("SELECT * FROM workout_moments ORDER BY performedAt DESC")
    fun observeAll(): Flow<List<WorkoutMomentEntity>>

    @Query("SELECT * FROM workout_moments")
    suspend fun getAllSnapshot(): List<WorkoutMomentEntity>

    @Query("SELECT * FROM workout_moments WHERE workoutId = :workoutId ORDER BY performedAt DESC")
    fun observeForWorkout(workoutId: String): Flow<List<WorkoutMomentEntity>>

    @Query("SELECT * FROM workout_moments WHERE id = :id")
    suspend fun getById(id: String): WorkoutMomentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: WorkoutMomentEntity)

    @Delete
    suspend fun delete(entity: WorkoutMomentEntity)

    @Query("DELETE FROM workout_moments")
    suspend fun deleteAll()
}

@Dao
interface PersonalBestDao {
    @Query("SELECT * FROM personal_bests ORDER BY exerciseName ASC, achievedAt DESC")
    fun observeAll(): Flow<List<PersonalBestEntity>>

    @Query("SELECT * FROM personal_bests")
    suspend fun getAllSnapshot(): List<PersonalBestEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: PersonalBestEntity)

    @Delete
    suspend fun delete(entity: PersonalBestEntity)

    @Query("DELETE FROM personal_bests")
    suspend fun deleteAll()
}
