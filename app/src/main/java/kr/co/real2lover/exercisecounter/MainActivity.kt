package kr.co.real2lover.exercisecounter

import android.os.Bundle
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.coroutines.*
import kr.co.real2lover.exercisecounter.databinding.ActivityMainBinding
import kotlin.coroutines.CoroutineContext


class MainActivity : AppCompatActivity(), CoroutineScope {
    val TAG = "MainActivity"

    private lateinit var binding: ActivityMainBinding

    /**
     * Stop Watch 시간 저장 변수
     * Stop Watch 상태 변수
     */
    private var timeWhenStopped: Long = 0
    private var timerStatus = STOP_WATCH_START
    private var breakTimerStatus = STOP_WATCH_PAUSE
    private lateinit var params: LinearLayout.LayoutParams

    /**
     * Stop Watch 상태 상수
     */
    companion object {
        private const val STOP_WATCH_START = 0
        private const val STOP_WATCH_PAUSE = 1
        private const val STOP_WATCH_CONTINUE = 2
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
    var vibrator: Vibrator? = null
    private val timings = longArrayOf(1000, 1000, 1000, 1000)
    private val amplitudes = intArrayOf(100, 0, 100, 0)
    var alarmTime = "01:00"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator

        params = binding.layoutWatchButton.layoutParams as LinearLayout.LayoutParams

        binding.apply {
            textTimer.setOnChronometerTickListener {
                val time: Long = SystemClock.elapsedRealtime() - it.base
                val h = (time / 3600000).toInt()
                val m = (time - h * 3600000).toInt() / 60000
                val s = (time - h * 3600000 - m * 60000).toInt() / 1000
                val t = (if (h < 10) "0$h" else h).toString() + ":" + (if (m < 10) "0$m" else m) + ":" + if (s < 10) "0$s" else s
                textTimer.text = t
            }
            textTimer.base = SystemClock.elapsedRealtime()
            textTimer.text = "00:00:00"

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
                buttonTimer.text = "START"
            }

            buttonPlus.setOnClickListener {
                layoutBreakTime.visibility = View.GONE
                counter++
                textCounter.text = counter.toString()

                breakTimeCounter(2)
                vibrator?.cancel()
            }

            buttonMinus.setOnClickListener {
                layoutBreakTime.visibility = View.GONE
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

    fun stopWatch() {
        binding.apply {
            when (timerStatus) {
                STOP_WATCH_START -> {
                    textTimer.base = SystemClock.elapsedRealtime()
                    textTimer.start()
                    timerStatus = 1
                    buttonTimer.text = "PAUSE"
                }
                STOP_WATCH_PAUSE -> {
                    timeWhenStopped = textTimer.base - SystemClock.elapsedRealtime()
                    textTimer.stop()
                    timerStatus = 2
                    params.weight = 3F
                    buttonTimer.text = "CONTINUE"
                    buttonReset.visibility = View.VISIBLE
                }
                STOP_WATCH_CONTINUE -> {
                    textTimer.base = SystemClock.elapsedRealtime() + timeWhenStopped
                    textTimer.start()
                    timerStatus = 1
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
                    Log.d(TAG, "$breakTimeCheckCounter do something")
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
                Log.d(TAG, "Vibrate")
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, 0))
            }
        }
    }

    fun breakTimeBtn() {
        binding.apply {
            when (breakTimerStatus) {
                STOP_WATCH_PAUSE -> {
                    timeWhenStopped = textBreakTime.base - SystemClock.elapsedRealtime()
                    textBreakTime.stop()
                    breakTimerStatus = STOP_WATCH_CONTINUE
                    buttonBtStop.text = "CONTINUE"
                    vibrator?.cancel()
                }
                STOP_WATCH_CONTINUE -> {
                    textBreakTime.base = SystemClock.elapsedRealtime() + timeWhenStopped
                    textBreakTime.start()
                    breakTimerStatus = STOP_WATCH_PAUSE
                    buttonBtStop.text = "PAUSE"
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuCalendar -> {

            }
            R.id.menuReset -> {
                vibrator?.cancel()
                counter = 0
                binding.textCounter.text = counter.toString()
                binding.layoutBreakTime.visibility = View.GONE
                job.cancel()
            }
            else -> {

            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    fun dialogOpen() {
        val counterDialog = CounterEditDialog(this, object : CustomDialogClickListener{
            override fun onPositiveClick(value: String) {
                binding.textCounter.text = value
                breakTimeCounter(0)
            }

            override fun onNegativeClick() {
            }

        })

        counterDialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
        counterDialog.show()
    }
}
