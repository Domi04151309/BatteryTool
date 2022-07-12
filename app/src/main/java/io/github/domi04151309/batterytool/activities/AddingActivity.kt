package io.github.domi04151309.batterytool.activities

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.github.domi04151309.batterytool.R
import io.github.domi04151309.batterytool.helpers.AppHelper
import io.github.domi04151309.batterytool.helpers.P
import io.github.domi04151309.batterytool.helpers.Theme
import org.json.JSONArray

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
            pref.fragment ?: throw IllegalStateException()
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
            val c = requireContext()
            val prefs = PreferenceManager.getDefaultSharedPreferences(c)
            val pm: PackageManager = c.packageManager
            val addingArray: ArrayList<CharSequence> = arrayListOf()
            val addingArrayDisplay: ArrayList<CharSequence> = arrayListOf()
            val bottomBar = requireActivity().findViewById<TextView>(R.id.bottom_title)

            Thread {
                val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                val arrayList: ArrayList<Preference> = ArrayList(packages.size)
                val arrayListSystem: ArrayList<Preference> = ArrayList(packages.size)
                for (packageInfo in packages) {
                    if (pm.getLaunchIntentForPackage(packageInfo.packageName) != null
                        && prefs.getString(P.PREF_APP_LIST, P.PREF_APP_LIST_DEFAULT)
                            ?.contains(packageInfo.packageName) != true
                        && packageInfo.packageName != c.packageName
                    ) {
                        val preference = AppHelper.generatePreference(c, packageInfo)
                        preference.setOnPreferenceClickListener {
                            if (addingArray.contains(it.summary)) {
                                it.icon = pm.getApplicationIcon(it.summary.toString())
                                addingArray.remove(it.summary)
                                addingArrayDisplay.remove(it.title)
                            } else {
                                it.icon = LayerDrawable(
                                    arrayOf(
                                        pm.getApplicationIcon(
                                            it.summary.toString()
                                        ),
                                        ContextCompat.getDrawable(
                                            c,
                                            R.drawable.overlay_icon
                                        )
                                    )
                                )
                                if (it.summary != null) addingArray.add(
                                    it.summary ?: throw IllegalStateException()
                                )
                                if (it.summary != null) addingArrayDisplay.add(
                                    it.title ?: throw IllegalStateException()
                                )
                            }
                            bottomBar.text =
                                addingArrayDisplay.sortedWith(compareBy { chars -> chars.toString() })
                                    .joinToString()
                            true
                        }
                        if (packageInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0) {
                            arrayListSystem.add(preference)
                        } else {
                            arrayList.add(preference)
                        }
                    }
                }

                Looper.prepare()
                addPreferencesFromResource(R.xml.pref_adding)
                val categoryUser = findPreference<PreferenceCategory>("user")
                    ?: throw  NullPointerException()
                for (preference in arrayList.sortedWith(compareBy { it.title.toString() })) {
                    categoryUser.addPreference(preference)
                }
                val categorySystem = findPreference<PreferenceCategory>("system")
                    ?: throw  NullPointerException()
                for (preference in arrayListSystem.sortedWith(compareBy { it.title.toString() })) {
                    categorySystem.addPreference(preference)
                }
                preferenceScreen.addPreference(Preference(c).let {
                    it.layoutResource = R.layout.preference_divider
                    it.isSelectable = false
                    it
                })
            }.start()

            requireActivity().findViewById<FloatingActionButton>(R.id.add).setOnClickListener {
                val currentList = JSONArray(
                    prefs.getString(P.PREF_APP_LIST, P.PREF_APP_LIST_DEFAULT)
                )
                for (item in addingArray) currentList.put(item)
                prefs.edit().putString(P.PREF_APP_LIST, currentList.toString()).apply()
                requireActivity().finish()
            }
        }
    }
}
