package io.github.domi04151309.batterytool.activities

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.os.Looper
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.github.domi04151309.batterytool.R
import io.github.domi04151309.batterytool.helpers.AppHelper
import io.github.domi04151309.batterytool.helpers.P
import org.json.JSONArray

class AddingActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adding)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.content, PreferenceFragment())
            .commit()
    }

    class PreferenceFragment : PreferenceFragmentCompat() {
        private val appsToAdd: ArrayList<CharSequence> = arrayListOf()
        private val appsToAddNames: ArrayList<CharSequence> = arrayListOf()
        private lateinit var bottomBar: TextView

        override fun onCreatePreferences(
            savedInstanceState: Bundle?,
            rootKey: String?,
        ) {
            addPreferencesFromResource(R.xml.pref_adding)
            bottomBar = requireActivity().findViewById(R.id.bottom_title)

            loadApps()

            requireActivity().findViewById<FloatingActionButton>(R.id.add).setOnClickListener {
                onAddClicked()
            }
        }

        private fun loadApps() =
            Thread {
                val packageManager: PackageManager = requireContext().packageManager
                val packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
                val userApps: ArrayList<Preference> = ArrayList(packages.size)
                val systemApps: ArrayList<Preference> = ArrayList(packages.size)
                for (packageInfo in packages) {
                    if (packageManager.getLaunchIntentForPackage(packageInfo.packageName) != null &&
                        PreferenceManager.getDefaultSharedPreferences(requireContext())
                            .getString(P.PREF_APP_LIST, P.PREF_APP_LIST_DEFAULT)
                            ?.contains(packageInfo.packageName) != true &&
                        packageInfo.packageName != requireContext().packageName
                    ) {
                        val preference = AppHelper.generatePreference(requireContext(), packageInfo)
                        preference.setOnPreferenceClickListener {
                            if (appsToAdd.contains(it.summary)) {
                                it.icon = packageManager.getApplicationIcon(it.summary.toString())
                                appsToAdd.remove(it.summary)
                                appsToAddNames.remove(it.title)
                            } else {
                                it.icon =
                                    LayerDrawable(
                                        arrayOf(
                                            packageManager.getApplicationIcon(
                                                it.summary.toString(),
                                            ),
                                            ContextCompat.getDrawable(
                                                requireContext(),
                                                R.drawable.overlay_icon,
                                            ),
                                        ),
                                    )
                                if (it.summary != null) {
                                    appsToAdd.add(
                                        it.summary ?: error("Impossible state."),
                                    )
                                }
                                if (it.summary != null) {
                                    appsToAddNames.add(
                                        it.title ?: error("Impossible state."),
                                    )
                                }
                            }
                            bottomBar.text =
                                appsToAddNames.sortedWith(compareBy { chars -> chars.toString() })
                                    .joinToString()
                            true
                        }
                        if (packageInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0) {
                            systemApps.add(preference)
                        } else {
                            userApps.add(preference)
                        }
                    }
                }

                Looper.prepare()
                displayApps(userApps, systemApps)
            }.start()

        private fun displayApps(
            userApps: List<Preference>,
            systemApps: List<Preference>,
        ) {
            val categoryUser =
                findPreference<PreferenceCategory>("user")
                    ?: error("Invalid layout.")
            for (preference in userApps.sortedWith(compareBy { it.title.toString() })) {
                categoryUser.addPreference(preference)
            }
            val categorySystem =
                findPreference<PreferenceCategory>("system")
                    ?: error("Invalid layout.")
            for (preference in systemApps.sortedWith(compareBy { it.title.toString() })) {
                categorySystem.addPreference(preference)
            }
            preferenceScreen.addPreference(
                Preference(requireContext()).let {
                    it.layoutResource = R.layout.preference_divider
                    it.isSelectable = false
                    it
                },
            )
        }

        private fun onAddClicked() {
            val currentList =
                JSONArray(
                    PreferenceManager.getDefaultSharedPreferences(requireContext())
                        .getString(P.PREF_APP_LIST, P.PREF_APP_LIST_DEFAULT),
                )
            for (item in appsToAdd) currentList.put(item)
            PreferenceManager.getDefaultSharedPreferences(requireContext())
                .edit()
                .putString(P.PREF_APP_LIST, currentList.toString())
                .apply()
            requireActivity().finish()
        }
    }
}
