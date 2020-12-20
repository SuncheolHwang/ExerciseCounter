package kr.co.real2lover.exercisecounter

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.getSystemService
import kr.co.real2lover.exercisecounter.databinding.CounterEditDialogBinding

class CounterEditDialog(context: Context) : Dialog(context) {
    private lateinit var binding: CounterEditDialogBinding
    private lateinit var customDialogClickListener: CustomDialogClickListener

    constructor(context: Context, customDialogClickListener: CustomDialogClickListener) : this(context) {
        this.customDialogClickListener = customDialogClickListener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =  CounterEditDialogBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.textCounter.requestFocus()

        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
	    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)

        binding.buttonCancel.setOnClickListener {
            customDialogClickListener.onNegativeClick()
            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
            dismiss()
        }

        binding.buttonInput.setOnClickListener {
            customDialogClickListener.onPositiveClick(binding.editValue.text.toString())
            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
            dismiss()
        }
    }
}