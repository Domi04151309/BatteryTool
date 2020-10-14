package io.github.domi04151309.batterytool.activities

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import io.github.domi04151309.batterytool.R
import io.github.domi04151309.batterytool.helpers.Theme

class AddingActivity : AppCompatActivity(),
    PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        Theme.set(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adding)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.content, PreferenceFragment())
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
            .replace(R.id.content, fragment)
            .addToBackStack(null)
            .commit()
        return true
    }

    class PreferenceFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.pref_adding)

            val pm: PackageManager = requireContext().packageManager
            val addingArray: ArrayList<CharSequence> = arrayListOf()
            val addingArrayDisplay: ArrayList<CharSequence> = arrayListOf()
            val bottomBar = requireActivity().findViewById<TextView>(R.id.bottom_title)

            Thread {
                val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                val arrayList: ArrayList<Preference> = ArrayList(packages.size)
                val arrayListSystem: ArrayList<Preference> = ArrayList(packages.size)
                for (packageInfo in packages) {
                    if (pm.getLaunchIntentForPackage(packageInfo.packageName) != null
                        && packageInfo.packageName != requireContext().packageName
                    ) {
                        val preference = Preference(requireContext())
                        preference.icon = packageInfo.loadIcon(pm)
                        preference.title = packageInfo.loadLabel(pm)
                        preference.summary = packageInfo.packageName
                        preference.setOnPreferenceClickListener {
                            if (addingArray.contains(it.summary)) {
                                it.icon = pm.getApplicationIcon(
                                    pm.getApplicationInfo(
                                        it.summary.toString(),
                                        PackageManager.GET_META_DATA
                                    )
                                )
                                addingArray.remove(it.summary)
                                addingArrayDisplay.remove(
                                    pm.getApplicationLabel(
                                        pm.getApplicationInfo(
                                            it.summary.toString(),
                                            PackageManager.GET_META_DATA
                                        )
                                    )
                                )
                            } else {
                                it.icon = LayerDrawable(
                                    arrayOf(
                                        pm.getApplicationIcon(
                                            pm.getApplicationInfo(
                                                it.summary.toString(),
                                                PackageManager.GET_META_DATA
                                            )
                                        ),
                                        ContextCompat.getDrawable(
                                            requireContext(),
                                            R.drawable.overlay_icon
                                        )
                                    )
                                )
                                addingArray.add(it.summary)
                                addingArrayDisplay.add(
                                    pm.getApplicationLabel(
                                        pm.getApplicationInfo(
                                            it.summary.toString(),
                                            0
                                        )
                                    )
                                )
                            }
                            bottomBar.text = addingArrayDisplay.joinToString()
                            true
                        }
                        if (packageInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0) {
                            arrayListSystem.add(preference)
                        } else {
                            arrayList.add(preference)
                        }
                    }
                }
                for (preference in arrayList.sortedWith(compareBy { it.title.toString() })) {
                    findPreference<PreferenceCategory>("user")?.addPreference(preference)
                }
                for (preference in arrayListSystem.sortedWith(compareBy { it.title.toString() })) {
                    findPreference<PreferenceCategory>("system")?.addPreference(preference)
                }
                val bottomDivider = Preference(requireContext())
                bottomDivider.layoutResource = R.layout.preference_divider
                bottomDivider.isSelectable = false
                preferenceScreen.addPreference(bottomDivider)
            }.start()
        }
    }
}
