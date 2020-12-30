package kr.co.real2lover.exercisecounter.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query

@Dao
interface RoomRecordDao {
    @Query("SELECT * FROM orm_record")
    fun getAll(): List<RoomRecord>

    @Query("SELECT * FROM orm_record WHERE orm_record.`id` IN (:recordIds)")
    fun loadAllByIds(recordIds: IntArray): List<RoomRecord>

    @Query("SELECT * FROM orm_record WHERE orm_record.`date` IN (:recordDates)")
    fun loadAllByDate(recordDates: Array<String>): List<RoomRecord>

    @Insert(onConflict = REPLACE)
    fun insertAll(vararg record: RoomRecord)

    @Delete
    fun delete(record: RoomRecord)
}