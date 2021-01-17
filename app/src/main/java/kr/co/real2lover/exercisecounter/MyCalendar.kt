package kr.co.real2lover.exercisecounter

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.room.Room
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import kr.co.real2lover.exercisecounter.data.RoomHelper
import kr.co.real2lover.exercisecounter.data.RoomRecord
import kr.co.real2lover.exercisecounter.databinding.MyCalendarBinding
import java.util.*
import kotlin.collections.HashMap

class MyCalendar : AppCompatActivity() {
    private lateinit var binding: MyCalendarBinding
    private lateinit var calendarView: MaterialCalendarView

    val mCalendar: Calendar = Calendar.getInstance()

    /**
     * for RoomDatabase
     */
    var helper: RoomHelper? = null

    /**
     * 운동 저장 시간, 날짜
     */
    var mToday = "00:00:00"
    var exerciseTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = MyCalendarBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //ActionBar Setting
        supportActionBar?.apply {
            title = getString(R.string.my_calendar)
            setDisplayHomeAsUpEnabled(true)
        }

        helper = Room.databaseBuilder(this, RoomHelper::class.java, "room_record")
                .allowMainThreadQueries()
                .build()

        val list = helper?.roomRecordDao()?.getAll()

        var map = list?.let { listToMap(it) }

        calendarView = findViewById(R.id.calendar_view)
        calendarView.addDecorators(
                SaturdayDecorator(),
                SundayDecorator(),
                MinMaxDecorator(),
                TodayDecorator(this),
                EventDecorator(map)
        )

        calendarView.selectedDate = CalendarDay.today()

        calendarView.setOnMonthChangedListener { widget, date ->
            date.copyTo(mCalendar)
            widget.addDecorator(MinMaxDecorator())
        }

        mToday = intent.getStringExtra("Today").toString()
        exerciseTime = intent.getLongExtra("ExerciseTime", 0)
        binding.textDay.text = getString(R.string.exercise_time) + " -> " + convertLongToTime(exerciseTime)

        setResult(RESULT_OK, Intent())
    }

    override fun onPause() {
        super.onPause()
        Log.d(MainActivity.TAG, "Calendar onPause() 호출")

    }
    override fun onStop() {
        super.onStop()
        Log.d(MainActivity.TAG, "Calendar onStop() 호출")

        helper?.roomRecordDao()?.insertAll(RoomRecord(mToday, exerciseTime, MainActivity.counter))
    }

    fun convertLongToTime(time: Long): String {
        val h = (time / 3600000).toInt()
        val m = (time - h * 3600000).toInt() / 60000
        val s = (time - h * 3600000 - m * 60000).toInt() / 1000
        return (if (h < 10) "0$h" else h).toString() + ":" + (if (m < 10) "0$m" else m) + ":" + if (s < 10) "0$s" else s
    }

    fun listToMap(list: List<RoomRecord>): Map<CalendarDay, String> {
        val map = mutableMapOf<CalendarDay, String>()
        for (record in list) {
            val locColon: Int? = record.date?.indexOf(':', 0)
            val lastLocColon : Int? = record.date?.indexOf(':', locColon!!+1)

            val year = record.date?.substring(0..locColon!!.minus(1))?.toInt()
//            Log.d(MainActivity.TAG, "record: ${record.date.toString()}")
            val month = record.date?.substring(locColon!!.plus(1)..lastLocColon!!.minus(1))?.toInt()
            val day = record.date?.substring(lastLocColon!!.plus(1))?.toInt()

            val calendarDay = CalendarDay.from(year ?: 0, month ?: 0, day ?: 0)
            map[calendarDay] = convertLongToTime(record.time)
        }

        return map
    }

    inner class SaturdayDecorator : DayViewDecorator {
        private val calendar = Calendar.getInstance()

        override fun shouldDecorate(day: CalendarDay?): Boolean {
            day?.copyTo(calendar)
            val weekDay = calendar.get(Calendar.DAY_OF_WEEK)
            return weekDay == Calendar.SATURDAY
        }

        override fun decorate(view: DayViewFacade?) {
            view?.addSpan(object:ForegroundColorSpan(Color.BLUE){})
        }
    }

    inner class SundayDecorator : DayViewDecorator {
        private val calendar = Calendar.getInstance()

        override fun shouldDecorate(day: CalendarDay?): Boolean {
            day?.copyTo(calendar)
            val weekDay = calendar.get(Calendar.DAY_OF_WEEK)
            return weekDay == Calendar.SUNDAY
        }

        override fun decorate(view: DayViewFacade?) {
            view?.addSpan(object:ForegroundColorSpan(Color.RED){})
        }
    }

    inner class MinMaxDecorator : DayViewDecorator {
        private val calendar = Calendar.getInstance()

        override fun shouldDecorate(day: CalendarDay?): Boolean {
            day?.copyTo(calendar)

            val thisMonth = mCalendar.get(Calendar.MONTH)
            return day?.month != thisMonth
        }

        override fun decorate(view: DayViewFacade?) {
            view?.addSpan(object:ForegroundColorSpan(Color.parseColor("#d2d2d2")){})
            view?.setDaysDisabled(true)
        }
    }

    inner class TodayDecorator(context: Activity) : DayViewDecorator {
        private val drawable: Drawable = context.resources.getDrawable(R.drawable.my_selector, null)
        private val today = CalendarDay.today()

        override fun shouldDecorate(day: CalendarDay?): Boolean {
            return day == today
        }

        override fun decorate(view: DayViewFacade?) {
            view?.setBackgroundDrawable(drawable)
        }
    }

    inner class EventDecorator(dates: Map<CalendarDay, String>?) : DayViewDecorator {
        private val dates: HashMap<CalendarDay, String>? = HashMap(dates)

        override fun shouldDecorate(day: CalendarDay?): Boolean {
            if (dates != null) {
                return dates.containsKey(day)
            }
            return false
        }

        override fun decorate(view: DayViewFacade?) {
            view?.addSpan(TextSpan(dates, mCalendar))
        }
    }
}