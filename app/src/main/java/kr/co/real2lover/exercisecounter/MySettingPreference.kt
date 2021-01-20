package kr.co.real2lover.exercisecounter

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

class MySettingPreference : PreferenceFragmentCompat() {
    private lateinit var alarmTimePref: Preference
    var strSetTime = ""

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.setting_preference, null)

        alarmTimePref = findPreference("alarm_time")!!

        strSetTime = alarmTimePref.sharedPreferences.getString(getString(R.string.setting_alarm_time), "00:00").toString()

        alarmTimePref.setOnPreferenceClickListener {
            timePickerOpen()
            true
        }

        val alarmTypePref = findPreference<Preference>("alarm_type")
        alarmTypePref?.setOnPreferenceChangeListener { preference, newValue ->
            preference.sharedPreferences.edit()
                    .putBoolean(getString(R.string.vibration_select_key), newValue as Boolean).commit()
        }
    }

    fun timePickerOpen() {
        val minutesAndSecondsPicker = context?.let {
            MinutesAndSecondsPicker(it, strSetTime, object : MinutesAndSecondsPicker.PickerDialogClickListener {
                    override fun onPositiveClick(minute: Int, second: Int) {
                        val strMinute = if (minute.toString().length != 2) "0$minute" else "$minute"
                        val strSecond = if (second.toString().length != 2) "0$second" else "$second"
                        alarmTimePref.sharedPreferences.
                        edit().putString(getString(R.string.setting_alarm_time), "${strMinute}:${strSecond}").apply()
                        Toast.makeText(it,
                                getString(R.string.alarm_set_time) + " $strMinute:$strSecond",
                                Toast.LENGTH_SHORT).show()
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