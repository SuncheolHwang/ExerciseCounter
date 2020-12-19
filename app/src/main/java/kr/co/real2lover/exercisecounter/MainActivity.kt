package kr.co.real2lover.exercisecounter

import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import kr.co.real2lover.exercisecounter.databinding.ActivityMainBinding
import kotlin.coroutines.CoroutineContext

///
class MainActivity : AppCompatActivity(), CoroutineScope {
    val TAG = "MainActivity"

    private lateinit var binding: ActivityMainBinding

    /**
     * Stop Watch 시간 저장 변수
     * Stop Watch 상태 변수
     */
    private var timeWhenStopped: Long = 0
    private var timerStatus = 0

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

    var breakTimeCheckCounter = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

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
                counter++
                textCounter.text = counter.toString()
                textBreakTime.base = SystemClock.elapsedRealtime()
                textBreakTime.stop()
                breakTimeCheckCounter = 0
                launch {
                    for (i in 1..10) {
                        Log.d(TAG, "$i do something")
                        delay(1000)
                        breakTimeCheckCounter++

                        if (breakTimeCheckCounter >= 5) {
                            layoutBreakTime.visibility = View.VISIBLE
                            textBreakTime.start()
                        }
                    }
                }
            }

            buttonMinus.setOnClickListener {
                counter--
                textCounter.text = counter.toString()
            }

            textBreakTime.setOnChronometerTickListener {
                val time: Long = SystemClock.elapsedRealtime() - it.base
                val h = (time / 3600000).toInt()
                val m = (time - h * 3600000).toInt() / 60000
                val s = (time - h * 3600000 - m * 60000).toInt() / 1000
                val t = (if (m < 10) "0$m" else m).toString() + ":" + if (s < 10) "0$s" else s
                textBreakTime.text = t
            }
            textBreakTime.base = SystemClock.elapsedRealtime()
            textBreakTime.text = "00:00"
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
                STOP_WATCH_PAUSE-> {
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuCalendar -> {

            }
            R.id.menuReset -> {

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
}