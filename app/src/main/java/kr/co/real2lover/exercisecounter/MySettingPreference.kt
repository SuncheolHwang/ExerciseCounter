package kr.co.real2lover.exercisecounter

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat

class MySettingPreference : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.setting_preference, null)
    }
}