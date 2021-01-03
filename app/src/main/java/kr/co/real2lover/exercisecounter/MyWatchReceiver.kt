package kr.co.real2lover.exercisecounter

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class MyWatchReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(MainActivity.TAG, "onReceive() 호출")
        if (intent.getStringExtra(WatchForeground.ACTION_STATUS) == "PAUSE") {
            Log.d(MainActivity.TAG, "pause 호출")
            WatchForeground.timerStop = true
        } else if (intent.getStringExtra(WatchForeground.ACTION_STOP) == "STOP") {
            Log.d(MainActivity.TAG, "stop 호출")
            val serviceIntent = Intent(context, WatchForeground::class.java)
            context.stopService(serviceIntent)
        }
    }
}