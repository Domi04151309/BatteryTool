package io.github.domi04151309.batterytool.activities

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.elevation.SurfaceColors
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.github.domi04151309.batterytool.R
import io.github.domi04151309.batterytool.helpers.AppHelper
import io.github.domi04151309.batterytool.helpers.ForcedSet
import io.github.domi04151309.batterytool.helpers.Global
import io.github.domi04151309.batterytool.helpers.P
import io.github.domi04151309.batterytool.helpers.Root
import io.github.domi04151309.batterytool.services.ForegroundService
import org.json.JSONArray

class MainActivity : BaseActivity() {
    private var themeId = ""

    private fun getThemeId(): String =
        PreferenceManager.getDefaultSharedPreferences(this)
            .getString(P.PREF_THEME, P.PREF_THEME_DEFAULT)
            ?: P.PREF_THEME_DEFAULT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        window.statusBarColor = SurfaceColors.SURFACE_0.getColor(this)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.content, PreferenceFragment())
            .commit()

        if (
            !SetupActivity.demoMode &&
            !PreferenceManager.getDefaultSharedPreferences(this).getBoolean("setup_complete", false)
        ) {
            startActivity(Intent(this, SetupActivity::class.java))
            finish()
            return
        }

        ContextCompat.startForegroundService(this, Intent(this, ForegroundService::class.java))

        findViewById<MaterialToolbar>(R.id.toolbar).setOnMenuItemClickListener {
            startActivity(
                Intent(
                    this@MainActivity,
                    SettingsActivity::class.java,
                ),
            )
            true
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

    class PreferenceFragment : PreferenceFragmentCompat() {
        private lateinit var categorySoon: PreferenceCategory
        private lateinit var categoryUnnecessary: PreferenceCategory
        private lateinit var categoryStopped: PreferenceCategory

        override fun onCreatePreferences(
            savedInstanceState: Bundle?,
            rootKey: String?,
        ) {
            addPreferencesFromResource(R.xml.pref_main)

            categorySoon = findPreference("soon") ?: error(INVALID_LAYOUT)
            categoryUnnecessary = findPreference("unnecessary") ?: error(INVALID_LAYOUT)
            categoryStopped = findPreference("stopped") ?: error(INVALID_LAYOUT)

            activity?.findViewById<FloatingActionButton>(R.id.hibernate)?.setOnClickListener {
                AppHelper.hibernate(requireContext())
                Toast.makeText(requireContext(), R.string.toast_stopped_all, Toast.LENGTH_SHORT).show()
                Handler(Looper.getMainLooper()).postDelayed({
                    loadLists()
                }, HIBERNATE_UPDATE_DELAY)
            }
        }

        override fun onStart() {
            super.onStart()
            loadLists()
        }

        private fun generateEmptyListIndicator(): Preference =
            Preference(requireContext()).let {
                it.icon = ContextCompat.getDrawable(requireContext(), R.mipmap.ic_launcher)
                it.title = requireContext().getString(R.string.main_empty)
                it.summary = requireContext().getString(R.string.main_empty_summary)
                it.isSelectable = false
                it
            }

        private fun generatePreference(
            appArray: JSONArray,
            i: Int,
        ): Preference? {
            val preference =
                try {
                    AppHelper.generatePreference(requireContext(), appArray.getString(i))
                } catch (e: NameNotFoundException) {
                    Log.w(Global.LOG_TAG, e)
                    return null
                }
            preference.setOnPreferenceClickListener {
                val options =
                    resources
                        .getStringArray(R.array.main_click_dialog_options)
                        .toMutableList()
                options.add(
                    2,
                    resources.getString(
                        if (ForcedSet.getInstance(requireContext()).contains(it.summary.toString())) {
                            R.string.main_click_dialog_turn_off_always
                        } else {
                            R.string.main_click_dialog_turn_on_always
                        },
                    ),
                )
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.main_click_dialog_title)
                    .setItems(options.toTypedArray()) { _, which ->
                        onDialogItemClicked(which, it.summary.toString(), appArray)
                    }
                    .setNegativeButton(android.R.string.cancel) { _, _ -> }
                    .show()
                true
            }
            return preference
        }

        private fun onDialogItemClicked(
            which: Int,
            packageName: String,
            appArray: JSONArray,
        ) {
            val forcedSet = ForcedSet.getInstance(requireContext())
            when (which) {
                ITEM_STOP_NOW -> {
                    Root.shell("am force-stop $packageName")
                    loadLists()
                    Toast.makeText(context, R.string.toast_stopped, Toast.LENGTH_SHORT)
                        .show()
                }
                ITEM_OPEN_SETTINGS -> {
                    startActivity(
                        Intent().apply {
                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            data = Uri.fromParts("package", packageName, null)
                        },
                    )
                }
                ITEM_ALWAYS_STOP -> {
                    Toast.makeText(
                        context,
                        if (forcedSet.contains(packageName)) {
                            forcedSet.remove(packageName)
                            R.string.main_click_dialog_turn_off_always
                        } else {
                            forcedSet.add(packageName)
                            R.string.main_click_dialog_turn_on_always
                        },
                        Toast.LENGTH_LONG,
                    ).show()
                    forcedSet.save()
                    loadLists()
                }
                ITEM_REMOVE_FROM_LIST -> {
                    for (appIndex in 0 until appArray.length()) {
                        if (appArray.getString(appIndex) == packageName) {
                            appArray.remove(appIndex)
                            break
                        }
                    }
                    PreferenceManager
                        .getDefaultSharedPreferences(requireContext())
                        .edit()
                        .putString(P.PREF_APP_LIST, appArray.toString())
                        .apply()
                    loadLists()
                }
            }
        }

        private fun loadLists() =
            Thread {
                categorySoon.removeAll()
                categoryUnnecessary.removeAll()
                categoryStopped.removeAll()

                val appArray =
                    JSONArray(
                        PreferenceManager
                            .getDefaultSharedPreferences(requireContext())
                            .getString(P.PREF_APP_LIST, P.PREF_APP_LIST_DEFAULT),
                    )
                val preferenceSoonArray: ArrayList<Preference> = ArrayList(appArray.length() / 2)
                val preferenceStoppedArray: ArrayList<Preference> = ArrayList(appArray.length() / 2)
                val services = Root.getServices()

                var preference: Preference
                for (i in 0 until appArray.length()) {
                    preference = generatePreference(appArray, i) ?: continue
                    if (requireContext().packageManager.getApplicationInfo(
                            preference.summary.toString(),
                            PackageManager.GET_META_DATA,
                        ).flags and ApplicationInfo.FLAG_STOPPED != 0
                    ) {
                        preferenceStoppedArray.add(preference)
                    } else {
                        preferenceSoonArray.add(preference)
                    }
                }
                fillLists(preferenceSoonArray, services, preferenceStoppedArray)
            }.start()

        private fun fillLists(
            preferenceSoonArray: ArrayList<Preference>,
            services: HashSet<String>,
            preferenceStoppedArray: ArrayList<Preference>,
        ) {
            var isSoonEmpty = true
            var isUnnecessaryEmpty = true
            for (item in preferenceSoonArray.sortedWith(compareBy { it.title.toString() })) {
                if (
                    item.summary != null &&
                    (
                        services.contains(item.summary ?: error("Impossible state.")) ||
                            ForcedSet.getInstance(requireContext())
                                .contains(item.summary.toString())
                    )
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

        companion object {
            private const val INVALID_LAYOUT = "Invalid layout."
            private const val HIBERNATE_UPDATE_DELAY = 1000L
            private const val ITEM_STOP_NOW = 0
            private const val ITEM_OPEN_SETTINGS = 1
            private const val ITEM_ALWAYS_STOP = 2
            private const val ITEM_REMOVE_FROM_LIST = 3
        }
    }
}
