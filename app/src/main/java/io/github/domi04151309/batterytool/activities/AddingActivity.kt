package io.github.domi04151309.batterytool.activities

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import io.github.domi04151309.batterytool.R
import io.github.domi04151309.batterytool.helpers.Theme

class AddingActivity : AppCompatActivity(),
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

    override fun onPreferenceStartFragment(caller: PreferenceFragmentCompat, pref: Preference): Boolean {
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

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.pref_adding)

            Thread {
                val pm: PackageManager = requireContext().packageManager
                val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                val arrayList: ArrayList<Preference> = ArrayList(packages.size)
                for (packageInfo in packages) {
                    if (pm.getLaunchIntentForPackage(packageInfo.packageName) != null
                        && packageInfo.packageName != requireContext().packageName) {
                        val preference = Preference(requireContext())
                        preference.icon = packageInfo.loadIcon(pm)
                        preference.title = packageInfo.loadLabel(pm)
                        preference.summary = packageInfo.packageName
                        arrayList.add(preference)
                    }
                }
                for (preference in arrayList.sortedWith(compareBy { it.title.toString() })) {
                    preferenceScreen.addPreference(preference)
                }
            }.start()
        }
    }
}
