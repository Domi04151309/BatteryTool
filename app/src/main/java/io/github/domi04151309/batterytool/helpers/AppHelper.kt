package io.github.domi04151309.batterytool.helpers

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import org.json.JSONArray

object AppHelper {

    internal fun generatePreference(
        c: Context,
        packageName: String
    ): Preference {
        val applicationInfo = c.packageManager.getApplicationInfo(
            packageName,
            PackageManager.GET_META_DATA
        )
        return generatePreference(c, applicationInfo)
    }

    internal fun generatePreference(
        c: Context,
        applicationInfo: ApplicationInfo
    ): Preference {
        val it = Preference(c)
        it.icon = applicationInfo.loadIcon(c.packageManager)
        it.title = applicationInfo.loadLabel(c.packageManager)
        it.summary = applicationInfo.packageName
        return it
    }

    internal fun hibernate(c: Context) {
        val appArray = JSONArray(
            PreferenceManager.getDefaultSharedPreferences(c)
                .getString(P.PREF_APP_LIST, P.PREF_APP_LIST_DEFAULT)
        )
        val services = Root.getServices()
        val commandArray: ArrayList<String> = ArrayList(appArray.length() / 2)

        Log.e("TAG", "spaghetti noodles")

        for (i in 0 until appArray.length()) {
            if (c.packageManager.getApplicationInfo(
                    appArray.getString(i),
                    PackageManager.GET_META_DATA
                ).flags and ApplicationInfo.FLAG_STOPPED == 0
                && services.contains(appArray.getString(i))
            ) {
                Log.e("TAG", appArray.getString(i))
                commandArray.add("am force-stop ${appArray.getString(i)}")
            }
        }
        Root.shell(commandArray.toArray(arrayOf<String>()))
    }
}