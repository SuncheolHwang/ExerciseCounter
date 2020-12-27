package kr.co.real2lover.exercisecounter

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.NumberPicker
import kr.co.real2lover.exercisecounter.databinding.CounterEditDialogBinding
import kr.co.real2lover.exercisecounter.databinding.MinutesSecondsPickerBinding

class MinutesAndSecondsPicker(context: Context) : Dialog(context) {
    private lateinit var binding: MinutesSecondsPickerBinding
    private lateinit var pickerDialogClickListener: PickerDialogClickListener

    var minute = 0
    var second = 0

    constructor(context: Context, pickerDialogClickListener: PickerDialogClickListener) : this(context) {
        this.pickerDialogClickListener = pickerDialogClickListener
    }

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
            maxValue = 59
            wrapSelectorWheel = true
            descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
        }

        binding.buttonInput.setOnClickListener {
            minute = binding.minutePicker.value
            second = binding.secondPicker.value
            pickerDialogClickListener.onPositiveClick(minute, second)
            dismiss()
        }

        binding.buttonCancel.setOnClickListener {
            pickerDialogClickListener.onNegativeClick()
            dismiss()
        }
    }

    interface PickerDialogClickListener {
        fun onPositiveClick(minute: Int, second: Int)
        fun onNegativeClick()
    }
}