package io.github.domi04151309.batterytool.activities

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.domi04151309.batterytool.R
import io.github.domi04151309.batterytool.custom.EditIntegerPreference
import io.github.domi04151309.batterytool.helpers.Global
import io.github.domi04151309.batterytool.helpers.P
import io.github.domi04151309.batterytool.services.NotificationService

class SettingsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, PreferenceFragment())
            .commit()
    }

    class PreferenceFragment : PreferenceFragmentCompat() {
        private lateinit var notificationSettings: ActivityResultLauncher<Intent>
        private val preferenceChangeListener =
            SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                if (key == P.AUTO_STOP_DELAY) updateAutoStopDelaySummary()
                if (key == P.AGGRESSIVE_DOZE_DELAY) updateDozeDelaySummary()
                if (key == P.ALLOW_MUSIC) updateAllowMusicApps()
            }

        private fun checkNotificationsPermission() {
            val needsPermission =
                preferenceManager.sharedPreferences?.getBoolean(
                    P.ALLOW_MUSIC,
                    P.ALLOW_MUSIC_DEFAULT,
                ) ?: P.ALLOW_MUSIC_DEFAULT
            val hasPermission = NotificationService.getInstance() != null
            if (needsPermission && !hasPermission) {
                preferenceManager.sharedPreferences?.edit()
                    ?.putBoolean(P.ALLOW_MUSIC, P.ALLOW_MUSIC_DEFAULT)
                    ?.apply()
                preferenceScreen.findPreference<SwitchPreference>(
                    P.ALLOW_MUSIC,
                )?.isChecked = P.ALLOW_MUSIC_DEFAULT
            }
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            checkNotificationsPermission()
            notificationSettings =
                registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->
                    checkNotificationsPermission()
                }
            preferenceManager.sharedPreferences?.registerOnSharedPreferenceChangeListener(
                preferenceChangeListener,
            )
        }

        override fun onDestroy() {
            super.onDestroy()
            preferenceManager.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(
                preferenceChangeListener,
            )
        }

        override fun onCreatePreferences(
            savedInstanceState: Bundle?,
            rootKey: String?,
        ) {
            addPreferencesFromResource(R.xml.pref_general)
            updateAutoStopDelaySummary()
            updateDozeDelaySummary()
            findPreference<Preference>("about")?.setOnPreferenceClickListener {
                startActivity(Intent(context, AboutActivity::class.java))
                true
            }
        }

        private fun updateAutoStopDelaySummary() {
            findPreference<EditIntegerPreference>(P.AUTO_STOP_DELAY)?.summary =
                requireContext().resources.getQuantityString(
                    R.plurals.pref_auto_stop_delay_summary,
                    preferenceManager.sharedPreferences?.getInt(
                        P.AUTO_STOP_DELAY,
                        P.AUTO_STOP_DELAY_DEFAULT,
                    ) ?: P.AUTO_STOP_DELAY_DEFAULT,
                    preferenceManager.sharedPreferences?.getInt(
                        P.AUTO_STOP_DELAY,
                        P.AUTO_STOP_DELAY_DEFAULT,
                    ),
                )
        }

        private fun updateDozeDelaySummary() {
            findPreference<EditIntegerPreference>(P.AGGRESSIVE_DOZE_DELAY)?.summary =
                requireContext().resources.getQuantityString(
                    R.plurals.pref_aggressive_doze_delay_summary,
                    preferenceManager.sharedPreferences?.getInt(
                        P.AGGRESSIVE_DOZE_DELAY,
                        P.AGGRESSIVE_DOZE_DELAY_DEFAULT,
                    ) ?: P.AGGRESSIVE_DOZE_DELAY_DEFAULT,
                    preferenceManager.sharedPreferences?.getInt(
                        P.AGGRESSIVE_DOZE_DELAY,
                        P.AGGRESSIVE_DOZE_DELAY_DEFAULT,
                    ),
                )
        }

        private fun updateAllowMusicApps() {
            if (preferenceManager.sharedPreferences?.getBoolean(
                    P.ALLOW_MUSIC,
                    P.ALLOW_MUSIC_DEFAULT,
                ) ?: P.ALLOW_MUSIC_DEFAULT
            ) {
                val hasPermission = NotificationService.getInstance() != null
                if (!hasPermission) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.notifications_permission)
                        .setMessage(R.string.notifications_permission_explanation)
                        .setPositiveButton(android.R.string.ok) { _, _ ->
                            try {
                                notificationSettings.launch(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                            } catch (exception: ActivityNotFoundException) {
                                Log.w(Global.LOG_TAG, exception)
                            }
                        }
                        .show()
                }
            }
        }
    }
}
