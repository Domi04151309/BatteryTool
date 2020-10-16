package io.github.domi04151309.batterytool.activities

import android.content.*
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import io.github.domi04151309.batterytool.R
import io.github.domi04151309.batterytool.custom.EditIntegerPreference
import io.github.domi04151309.batterytool.helpers.P
import io.github.domi04151309.batterytool.helpers.Theme
import java.lang.NullPointerException

class SettingsActivity : AppCompatActivity(),
    PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        Theme.set(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, PreferenceFragment())
            .commit()
    }

    override fun onPreferenceStartFragment(
        caller: PreferenceFragmentCompat,
        pref: Preference
    ): Boolean {
        val fragment = supportFragmentManager.fragmentFactory.instantiate(
            classLoader,
            pref.fragment
        )
        fragment.arguments = pref.extras
        fragment.setTargetFragment(caller, 0)
        supportFragmentManager.beginTransaction()
            .replace(R.id.settings, fragment)
            .addToBackStack(null)
            .commit()
        return true
    }

    class PreferenceFragment : PreferenceFragmentCompat() {

        private val prefsChangedListener =
            SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
                if (key == P.PREF_THEME) requireActivity().recreate()
                if (key == P.PREF_AUTO_STOP_DELAY) updateAutoStopDelaySummary()
            }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(
                prefsChangedListener
            )
        }

        override fun onDestroy() {
            super.onDestroy()
            preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(
                prefsChangedListener
            )
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.pref_general)
            updateAutoStopDelaySummary()
            findPreference<Preference>("about")?.setOnPreferenceClickListener {
                startActivity(Intent(context, AboutActivity::class.java))
                true
            }
        }

        private fun updateAutoStopDelaySummary() {
            findPreference<EditIntegerPreference>(P.PREF_AUTO_STOP_DELAY)?.summary =
                requireContext().resources.getQuantityString(
                    R.plurals.pref_auto_stop_delay_summary,
                    preferenceManager.sharedPreferences.getInt(
                        P.PREF_AUTO_STOP_DELAY,
                        P.PREF_AUTO_STOP_DELAY_DEFAULT
                    ),
                    preferenceManager.sharedPreferences.getInt(
                        P.PREF_AUTO_STOP_DELAY,
                        P.PREF_AUTO_STOP_DELAY_DEFAULT
                    )
                )
        }
    }
}
