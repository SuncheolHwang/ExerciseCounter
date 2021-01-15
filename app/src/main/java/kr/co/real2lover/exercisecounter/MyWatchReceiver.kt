package kr.co.real2lover.exercisecounter

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.room.Room
import kr.co.real2lover.exercisecounter.data.RoomHelper
import kr.co.real2lover.exercisecounter.data.RoomRecord
import java.util.*
import kotlin.system.exitProcess

class MyWatchReceiver : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        WatchForeground.timerStop = !WatchForeground.timerStop
    }
}

class MyWatchStopReceiver : BroadcastReceiver() {
    var helper: RoomHelper? = null

    override fun onReceive(context: Context, intent: Intent) {
        val serviceIntent = Intent(context, WatchForeground::class.java)
        context.stopService(serviceIntent)
        MainActivity.pref?.edit()?.apply {
            putString(MainActivity.FOREGROUND_SERVICE_KEY, "from MainActivity")?.commit()
        }

        val exerciseTime = MainActivity.pref?.getLong(MainActivity.EXERCISE_TIME_KEY, 0)

        helper = Room.databaseBuilder(context, RoomHelper::class.java, "room_record")
                .allowMainThreadQueries()
                .build()

        val mCalendar = Calendar.getInstance()
        val strToday = "${mCalendar.get(Calendar.YEAR)}:${mCalendar.get(Calendar.MONTH)}:${mCalendar.get(Calendar.DAY_OF_MONTH)}"

        helper?.roomRecordDao()?.insertAll(RoomRecord(strToday, exerciseTime!!))

        exitProcess(0)
    }
}