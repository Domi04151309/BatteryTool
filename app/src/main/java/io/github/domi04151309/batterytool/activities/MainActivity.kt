package io.github.domi04151309.batterytool.activities

import android.content.*
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
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
import io.github.domi04151309.batterytool.services.ForegroundService
import org.json.JSONArray
import java.lang.NullPointerException

class MainActivity : AppCompatActivity(),
    PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        Theme.setNoActionBar(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.content, PreferenceFragment())
            .commit()

        ContextCompat.startForegroundService(this, Intent(this, ForegroundService::class.java))

        findViewById<ImageView>(R.id.settings_icon).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        findViewById<FloatingActionButton>(R.id.add).setOnClickListener {
            startActivity(Intent(this, AddingActivity::class.java))
        }

        findViewById<FloatingActionButton>(R.id.hibernate).setOnClickListener {
            Toast.makeText(this, R.string.dummy_text, Toast.LENGTH_SHORT).show()
        }
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

        private lateinit var c: Context
        private lateinit var prefs: SharedPreferences
        private lateinit var categorySoon: PreferenceCategory

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.pref_main)

            c = requireContext()
            prefs = PreferenceManager.getDefaultSharedPreferences(c)
            categorySoon = findPreference("soon") ?: throw NullPointerException()

        }

        override fun onStart() {
            super.onStart()
            loadLists()
        }

        private fun loadLists() {
            categorySoon.removeAll()

            val appArray = JSONArray(prefs.getString(P.PREF_APP_LIST, P.PREF_APP_LIST_DEFAULT))
            val preferenceArray: ArrayList<Preference> = ArrayList(appArray.length())

            for (i in 0 until appArray.length()) {
                preferenceArray.add(AppHelper.generatePreference(c, appArray.getString(i)))
            }
            for (preference in preferenceArray.sortedWith(compareBy { it.title.toString() })) {
                categorySoon.addPreference(preference)
            }

        }
    }
}
