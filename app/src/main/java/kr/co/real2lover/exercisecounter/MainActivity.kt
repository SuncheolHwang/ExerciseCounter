package kr.co.real2lover.exercisecounter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.*
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.prolificinteractive.materialcalendarview.CalendarDay
import kotlinx.coroutines.*
import kr.co.real2lover.exercisecounter.data.RoomHelper
import kr.co.real2lover.exercisecounter.data.RoomRecord
import kr.co.real2lover.exercisecounter.databinding.ActivityMainBinding
import java.util.*
import kotlin.coroutines.CoroutineContext


class MainActivity : AppCompatActivity(), CoroutineScope {

    private lateinit var binding: ActivityMainBinding

    var pref: SharedPreferences? = null
    var savedTime: Long = 0L
    var savedDate: String = ""

    /**
     * Stop Watch 시간 저장 변수
     * Stop Watch 상태 변수
     */
    private var timeWhenStopped: Long = 0
    private var breakTimeWhenStopped: Long = 0
    private var timerStatus = STOP_WATCH_START
    private var breakTimerStatus = STOP_WATCH_PAUSE
    private lateinit var params: LinearLayout.LayoutParams

    /**
     * Stop Watch 상태 상수
     */
    companion object {
        const val TAG = "MainActivity"
        private const val STOP_WATCH_START = 0
        private const val STOP_WATCH_PAUSE = 1
        private const val STOP_WATCH_CONTINUE = 2
        private const val SHOW_PREFERENCE = 101
        private const val SHOW_MY_CALENDAR = 102

        val PENDING_INTENT_FALG = 201

        /*
         * for Bundle Key
         */
        const val EXERCISE_TIME_KEY = "EXERCISE_TIME_KEY"
        const val DATE_KEY = "DATE_KEY"
    }

    /**
     * Counter 변수
     */
    private var counter = 0

    /**
     * For Coroutine
     */
    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job


    /**
     * for Vibrator
     */
    var alarmTime = "00:30"
    var vibrator: Vibrator? = null
    private val timings = longArrayOf(1000, 1000, 1000, 1000)
    private val amplitudes = intArrayOf(100, 0, 100, 0)

    /**
     * for RoomDatabase
     */
    var helper: RoomHelper? = null
    private val mCalendar = Calendar.getInstance()
    var thisYear = 0
    var thisMonth = 0
    var today = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "onCreate() 호출")

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        helper = Room.databaseBuilder(this, RoomHelper::class.java, "room_record")
            .allowMainThreadQueries()
            .build()
        thisYear = mCalendar.get(Calendar.YEAR)
        thisMonth = mCalendar.get(Calendar.MONTH)
        today = mCalendar.get(Calendar.DAY_OF_MONTH)

        vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator

        //Button 의 크기 수정을 위한 params 얻기
        params = binding.layoutWatchButton.layoutParams as LinearLayout.LayoutParams

        //설정 fragment 의 Data 읽기
        pref = application?.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)

        //같은 날짜에서는 운동시간 저장
        savedDate = pref?.getString(DATE_KEY, "00:00:00").toString()
        val strDate = "${thisYear}:${thisMonth}:${today}"

        savedTime = if (intent.flags != PENDING_INTENT_FALG) {
            Log.d(TAG, "intent == null")
            if (savedDate == strDate) pref?.getLong(EXERCISE_TIME_KEY, 0) ?: 0 else 0
        } else {
            Log.d(TAG, "intent != null")
            intent.getLongExtra(EXERCISE_TIME_KEY, 0)
        }

        savedTime = if (savedDate == strDate) pref?.getLong(EXERCISE_TIME_KEY, 0) ?: 0 else 0

        binding.apply {
            textTimer.setOnChronometerTickListener {
                val time: Long = SystemClock.elapsedRealtime() - it.base
                textTimer.text = convertLongToTime(time)
            }
            textTimer.base = SystemClock.elapsedRealtime() + savedTime
            textTimer.text = convertLongToTime(savedTime)

            buttonTimer.setOnClickListener {
                stopWatch()
            }

            buttonReset.setOnClickListener {
                textTimer.base = SystemClock.elapsedRealtime()
                textTimer.stop()
                timeWhenStopped = 0
                textTimer.text = "00:00:00"
                params.weight = 2F
                buttonReset.visibility = View.GONE
                textExTime.visibility = View.GONE
                buttonTimer.text = "START"
            }

            buttonPlus.setOnClickListener {
                breakTimeBtnReset()
                layoutBreakTime.visibility = View.GONE
                textExTime.visibility = View.GONE
                counter++
                textCounter.text = counter.toString()

                breakTimeCounter(2)
                vibrator?.cancel()
            }

            buttonMinus.setOnClickListener {
                breakTimeBtnReset()
                layoutBreakTime.visibility = View.GONE
                textExTime.visibility = View.GONE
                counter--
                textCounter.text = counter.toString()

                breakTimeCounter(2)
                vibrator?.cancel()
            }

            textBreakTime.setOnChronometerTickListener {
                val time: Long = SystemClock.elapsedRealtime() - it.base
                val h = (time / 3600000).toInt()
                val m = (time - h * 3600000).toInt() / 60000
                val s = (time - h * 3600000 - m * 60000).toInt() / 1000
                val t = (if (m < 10) "0$m" else m).toString() + ":" + if (s < 10) "0$s" else s
                textBreakTime.text = t

                timeIsUp(t)
            }
            textBreakTime.base = SystemClock.elapsedRealtime()
            textBreakTime.text = "00:00"

            buttonBtStop.setOnClickListener {
                breakTimeBtn()
            }

            textCounter.setOnLongClickListener {
                dialogOpen()
                true
            }
        }

        job = Job()
    }

    override fun onResume() {
        super.onResume()
        alarmTime = pref?.getString(getString(R.string.setting_alarm_time), "00:30").toString()
        Toast.makeText(this, alarmTime, Toast.LENGTH_SHORT).show()

        val serviceIntent = Intent(this, WatchForeground::class.java)
        stopService(serviceIntent)
    }

    fun stopWatch() {
        binding.apply {
            when (timerStatus) {
                STOP_WATCH_START -> {
                    textTimer.base = SystemClock.elapsedRealtime() - savedTime
                    textTimer.start()
                    timerStatus = STOP_WATCH_PAUSE
                    buttonTimer.text = "PAUSE"
                }
                STOP_WATCH_PAUSE -> {
                    timeWhenStopped = textTimer.base - SystemClock.elapsedRealtime()
                    textTimer.stop()
                    timerStatus = STOP_WATCH_CONTINUE
                    params.weight = 3F
                    buttonTimer.text = "CONTINUE"
                    buttonReset.visibility = View.VISIBLE
                }
                STOP_WATCH_CONTINUE -> {
                    textTimer.base = SystemClock.elapsedRealtime() + timeWhenStopped
                    textTimer.start()
                    timerStatus = STOP_WATCH_PAUSE
                    params.weight = 2F
                    buttonTimer.text = "PAUSE"
                    buttonReset.visibility = View.GONE
                }
            }
        }
    }

    fun breakTimeCounter(time: Int) {
        binding.apply {
            job.cancel()
            textBreakTime.base = SystemClock.elapsedRealtime()
            textBreakTime.stop()

            job = Job()
            var breakTimeCheckCounter = 0
            launch {
                while (true) {
                    delay(1000)

                    if (breakTimeCheckCounter >= time) {
                        layoutBreakTime.visibility = View.VISIBLE
                        textBreakTime.start()
                        break
                    }
                    breakTimeCheckCounter++
                }
            }
        }
    }

    fun timeIsUp(time: String) {
        if (time == alarmTime) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

//                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, 0))
                binding.textExTime.visibility = View.VISIBLE
            }
        }
    }

    fun breakTimeBtn() {
        binding.apply {
            when (breakTimerStatus) {
                STOP_WATCH_PAUSE -> {
                    breakTimeWhenStopped = textBreakTime.base - SystemClock.elapsedRealtime()
                    textBreakTime.stop()
                    breakTimerStatus = STOP_WATCH_CONTINUE
                    buttonBtStop.text = "CONTINUE"
                    vibrator?.cancel()
                }
                STOP_WATCH_CONTINUE -> {
                    textBreakTime.base = SystemClock.elapsedRealtime() + breakTimeWhenStopped
                    textBreakTime.start()
                    breakTimerStatus = STOP_WATCH_PAUSE
                    buttonBtStop.text = "PAUSE"
                }
            }
        }
    }

    fun breakTimeBtnReset() {
        breakTimerStatus = STOP_WATCH_PAUSE
        binding.buttonBtStop.text = "PAUSE"
        binding.textBreakTime.stop()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuCalendar -> {
                myCalendarStart()
            }

            R.id.menuReset -> {
                vibrator?.cancel()
                counter = 0
                binding.textCounter.text = counter.toString()
                binding.layoutBreakTime.visibility = View.GONE
                job.cancel()
            }

            else -> {
                SettingFragmentStart()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    fun dialogOpen() {
        val counterDialog = CounterEditDialog(this, object : CustomDialogClickListener {
            override fun onPositiveClick(value: String) {
                breakTimeBtnReset()
                binding.textCounter.text = value
                counter = value.toInt()
                breakTimeCounter(0)
            }

            override fun onNegativeClick() {
            }

        })

        counterDialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
        counterDialog.show()
    }

    fun SettingFragmentStart() {
        val intent = Intent(this, SettingActivity::class.java)
        startActivityForResult(intent, SHOW_PREFERENCE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            Activity.RESULT_OK -> {
                if (requestCode == SHOW_PREFERENCE) {

                    val alarmTime = pref?.getString("alarm_time", "00:30")
                    Toast.makeText(this, alarmTime, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun myCalendarStart() {
        val intent = Intent(this, MyCalendar::class.java)
        val exerciseTime = when {
            (timerStatus == STOP_WATCH_START) -> savedTime
            (timerStatus == STOP_WATCH_PAUSE) -> SystemClock.elapsedRealtime() - binding.textTimer.base
            else -> -timeWhenStopped
        }
        val strDate = "${thisYear}:${thisMonth}:${today}"
        helper?.roomRecordDao()?.insertAll(RoomRecord(strDate, exerciseTime))

        intent.putExtra("ExerciseTime", exerciseTime)
        startActivityForResult(intent, SHOW_MY_CALENDAR)
    }

    fun convertLongToTime(time: Long): String {
        val h = (time / 3600000).toInt()
        val m = (time - h * 3600000).toInt() / 60000
        val s = (time - h * 3600000 - m * 60000).toInt() / 1000
        return (if (h < 10) "0$h" else h).toString() + ":" + (if (m < 10) "0$m" else m) + ":" + if (s < 10) "0$s" else s
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop() 호출")

        val exerciseTime = when {
            (timerStatus == STOP_WATCH_START) -> 0
            (timerStatus == STOP_WATCH_PAUSE) -> SystemClock.elapsedRealtime() - binding.textTimer.base
            else -> -timeWhenStopped
        }
        val strDate = "${thisYear}:${thisMonth}:${today}"

        pref?.edit()?.run {
            putLong(EXERCISE_TIME_KEY, exerciseTime).commit()
            putString(DATE_KEY, strDate).commit()
        }

        helper?.roomRecordDao()?.insertAll(RoomRecord(strDate, exerciseTime))

        if (timerStatus == STOP_WATCH_PAUSE) {
            val serviceIntent = Intent(this, WatchForeground::class.java)
            serviceIntent.putExtra(EXERCISE_TIME_KEY, exerciseTime)
            startForegroundService(serviceIntent)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        Log.d(TAG, "onNewIntent() 호출")

        super.onNewIntent(intent)
    }
}
