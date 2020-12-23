package kr.co.real2lover.exercisecounter

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.NumberPicker
import kr.co.real2lover.exercisecounter.databinding.CounterEditDialogBinding
import kr.co.real2lover.exercisecounter.databinding.MinutesSecondsPickerBinding

class MinutesAndSecondsPicker(context: Context) : Dialog(context) {
    private lateinit var binding: MinutesSecondsPickerBinding

    val minute = 0
    val second = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =  MinutesSecondsPickerBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.minutePicker.apply {
            minValue = 0
            maxValue = 60
            wrapSelectorWheel = true
            descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
        }

        binding.secondPicker.apply {
            minValue = 0
            maxValue = 60
            wrapSelectorWheel = true
            descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
        }
    }
}