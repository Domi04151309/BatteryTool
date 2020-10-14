package io.github.domi04151309.batterytool.helpers

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.preference.Preference

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
}