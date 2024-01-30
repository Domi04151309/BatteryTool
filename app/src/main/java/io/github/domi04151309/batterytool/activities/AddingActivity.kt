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

        @Suppress("CognitiveComplexMethod")
        private fun loadApps() =
            Thread {
                val packageManager: PackageManager = requireContext().packageManager
                val packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
                val userApps: ArrayList<Preference> = ArrayList(packages.size)
                val systemApps: ArrayList<Preference> = ArrayList(packages.size)
                val internalApps: ArrayList<Preference> = ArrayList(packages.size)
                for (packageInfo in packages) {
                    @Suppress("ComplexCondition")
                    if (
                        packageInfo.flags and ApplicationInfo.FLAG_INSTALLED != 0 &&
                        packageInfo.flags and ApplicationInfo.FLAG_HAS_CODE != 0 &&
                        packageInfo.packageName != requireContext().packageName &&
                        PreferenceManager.getDefaultSharedPreferences(requireContext())
                            .getString(P.PREF_APP_LIST, P.PREF_APP_LIST_DEFAULT)
                            ?.contains(packageInfo.packageName) != true
                    ) {
                        val preference =
                            AppHelper.generatePreference(requireContext(), packageInfo).apply {
                                setOnPreferenceClickListener {
                                    onPreferenceClicked(it, packageManager)
                                }
                            }
                        if (preference.title == preference.summary) continue
                        if (packageInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0) {
                            if (packageManager.getLaunchIntentForPackage(packageInfo.packageName) != null) {
                                systemApps.add(preference)
                            } else {
                                internalApps.add(preference)
                            }
                        } else {
                            userApps.add(preference)
                        }
                    }
                }

                Looper.prepare()
                displayApps(userApps, systemApps, internalApps)
            }.start()

        private fun displayApps(
            userApps: List<Preference>,
            systemApps: List<Preference>,
            internalApps: List<Preference>,
        ) {
            val categoryUser =
                findPreference<PreferenceCategory>("user")
                    ?: error(INVALID_LAYOUT)
            val categorySystem =
                findPreference<PreferenceCategory>("system")
                    ?: error(INVALID_LAYOUT)
            val categoryInternal =
                findPreference<PreferenceCategory>("internal")
                    ?: error(INVALID_LAYOUT)
            for (preference in userApps.sortedBy { it.title.toString() }) {
                categoryUser.addPreference(preference)
            }
            for (preference in systemApps.sortedBy { it.title.toString() }) {
                categorySystem.addPreference(preference)
            }
            for (preference in internalApps.sortedBy { it.title.toString() }) {
                categoryInternal.addPreference(preference)
            }
            preferenceScreen.addPreference(
                Preference(requireContext()).apply {
                    layoutResource = R.layout.preference_divider
                    isSelectable = false
                },
            )
        }

        private fun onPreferenceClicked(
            preference: Preference,
            packageManager: PackageManager,
        ): Boolean {
            if (appsToAdd.contains(preference.summary)) {
                preference.icon = packageManager.getApplicationIcon(preference.summary.toString())
                appsToAdd.remove(preference.summary)
                appsToAddNames.remove(preference.title)
            } else {
                preference.icon =
                    LayerDrawable(
                        arrayOf(
                            packageManager.getApplicationIcon(
                                preference.summary.toString(),
                            ),
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.overlay_icon,
                            ),
                        ),
                    )
                if (preference.summary != null) {
                    appsToAdd.add(
                        preference.summary ?: error(IMPOSSIBLE_STATE),
                    )
                    appsToAddNames.add(
                        preference.title ?: error(IMPOSSIBLE_STATE),
                    )
                }
            }
            bottomBar.text = appsToAddNames.sortedBy { it.toString() }.joinToString()
            return true
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

    companion object {
        private const val INVALID_LAYOUT = "Invalid layout."
        private const val IMPOSSIBLE_STATE = "Impossible state."
    }
}
