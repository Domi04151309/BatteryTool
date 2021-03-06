package io.github.domi04151309.batterytool.helpers

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import org.json.JSONArray

object AppHelper {

    internal fun generatePreference(
        c: Context,
        packageName: String
    ): Preference {
        return Preference(c).let {
            it.icon = c.packageManager.getApplicationIcon(packageName)
            it.title = c.packageManager.getApplicationLabel(
                c.packageManager.getApplicationInfo(
                    packageName,
                    PackageManager.GET_META_DATA
                )
            )
            it.summary = packageName
            it
        }
    }

    internal fun generatePreference(
        c: Context,
        applicationInfo: ApplicationInfo
    ): Preference {
        return Preference(c).let {
            it.icon = applicationInfo.loadIcon(c.packageManager)
            it.title = applicationInfo.loadLabel(c.packageManager)
            it.summary = applicationInfo.packageName
            it
        }
    }

    internal fun hibernate(c: Context) {
        val appArray = JSONArray(
            PreferenceManager.getDefaultSharedPreferences(c)
                .getString(P.PREF_APP_LIST, P.PREF_APP_LIST_DEFAULT)
        )
        val services = Root.getServices()
        val commandArray: ArrayList<String> = ArrayList(appArray.length() / 2)

        for (i in 0 until appArray.length()) {
            try {
                if (c.packageManager.getApplicationInfo(
                        appArray.getString(i),
                        PackageManager.GET_META_DATA
                    ).flags and ApplicationInfo.FLAG_STOPPED == 0
                    && services.contains(appArray.getString(i))
                ) {
                    commandArray.add("am force-stop ${appArray.getString(i)}")
                }
            } catch (e: PackageManager.NameNotFoundException) {
                continue
            }
        }
        if (commandArray.isNotEmpty()) Root.shell(commandArray.toArray(arrayOf<String>()))
    }
}