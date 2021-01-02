package kr.co.real2lover.exercisecounter.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [RoomRecord::class], version = 1, exportSchema = false)
abstract class RoomHelper : RoomDatabase() {
    abstract fun roomRecordDao(): RoomRecordDao
}