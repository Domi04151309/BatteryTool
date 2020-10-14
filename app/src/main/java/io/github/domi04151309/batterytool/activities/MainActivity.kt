package io.github.domi04151309.batterytool.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
import org.json.JSONObject


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

            var preference: Preference
            for (i in 0 until appArray.length()) {
                preference = AppHelper.generatePreference(c, appArray.getString(i))
                preference.setOnPreferenceClickListener {
                    AlertDialog.Builder(c)
                        .setTitle(R.string.main_click_dialog_title)
                        .setItems(R.array.main_click_dialog_options) { _, which ->
                            when (which) {
                                0 -> Toast.makeText(c, R.string.dummy_text, Toast.LENGTH_SHORT)
                                    .show()
                                1 -> {
                                    startActivity(Intent().apply {
                                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                        data = Uri.fromParts("package", it.summary.toString(), null)
                                    })
                                }
                                2 -> {
                                    val jsonArray = JSONArray(
                                        prefs.getString(
                                            P.PREF_APP_LIST,
                                            P.PREF_APP_LIST_DEFAULT
                                        )
                                    )
                                    for (j in 0 until jsonArray.length()) {
                                        if (jsonArray.getString(j) == it.summary) {
                                            jsonArray.remove(j)
                                            break
                                        }
                                    }
                                    prefs.edit().putString(P.PREF_APP_LIST, jsonArray.toString()).apply()
                                    loadLists()
                                }
                            }
                        }
                        .setNegativeButton(android.R.string.cancel) { _, _ -> }
                        .show()
                    true
                }
                preferenceArray.add(preference)
            }
            for (item in preferenceArray.sortedWith(compareBy { it.title.toString() })) {
                categorySoon.addPreference(item)
            }

        }
    }
}
