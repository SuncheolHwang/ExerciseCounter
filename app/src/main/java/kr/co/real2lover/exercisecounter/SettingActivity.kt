package kr.co.real2lover.exercisecounter

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.preference.PreferenceManager

class SettingActivity : AppCompatActivity() {
    lateinit var pref: SharedPreferences
    lateinit var sharedPref: SharedPreferences
    var alarmTime = "00:30"
    var isVibratorOn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        //ActionBar Setting
        supportActionBar?.apply {
            title = getString(R.string.my_setting)
            setDisplayHomeAsUpEnabled(true)
        }

        pref = PreferenceManager.getDefaultSharedPreferences(this)
        alarmTime = pref.getString(getString(R.string.setting_alarm_time), alarmTime).toString()

        isVibratorOn = pref.getBoolean(getString(R.string.vibration_select_key), false)

        sharedPref = application.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putString(getString(R.string.setting_alarm_time), alarmTime).commit()
            putBoolean(getString(R.string.vibration_select_key), isVibratorOn)
        }

        setResult(RESULT_OK, Intent())
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
/*                Toast.makeText(applicationContext,
                        getString(R.string.alarm_set_time) + "$alarmTime",
                        Toast.LENGTH_SHORT).show()*/
            } else if (key == getString(R.string.vibration_select_key)) {
                isVibratorOn =
                        sharedPreferences.getBoolean(getString(R.string.vibration_select_key), false)
                with(sharedPref.edit()) {
                    putBoolean(getString(R.string.vibration_select_key), isVibratorOn)
                    commit()
                }
            }
        }
}