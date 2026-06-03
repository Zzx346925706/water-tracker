package com.drink.watertracker.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

data class DailyTotal(
    val date: String,
    val total: Int
)

@Entity(tableName = "water_records")
data class WaterRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Int,           // ml
    val timestamp: Long,       // epoch millis
    val date: String           // yyyy-MM-dd
)

@Dao
interface WaterDao {
    @Insert
    suspend fun insert(record: WaterRecord)

    @Query("SELECT * FROM water_records WHERE date = :date ORDER BY timestamp DESC")
    fun getRecordsByDate(date: String): Flow<List<WaterRecord>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM water_records WHERE date = :date")
    fun getTotalByDate(date: String): Flow<Int>

    @Query("DELETE FROM water_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT DISTINCT date FROM water_records ORDER BY date DESC LIMIT 30")
    fun getRecentDates(): Flow<List<String>>

    @Query("SELECT date, COALESCE(SUM(amount), 0) as total FROM water_records WHERE date >= :startDate GROUP BY date ORDER BY date ASC")
    fun getDailyTotals(startDate: String): Flow<List<DailyTotal>>
}

@Database(entities = [WaterRecord::class], version = 1, exportSchema = false)
abstract class WaterDatabase : RoomDatabase() {
    abstract fun waterDao(): WaterDao

    companion object {
        @Volatile
        private var INSTANCE: WaterDatabase? = null

        fun getDatabase(context: android.content.Context): WaterDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WaterDatabase::class.java,
                    "water_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
