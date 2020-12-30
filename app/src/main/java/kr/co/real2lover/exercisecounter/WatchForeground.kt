package kr.co.real2lover.exercisecounter

import android.app.*
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class WatchForeground : Service(), CoroutineScope {

    /**
     * For Coroutine
     */
    private lateinit var serviceJob: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + serviceJob

    val CHANNEL_ID = "ForegroundChannel"
    val ONGOING_NOTIFICATION_ID = 1
    val watchTimer = WatchTimer()

    override fun onBind(intent: Intent): IBinder {
        return Binder()
    }

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            )

            getSystemService(NotificationManager::class.java)
                    .createNotificationChannel(serviceChannel)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        serviceJob = Job()

        createNotificationChannel()

        var timer = intent?.getLongExtra(MainActivity.EXERCISE_TIME_KEY, 0)
        var strTimer = timer?.let { watchTimer.convertLongToTime(it) }

        val notificationIntent = Intent(this, MainActivity::class.java)
        notificationIntent.putExtra(MainActivity.EXERCISE_TIME_KEY, timer)

        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID).apply {
            setContentTitle(getText(R.string.notification_title))
            setContentText(strTimer)
            setSmallIcon(R.drawable.ic_forground)
            setContentIntent(pendingIntent)
            setTicker(getText(R.string.ticker_text))
            setAutoCancel(true)
        }

        startForeground(ONGOING_NOTIFICATION_ID, builder.build())

        NotificationManagerCompat.from(this).apply {
            notify(ONGOING_NOTIFICATION_ID, builder.build())

            launch {
                while (true) {
                    delay(1000)
                    timer = timer?.plus(1000L)
                    Log.d(MainActivity.TAG, "timer: $timer")
                    strTimer = timer?.let { watchTimer.convertLongToTime(it) }
                    builder.setContentText(strTimer)
                    notify(ONGOING_NOTIFICATION_ID, builder.build())
                }
            }
            notify(ONGOING_NOTIFICATION_ID, builder.build())
        }

        return START_STICKY
    }

    override fun onDestroy() {
        Log.d(MainActivity.TAG, "service onDestroy() 호출")
        serviceJob.cancel()
        super.onDestroy()
    }
}