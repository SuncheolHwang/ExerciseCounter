package kr.co.real2lover.exercisecounter

import android.app.*
import android.content.Context
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

    companion object {
        const val ACTION_STATUS = "WatchForeground.ACTION_STATUS"
        const val IS_TIMER_STOP = "WatchForeground.TIMER_STATUS"
        const val FOREGROUND_TIMER = "WatchForeground.TIMER"
        const val ACTION_PAUSE = "PAUSE"
        const val ACTION_STOP = "STOP"

        var timerStop = false
    }

    override fun onBind(intent: Intent): IBinder {
        return Binder()
    }

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)
                    .createNotificationChannel(serviceChannel)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        timerStop = false

        serviceJob = Job()

        createNotificationChannel()

        //MainActivity 로 부터 timer 수신
        var timer = intent?.getLongExtra(MainActivity.EXERCISE_TIME_KEY, 0)
        var strTimer = timer?.let { watchTimer.convertLongToTime(it) }

        //Notification 알림 생성
        val builder = createNotification(strTimer, false)
        val pauseBuilder = createNotification(strTimer, true)

        //Foreground 시작
        startForeground(ONGOING_NOTIFICATION_ID, builder.build())

        NotificationManagerCompat.from(this).apply {
            notify(ONGOING_NOTIFICATION_ID, builder.build())
            launch {
                while (true) {
                    if (!timerStop) {
                        Log.d(MainActivity.TAG, "timer: $timer")
                        strTimer = timer?.let { watchTimer.convertLongToTime(it) }
                        builder.setContentText(strTimer)
                        notify(ONGOING_NOTIFICATION_ID, builder.build())
                        timer = timer?.plus(1000L)
                    } else {
                        Log.d(MainActivity.TAG, "timer: $timer")
                        pauseBuilder.setContentText(strTimer)
                        notify(ONGOING_NOTIFICATION_ID, pauseBuilder.build())
                    }
                    setContentIntent(this@WatchForeground, pauseBuilder, timer)

                    //MainActivity 로 data 전달
                    MainActivity.pref?.edit()?.apply {
                        putBoolean(MainActivity.TIMER_STATUS_KEY, timerStop)
                        putLong(MainActivity.EXERCISE_TIME_KEY, timer!!)?.commit()
                        putString(MainActivity.FOREGROUND_SERVICE_KEY, "from Service")?.commit()
                    }
                    delay(1000)
                }
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        Log.d(MainActivity.TAG, "service onDestroy() 호출")
        serviceJob.cancel()
        super.onDestroy()
    }

    fun createNotification(timer: String?, isTimerStop: Boolean) : NotificationCompat.Builder {
        val pendingIntent: PendingIntent = Intent(this, MainActivity::class.java).let { notificationIntent ->
            notificationIntent.putExtra(IS_TIMER_STOP, timerStop)
            PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val pausePendingIntent: PendingIntent = Intent(this, MyWatchReceiver::class.java).let { notificationIntent ->
            notificationIntent.putExtra(ACTION_STATUS, "PAUSE")
            PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val stopPendingIntent: PendingIntent = Intent(this, MyWatchStopReceiver::class.java).let { notificationIntent ->
            notificationIntent.putExtra(ACTION_STATUS, "STOP")
            PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        return NotificationCompat.Builder(this, CHANNEL_ID).apply {
            setContentTitle(getText(R.string.notification_title))
            setContentText(timer)
            setSmallIcon(R.drawable.ic_forground)
            setContentIntent(pendingIntent)
            setShowWhen(false)
            setTicker(getText(R.string.ticker_text))
            setAutoCancel(true)
            addAction(R.drawable.ic_pause, if(isTimerStop) getString(R.string.watch_continue) else getString(R.string.pause),
                    pausePendingIntent)
            addAction(R.drawable.ic_pause, getString(R.string.stop), stopPendingIntent)
        }
    }

    suspend fun setContentIntent(context: Context, builder: NotificationCompat.Builder, timer: Long?) =
            withContext(Dispatchers.Main) {
                val pendingIntent: PendingIntent = Intent(context, MainActivity::class.java).let { notificationIntent ->
                    notificationIntent
                            .putExtra(IS_TIMER_STOP, timerStop)
                            .putExtra(FOREGROUND_TIMER, timer)
                    PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                }
                builder.setContentIntent(pendingIntent)
            }
}