package kr.co.real2lover.exercisecounter

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import kr.co.real2lover.exercisecounter.databinding.MyCalendarBinding
import java.util.*

class MyCalendar : AppCompatActivity() {
    private lateinit var binding: MyCalendarBinding
    private lateinit var calendarView: MaterialCalendarView

    val mCalendar: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = MyCalendarBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        calendarView = findViewById(R.id.calendar_view)
        calendarView.addDecorators(
                SaturdayDecorator(),
                SundayDecorator(),
                MinMaxDecorator(),
                TodayDecorator(this)
        )

        calendarView.selectedDate = CalendarDay.today()
        calendarView.setOnMonthChangedListener { widget, date ->
            Log.d("MainActivity", "setOnMonthChangedListener 호출")
            date.copyTo(mCalendar)
            widget.addDecorator(MinMaxDecorator())
        }

        val exerciseTime = intent.getLongExtra("ExerciseTime", 0)
        binding.textDay.text = getString(R.string.exercise_time) + " -> " + convertLongToTime(exerciseTime)
    }

    fun convertLongToTime(time: Long): String {
        val h = (time / 3600000).toInt()
        val m = (time - h * 3600000).toInt() / 60000
        val s = (time - h * 3600000 - m * 60000).toInt() / 1000
        return (if (h < 10) "0$h" else h).toString() + ":" + (if (m < 10) "0$m" else m) + ":" + if (s < 10) "0$s" else s
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
}
