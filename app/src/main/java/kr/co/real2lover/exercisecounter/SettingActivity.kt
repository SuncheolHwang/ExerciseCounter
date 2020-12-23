package kr.co.real2lover.exercisecounter

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.preference.PreferenceManager

class SettingActivity : AppCompatActivity() {
    lateinit var pref: SharedPreferences
    lateinit var sharedPref: SharedPreferences
    var alarmTime = "00:30"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        pref = PreferenceManager.getDefaultSharedPreferences(this)
        alarmTime = pref.getString(getString(R.string.setting_alarm_time), alarmTime).toString()

        sharedPref = application.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putString(getString(R.string.setting_alarm_time), alarmTime)
            commit()
        }

        Toast.makeText(this, alarmTime, Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        pref.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun onPause() {
        super.onPause()
        pref.registerOnSharedPreferenceChangeListener(listener)
    }

    val listener: SharedPreferences.OnSharedPreferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == getString(R.string.setting_alarm_time)) {
                alarmTime =
                    sharedPreferences.getString(getString(R.string.setting_alarm_time), alarmTime).toString()
                with(sharedPref.edit()) {
                    putString(getString(R.string.setting_alarm_time), alarmTime)
                    commit()
                }
            }
        }
}