package io.github.domi04151309.batterytool.activities

import android.app.AlertDialog
import android.content.*
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import io.github.domi04151309.batterytool.R
import io.github.domi04151309.batterytool.custom.EditIntegerPreference
import io.github.domi04151309.batterytool.helpers.P
import io.github.domi04151309.batterytool.helpers.Theme
import io.github.domi04151309.batterytool.services.NotificationService


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
            pref.fragment ?: throw IllegalStateException()
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

        private lateinit var getNotifSettings: ActivityResultLauncher<Intent>
        private val prefsChangedListener =
            SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
                if (key == P.PREF_THEME) requireActivity().recreate()
                if (key == P.PREF_AUTO_STOP_DELAY) updateAutoStopDelaySummary()
                if (key == P.PREF_AGGRESSIVE_DOZE_DELAY) updateAggressiveDozeDelaySummary()
                if (key == P.PREF_ALLOW_MUSIC) updateAllowMusicApps()
            }


        private fun checkNotificationsPermission() {
            val needsPermission = preferenceManager.sharedPreferences?.getBoolean(
                P.PREF_ALLOW_MUSIC,
                P.PREF_ALLOW_MUSIC_DEFAULT
            ) ?: P.PREF_ALLOW_MUSIC_DEFAULT
            if (!needsPermission) return
            val hasPermission = NotificationService.getInstance() != null
            if (!hasPermission) {
                preferenceManager.sharedPreferences?.edit()
                    ?.putBoolean(P.PREF_ALLOW_MUSIC, P.PREF_ALLOW_MUSIC_DEFAULT)
                    ?.apply()
                preferenceScreen.findPreference<SwitchPreference>(
                    P.PREF_ALLOW_MUSIC
                )?.isChecked = P.PREF_ALLOW_MUSIC_DEFAULT
            }
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            checkNotificationsPermission()
            getNotifSettings =
                registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                    checkNotificationsPermission()
                }
            preferenceManager.sharedPreferences?.registerOnSharedPreferenceChangeListener(
                prefsChangedListener
            )
        }

        override fun onDestroy() {
            super.onDestroy()
            preferenceManager.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(
                prefsChangedListener
            )
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.pref_general)
            updateAutoStopDelaySummary()
            updateAggressiveDozeDelaySummary()
            findPreference<Preference>("about")?.setOnPreferenceClickListener {
                startActivity(Intent(context, AboutActivity::class.java))
                true
            }
        }

        private fun updateAutoStopDelaySummary() {
            findPreference<EditIntegerPreference>(P.PREF_AUTO_STOP_DELAY)?.summary =
                requireContext().resources.getQuantityString(
                    R.plurals.pref_auto_stop_delay_summary,
                    preferenceManager.sharedPreferences?.getInt(
                        P.PREF_AUTO_STOP_DELAY,
                        P.PREF_AUTO_STOP_DELAY_DEFAULT
                    ) ?: P.PREF_AUTO_STOP_DELAY_DEFAULT,
                    preferenceManager.sharedPreferences?.getInt(
                        P.PREF_AUTO_STOP_DELAY,
                        P.PREF_AUTO_STOP_DELAY_DEFAULT
                    )
                )
        }

        private fun updateAggressiveDozeDelaySummary() {
            findPreference<EditIntegerPreference>(P.PREF_AGGRESSIVE_DOZE_DELAY)?.summary =
                requireContext().resources.getQuantityString(
                    R.plurals.pref_aggressive_doze_delay_summary,
                    preferenceManager.sharedPreferences?.getInt(
                        P.PREF_AGGRESSIVE_DOZE_DELAY,
                        P.PREF_AGGRESSIVE_DOZE_DELAY_DEFAULT
                    ) ?: P.PREF_AGGRESSIVE_DOZE_DELAY_DEFAULT,
                    preferenceManager.sharedPreferences?.getInt(
                        P.PREF_AGGRESSIVE_DOZE_DELAY,
                        P.PREF_AGGRESSIVE_DOZE_DELAY_DEFAULT
                    )
                )
        }

        private fun updateAllowMusicApps() {
            val enabled = preferenceManager.sharedPreferences?.getBoolean(
                P.PREF_ALLOW_MUSIC,
                P.PREF_ALLOW_MUSIC_DEFAULT
            ) ?: P.PREF_ALLOW_MUSIC_DEFAULT
            if (enabled) {
                // we need to check if we have notifications permissions
                val hasPermission = NotificationService.getInstance() != null
                if (!hasPermission) {
                    AlertDialog.Builder(context)
                        .setTitle(R.string.notifications_permission)
                        .setMessage(R.string.notifications_permission_explanation)
                        .setPositiveButton(android.R.string.ok) { _, _ ->
                            try {
                                getNotifSettings.launch(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                            } catch (e: ActivityNotFoundException) {
                            }
                        }
                        .show()
                }

            }
        }
    }
}
