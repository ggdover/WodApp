package com.ggdover.wodapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ggdover.wodapp.data.local.dao.PersonalBestDao
import com.ggdover.wodapp.data.local.dao.WorkoutDao
import com.ggdover.wodapp.data.local.dao.WorkoutMomentDao
import com.ggdover.wodapp.data.local.entity.PersonalBestEntity
import com.ggdover.wodapp.data.local.entity.WorkoutEntity
import com.ggdover.wodapp.data.local.entity.WorkoutMomentEntity

@Database(
    entities = [
        WorkoutEntity::class,
        WorkoutMomentEntity::class,
        PersonalBestEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class WodDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
    abstract fun workoutMomentDao(): WorkoutMomentDao
    abstract fun personalBestDao(): PersonalBestDao

    companion object {
        fun build(context: Context): WodDatabase =
            Room.databaseBuilder(context, WodDatabase::class.java, "wod_app.db")
                .fallbackToDestructiveMigration()
                .build()
    }
}
