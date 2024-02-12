package io.github.domi04151309.batterytool.activities

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.github.domi04151309.batterytool.R
import io.github.domi04151309.batterytool.adapters.LoadingAdapter
import io.github.domi04151309.batterytool.adapters.SimpleListAdapter
import io.github.domi04151309.batterytool.data.SimpleListItem
import io.github.domi04151309.batterytool.helpers.AppHelper
import io.github.domi04151309.batterytool.helpers.P
import io.github.domi04151309.batterytool.interfaces.RecyclerViewHelperInterface

class AddingActivity : BaseActivity(), RecyclerViewHelperInterface {
    private val listItems: MutableList<SimpleListItem> = arrayListOf()
    private val appsToAdd: MutableList<SimpleListItem> = arrayListOf()
    private lateinit var recyclerView: RecyclerView
    private lateinit var addingList: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adding)

        addingList = findViewById(R.id.adding_list)
        recyclerView = findViewById(R.id.list)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = LoadingAdapter()

        loadApps()

        findViewById<FloatingActionButton>(R.id.add).setOnClickListener {
            onAddClicked()
        }
    }

    private fun isNewValidApp(applicationInfo: ApplicationInfo): Boolean =
        applicationInfo.flags and ApplicationInfo.FLAG_INSTALLED != 0 &&
            applicationInfo.flags and ApplicationInfo.FLAG_HAS_CODE != 0 &&
            applicationInfo.packageName != packageName &&
            !P.getBlacklist(this).toString().contains(applicationInfo.packageName)

    @Suppress("CognitiveComplexMethod")
    private fun loadApps() =
        Thread {
            val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            val userApps: MutableList<SimpleListItem> = ArrayList(installedApps.size)
            val systemApps: MutableList<SimpleListItem> = ArrayList(installedApps.size)
            val internalApps: MutableList<SimpleListItem> = ArrayList(installedApps.size)

            @Suppress("LoopWithTooManyJumpStatements")
            for (app in installedApps) {
                if (isNewValidApp(app)) {
                    val listItem = AppHelper.generateListItem(this, app)
                    if (listItem.title == listItem.summary) continue
                    if (app.flags and ApplicationInfo.FLAG_SYSTEM == 0) {
                        userApps.add(listItem)
                        continue
                    }
                    if (packageManager.getLaunchIntentForPackage(app.packageName) != null) {
                        systemApps.add(listItem)
                        continue
                    }
                    internalApps.add(listItem)
                }
            }

            listItems.add(SimpleListItem(summary = resources.getString(R.string.adding_user)))
            listItems.addAll(userApps)
            listItems.add(SimpleListItem(summary = resources.getString(R.string.adding_system)))
            listItems.addAll(systemApps)
            listItems.add(SimpleListItem(summary = resources.getString(R.string.adding_internal)))
            listItems.addAll(internalApps)

            Looper.prepare()
            runOnUiThread {
                recyclerView.adapter = SimpleListAdapter(listItems, this)
            }
        }.start()

    private fun onAddClicked() {
        P.setBlacklist(
            this,
            P.getBlacklist(this).apply {
                for (app in appsToAdd) put(app.summary)
            },
        )
        finish()
    }

    override fun onItemClicked(position: Int) {
        if (listItems[position].title.isBlank()) return

        if (appsToAdd.contains(listItems[position])) {
            listItems[position].icon = packageManager.getApplicationIcon(listItems[position].summary)
            appsToAdd.remove(listItems[position])
        } else {
            listItems[position].icon = ResourcesCompat.getDrawable(resources, R.drawable.overlay_icon, theme)
            appsToAdd.add(listItems[position])
        }
        recyclerView.adapter?.notifyItemChanged(position)

        addingList.text = appsToAdd.joinToString { it.title }
    }
}
