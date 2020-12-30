package kr.co.real2lover.exercisecounter

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.style.LineBackgroundSpan
import com.prolificinteractive.materialcalendarview.CalendarDay
import java.util.*

class TextSpan(val dates: Map<CalendarDay, String>?, val calender: Calendar, val color: Int)
    : LineBackgroundSpan {

    constructor(dates: Map<CalendarDay, String>?, calender: Calendar)
            : this(dates, calender, Color.GRAY)

    override fun drawBackground(
        canvas: Canvas,
        paint: Paint,
        left: Int,
        right: Int,
        top: Int,
        baseline: Int,
        bottom: Int,
        text: CharSequence,
        start: Int,
        end: Int,
        lineNumber: Int
    ) {
        val newPaint = Paint()
        newPaint.color = color
        newPaint.textSize = (paint.textSize/1.4).toFloat()

        val filterYearMap = dates?.filter { it.key.year == calender.get(Calendar.YEAR) }
        val filterMonthMap = filterYearMap?.filter { it.key.month == calender.get(Calendar.MONTH) }
        val filterDayMap = filterMonthMap?.filter { it.key.day.toString() == text.toString() }

        if (filterDayMap != null) {
            for (map in filterDayMap) {
                canvas.drawText(map.value, ((left + right) / 2 - newPaint.textSize * 1.8).toFloat(), (bottom + newPaint.textSize), newPaint)
            }
        }
    }
}