package com.example.fitnessapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete
import com.example.fitnessapp.data.model.WorkoutHistory
import kotlinx.coroutines.flow.Flow

// DAO (Data Access Object) - interfejs dostępu do bazy danych
@Dao
interface WorkoutDao {

    @Query("SELECT * FROM workout_history ORDER BY dateTimestamp DESC")
    fun getAllWorkouts(): Flow<List<WorkoutHistory>>

    @Query("SELECT * FROM workout_history WHERE id = :workoutId")
    suspend fun getWorkoutById(workoutId: Long): WorkoutHistory?

    @Query("SELECT * FROM workout_history ORDER BY dateTimestamp DESC LIMIT 1")
    suspend fun getLastWorkout(): WorkoutHistory?

    @Insert
    suspend fun insertWorkout(workout: WorkoutHistory): Long

    @Delete
    suspend fun deleteWorkout(workout: WorkoutHistory)

    @Query("DELETE FROM workout_history")
    suspend fun deleteAllWorkouts()

    // Statystyki
    @Query("SELECT SUM(distance) FROM workout_history")
    suspend fun getTotalDistance(): Float?

    @Query("SELECT SUM(calories) FROM workout_history")
    suspend fun getTotalCalories(): Int?

    @Query("SELECT COUNT(*) FROM workout_history")
    suspend fun getTotalWorkoutCount(): Int
}

// Room Database
@Database(
    entities = [WorkoutHistory::class],
    version = 1,
    exportSchema = false
)
abstract class WorkoutDatabase : RoomDatabase() {

    abstract fun workoutDao(): WorkoutDao

    companion object {
        @Volatile
        private var INSTANCE: WorkoutDatabase? = null

        fun getDatabase(context: Context): WorkoutDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WorkoutDatabase::class.java,
                    "workout_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}