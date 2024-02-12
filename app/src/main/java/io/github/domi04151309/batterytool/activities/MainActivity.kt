package io.github.domi04151309.batterytool.activities

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.elevation.SurfaceColors
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.github.domi04151309.batterytool.R
import io.github.domi04151309.batterytool.adapters.LoadingAdapter
import io.github.domi04151309.batterytool.adapters.SimpleListAdapter
import io.github.domi04151309.batterytool.data.SimpleListItem
import io.github.domi04151309.batterytool.helpers.AppHelper
import io.github.domi04151309.batterytool.helpers.ForcedSet
import io.github.domi04151309.batterytool.helpers.P
import io.github.domi04151309.batterytool.helpers.Root
import io.github.domi04151309.batterytool.interfaces.RecyclerViewHelperInterface
import io.github.domi04151309.batterytool.services.ForegroundService
import org.json.JSONArray

class MainActivity : BaseActivity(), RecyclerViewHelperInterface {
    private val listItems: MutableList<SimpleListItem> = arrayListOf()
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (
            !SetupActivity.demoMode &&
            !PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
                P.PREF_SETUP_COMPLETE,
                P.PREF_SETUP_COMPLETE_DEFAULT,
            )
        ) {
            startActivity(Intent(this, SetupActivity::class.java))
            finish()
            return
        }

        ContextCompat.startForegroundService(this, Intent(this, ForegroundService::class.java))

        window.statusBarColor = SurfaceColors.SURFACE_0.getColor(this)

        recyclerView = findViewById(R.id.list)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = LoadingAdapter()

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

        findViewById<FloatingActionButton>(R.id.hibernate)?.setOnClickListener {
            AppHelper.hibernate(this)
            Toast.makeText(this, R.string.toast_stopped_all, Toast.LENGTH_SHORT).show()
            Handler(Looper.getMainLooper()).postDelayed({ loadApps() }, HIBERNATE_UPDATE_DELAY)
        }
    }

    override fun onStart() {
        super.onStart()
        loadApps()
    }

    private fun loadApps() =
        Thread {
            val blacklist =
                JSONArray(
                    PreferenceManager
                        .getDefaultSharedPreferences(this)
                        .getString(P.PREF_APP_LIST, P.PREF_APP_LIST_DEFAULT),
                )
            val soonApps: MutableList<SimpleListItem> = ArrayList(blacklist.length() / 2)
            val unnecessaryApps: MutableList<SimpleListItem> = ArrayList(blacklist.length() / 2)
            val stoppedApps: MutableList<SimpleListItem> = ArrayList(blacklist.length() / 2)
            val services = Root.getServices()

            @Suppress("LoopWithTooManyJumpStatements")
            for (appIndex in 0 until blacklist.length()) {
                val listItem = AppHelper.generateListItem(this, blacklist.getString(appIndex))
                if (
                    packageManager.getApplicationInfo(
                        listItem.summary,
                        PackageManager.GET_META_DATA,
                    ).flags and ApplicationInfo.FLAG_STOPPED != 0
                ) {
                    stoppedApps.add(listItem)
                    continue
                }
                if (
                    services.contains(listItem.summary) || ForcedSet.getInstance(this).contains(listItem.summary)
                ) {
                    soonApps.add(listItem)
                    continue
                }
                unnecessaryApps.add(listItem)
            }

            val emptyList =
                listOf(
                    SimpleListItem(
                        resources.getString(R.string.main_empty),
                        resources.getString(R.string.main_empty_summary),
                        ResourcesCompat.getDrawable(resources, R.drawable.ic_empty, theme),
                    ),
                )

            listItems.clear()
            listItems.add(SimpleListItem(summary = resources.getString(R.string.main_stopped_soon)))
            listItems.addAll(soonApps.ifEmpty { emptyList })
            listItems.add(SimpleListItem(summary = resources.getString(R.string.main_stopped_unnecessary)))
            listItems.addAll(unnecessaryApps.ifEmpty { emptyList })
            listItems.add(SimpleListItem(summary = resources.getString(R.string.main_stopped)))
            listItems.addAll(stoppedApps.ifEmpty { emptyList })

            Looper.prepare()
            runOnUiThread {
                recyclerView.adapter = SimpleListAdapter(listItems, this)
            }
        }.start()

    override fun onItemClicked(position: Int) {
        if (
            listItems[position].title.isBlank() ||
            listItems[position].title == resources.getString(R.string.main_empty)
        ) {
            return
        }

        val options =
            resources
                .getStringArray(R.array.main_click_dialog_options)
                .toMutableList()
                .apply {
                    add(
                        2,
                        resources.getString(
                            if (
                                ForcedSet.getInstance(this@MainActivity).contains(listItems[position].summary)
                            ) {
                                R.string.main_click_dialog_turn_off_always
                            } else {
                                R.string.main_click_dialog_turn_on_always
                            },
                        ),
                    )
                }
                .toTypedArray()
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.main_click_dialog_title)
            .setItems(options) { _, which ->
                onDialogItemClicked(which, listItems[position].summary)
            }
            .setNegativeButton(android.R.string.cancel) { _, _ -> }
            .show()
    }

    private fun onDialogItemClicked(
        which: Int,
        packageName: String,
    ) {
        when (which) {
            ITEM_STOP_NOW -> {
                Root.shell("am force-stop $packageName")
                loadApps()
                Toast.makeText(this, R.string.toast_stopped, Toast.LENGTH_SHORT).show()
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
                val forcedSet = ForcedSet.getInstance(this)
                Toast.makeText(
                    this,
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
                loadApps()
            }
            ITEM_REMOVE_FROM_LIST -> {
                val apps =
                    JSONArray(
                        PreferenceManager.getDefaultSharedPreferences(this)
                            .getString(P.PREF_APP_LIST, P.PREF_APP_LIST_DEFAULT),
                    )
                for (appIndex in 0 until apps.length()) {
                    if (apps.getString(appIndex) == packageName) {
                        apps.remove(appIndex)
                        break
                    }
                }
                PreferenceManager
                    .getDefaultSharedPreferences(this)
                    .edit()
                    .putString(P.PREF_APP_LIST, apps.toString())
                    .apply()
                loadApps()
            }
        }
    }

    companion object {
        private const val HIBERNATE_UPDATE_DELAY = 1000L
        private const val ITEM_STOP_NOW = 0
        private const val ITEM_OPEN_SETTINGS = 1
        private const val ITEM_ALWAYS_STOP = 2
        private const val ITEM_REMOVE_FROM_LIST = 3
    }
}
