package kr.co.real2lover.exercisecounter

import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.TimePicker
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

class MySettingPreference : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.setting_preference, null)

        val alarmTimePref = findPreference<Preference>("alarm_time")
        alarmTimePref?.setOnPreferenceClickListener {
            Log.d("MySettingPreference", "setOnPreferenceClickListener() 호출")
            timePickerOpen()
            true
        }
    }

    fun timePickerOpen() {
        val masPicker = context?.let { MinutesAndSecondsPicker(it) }

        masPicker?.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
        masPicker?.show()
    }
}