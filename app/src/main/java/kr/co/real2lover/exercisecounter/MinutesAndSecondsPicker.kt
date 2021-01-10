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

    constructor(context: Context, strSetTime: String, pickerDialogClickListener: PickerDialogClickListener) : this(context) {
        this.pickerDialogClickListener = pickerDialogClickListener
        val indexColon = strSetTime!!.indexOf(':')
        minute = strSetTime!!.substring(0, indexColon).toInt()
        second = strSetTime!!.substring(indexColon + 1).toInt()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =  MinutesSecondsPickerBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.minutePicker.apply {
            minValue = 0
            maxValue = 10
            wrapSelectorWheel = true
            descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
            setFormatter { value ->
                if (value.toString().length == 1) "0$value" else value.toString()
            }
            value = minute
        }

        binding.secondPicker.apply {
            minValue = 0
            maxValue = 59
            wrapSelectorWheel = true
            descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
            setFormatter { value ->
                if (value.toString().length == 1) "0$value" else value.toString()
            }
            value = second
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