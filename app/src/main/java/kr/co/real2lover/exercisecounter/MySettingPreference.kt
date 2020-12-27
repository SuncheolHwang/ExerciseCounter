package kr.co.real2lover.exercisecounter

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

class MySettingPreference : PreferenceFragmentCompat() {
    private lateinit var alarmTimePref: Preference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.setting_preference, null)

        alarmTimePref = findPreference("alarm_time")!!
        alarmTimePref.setOnPreferenceClickListener {
            Log.d("MySettingPreference", "setOnPreferenceClickListener() 호출")
            timePickerOpen()
            true
        }

        val alarmTypePref = findPreference<Preference>("alarm_type")
    }

    fun timePickerOpen() {
        val minutesAndSecondsPicker = context?.let {
            MinutesAndSecondsPicker(it, object : MinutesAndSecondsPicker.PickerDialogClickListener {
                    override fun onPositiveClick(minute: Int, second: Int) {
                        val strMinute = if (minute.toString().length != 2) "0$minute" else "$minute"
                        val strSecond = if (second.toString().length != 2) "0$second" else "$second"
                        alarmTimePref.sharedPreferences.
                        edit().putString(getString(R.string.setting_alarm_time), "${strMinute}:${strSecond}").commit()
                    }

                    override fun onNegativeClick() {

                    }
                })
        }

        minutesAndSecondsPicker?.window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT)
        minutesAndSecondsPicker?.show()
    }
}