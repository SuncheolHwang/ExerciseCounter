package kr.co.real2lover.exercisecounter

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import kotlin.system.exitProcess

class MyWatchReceiver : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        WatchForeground.timerStop = !WatchForeground.timerStop
    }
}

class MyWatchStopReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val serviceIntent = Intent(context, WatchForeground::class.java)
        context.stopService(serviceIntent)
        MainActivity.pref?.edit()?.apply {
            putString(MainActivity.FOREGROUND_SERVICE_KEY, "from MainActivity")?.commit()
        }
        exitProcess(0)
    }
}