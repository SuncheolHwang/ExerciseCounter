package kr.co.real2lover.exercisecounter.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.prolificinteractive.materialcalendarview.CalendarDay

@Entity(tableName = "orm_record")
class RoomRecord {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo
    var id: Int? = null

    @ColumnInfo
    var date: String? = null

    @ColumnInfo
    var time: Long = 0

    constructor(date: String, time: Long) {
        this.date = date
        this.time = time
    }
}