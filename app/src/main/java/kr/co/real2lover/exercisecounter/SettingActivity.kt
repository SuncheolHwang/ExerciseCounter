package kr.co.real2lover.exercisecounter

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.preference.PreferenceManager

class SettingActivity : AppCompatActivity() {
    lateinit var pref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        pref = PreferenceManager.getDefaultSharedPreferences(this)

        val alarmTime = pref.getString("alarm_time", "00:30")
        Toast.makeText(this, alarmTime, Toast.LENGTH_SHORT).show()
    }

    val listener: SharedPreferences.OnSharedPreferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == "alarm_time") {

            }
        }
}