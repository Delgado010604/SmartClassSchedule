package com.example.smartclass

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.activityViewModels
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat

class SettingsFragment : PreferenceFragmentCompat() {

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        val themePreference = findPreference<ListPreference>("theme")
        themePreference?.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            when (newValue) {
                "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
            true
        }

        val notificationPreference = findPreference<SwitchPreferenceCompat>("notifications")
        notificationPreference?.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            val enabled = newValue as Boolean
            if (!enabled) {
                viewModel.allSchedules.removeObservers(viewLifecycleOwner)
                viewModel.allTasks.removeObservers(viewLifecycleOwner)
                viewModel.allSchedules.observe(viewLifecycleOwner) { schedules ->
                    schedules.forEach { schedule ->
                        val weekdays = schedule.weekdays.split(",").mapNotNull { it.toIntOrNull() }.toSet()
                        if (weekdays.isNotEmpty()) {
                            AlarmHelper.cancelScheduleAlarm(requireContext(), schedule.id, weekdays)
                        }
                    }
                }
                viewModel.allTasks.observe(viewLifecycleOwner) { tasks ->
                    tasks.forEach { task ->
                        if (!task.isCompleted) {
                            AlarmHelper.cancelTaskAlarm(requireContext(), task)
                        }
                    }
                }
            } else {
                // TODO: Re-enable all alarms
            }
            true
        }
    }
}
