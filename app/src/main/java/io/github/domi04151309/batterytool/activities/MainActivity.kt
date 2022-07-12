package io.github.domi04151309.batterytool.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import io.github.domi04151309.batterytool.helpers.*
import io.github.domi04151309.batterytool.helpers.Root
import io.github.domi04151309.batterytool.helpers.Theme
import io.github.domi04151309.batterytool.services.ForegroundService
import org.json.JSONArray
import java.lang.Exception

class MainActivity : AppCompatActivity(),
    PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    private var themeId = ""
    private fun getThemeId(): String =
        PreferenceManager.getDefaultSharedPreferences(this)
            .getString(P.PREF_THEME, P.PREF_THEME_DEFAULT) ?: P.PREF_THEME_DEFAULT

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

        themeId = getThemeId()
    }

    override fun onStart() {
        super.onStart()

        if (getThemeId() != themeId) {
            themeId = getThemeId()
            recreate()
        }
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

        private lateinit var c: Context
        private lateinit var prefs: SharedPreferences
        private lateinit var categorySoon: PreferenceCategory
        private lateinit var categoryUnnecessary: PreferenceCategory
        private lateinit var categoryStopped: PreferenceCategory

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.pref_main)

            c = requireContext()
            prefs = PreferenceManager.getDefaultSharedPreferences(c)
            categorySoon = findPreference("soon") ?: throw NullPointerException()
            categoryUnnecessary = findPreference("unnecessary") ?: throw NullPointerException()
            categoryStopped = findPreference("stopped") ?: throw NullPointerException()

            activity?.findViewById<FloatingActionButton>(R.id.hibernate)?.setOnClickListener {
                AppHelper.hibernate(c)
                Toast.makeText(c, R.string.toast_stopped_all, Toast.LENGTH_SHORT).show()
                Handler(Looper.getMainLooper()).postDelayed({
                    loadLists()
                }, 1000)
            }
        }

        override fun onStart() {
            super.onStart()
            loadLists()
        }

        private fun generateEmptyListIndicator(): Preference {
            return Preference(c).let {
                it.icon = ContextCompat.getDrawable(c, R.mipmap.ic_launcher)
                it.title = c.getString(R.string.main_empty)
                it.summary = c.getString(R.string.main_empty_summary)
                it.isSelectable = false
                it
            }
        }

        private fun loadLists() {
            categorySoon.removeAll()
            categoryUnnecessary.removeAll()
            categoryStopped.removeAll()

            val appArray = JSONArray(prefs.getString(P.PREF_APP_LIST, P.PREF_APP_LIST_DEFAULT))
            val preferenceSoonArray: ArrayList<Preference> = ArrayList(appArray.length() / 2)
            val preferenceStoppedArray: ArrayList<Preference> = ArrayList(appArray.length() / 2)
            val services = Root.getServices()
            val forcedSet = ForcedSet(prefs)

            var preference: Preference
            for (i in 0 until appArray.length()) {
                preference = try {
                    AppHelper.generatePreference(c, appArray.getString(i), forcedSet)
                } catch (e: Exception) {
                    continue
                }
                preference.setOnPreferenceClickListener {
                    val options = resources
                        .getStringArray(R.array.main_click_dialog_options)
                        .toMutableList()
                    val isForced = forcedSet.contains(it.summary.toString())
                    options.add(
                        2,
                        resources.getString(
                            if (isForced) R.string.main_click_dialog_turn_off_always
                            else R.string.main_click_dialog_turn_on_always
                        )
                    )
                    AlertDialog.Builder(c)
                        .setTitle(R.string.main_click_dialog_title)
                        .setItems(options.toTypedArray()) { _, which ->
                            when (which) {
                                0 -> {
                                    Root.shell("am force-stop ${it.summary}")
                                    loadLists()
                                    Toast.makeText(c, R.string.toast_stopped, Toast.LENGTH_SHORT)
                                        .show()
                                }
                                1 -> {
                                    startActivity(Intent().apply {
                                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                        data = Uri.fromParts("package", it.summary.toString(), null)
                                    })
                                }
                                2 -> {
                                    if (isForced) forcedSet.remove(it.summary.toString())
                                    else forcedSet.add(it.summary.toString())
                                    forcedSet.save()
                                    Toast.makeText(
                                        context,
                                        if (isForced) R.string.main_click_dialog_turn_off_always
                                        else R.string.main_click_dialog_turn_on_always,
                                        Toast.LENGTH_LONG
                                    ).show()
                                    loadLists()
                                }
                                3 -> {
                                    for (j in 0 until appArray.length()) {
                                        if (appArray.getString(j) == it.summary) {
                                            appArray.remove(j)
                                            break
                                        }
                                    }
                                    prefs.edit().putString(P.PREF_APP_LIST, appArray.toString())
                                        .apply()
                                    loadLists()
                                }
                            }
                        }
                        .setNegativeButton(android.R.string.cancel) { _, _ -> }
                        .show()
                    true
                }
                if (c.packageManager.getApplicationInfo(
                        preference.summary.toString(),
                        PackageManager.GET_META_DATA
                    ).flags and ApplicationInfo.FLAG_STOPPED != 0
                ) preferenceStoppedArray.add(preference)
                else preferenceSoonArray.add(preference)
            }
            var isSoonEmpty = true
            var isUnnecessaryEmpty = true
            for (item in preferenceSoonArray.sortedWith(compareBy { it.title.toString() })) {
                if (
                    item.summary != null
                    && (services.contains(item.summary ?: throw IllegalStateException())
                            || forcedSet.contains(item.summary.toString()))
                ) {
                    categorySoon.addPreference(item)
                    isSoonEmpty = false
                } else {
                    categoryUnnecessary.addPreference(item)
                    isUnnecessaryEmpty = false
                }
            }

            if (isSoonEmpty) categorySoon.addPreference(generateEmptyListIndicator())
            if (isUnnecessaryEmpty) categoryUnnecessary.addPreference(generateEmptyListIndicator())

            if (preferenceStoppedArray.isEmpty()) {
                categoryStopped.addPreference(generateEmptyListIndicator())
            } else {
                for (item in preferenceStoppedArray.sortedWith(compareBy { it.title.toString() })) {
                    categoryStopped.addPreference(item)
                }
            }
        }
    }
}
