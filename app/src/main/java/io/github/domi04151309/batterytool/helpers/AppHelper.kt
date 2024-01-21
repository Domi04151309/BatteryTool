package io.github.domi04151309.batterytool.helpers

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import io.github.domi04151309.batterytool.R
import io.github.domi04151309.batterytool.services.NotificationService
import org.json.JSONArray

object AppHelper {
    internal fun generatePreference(
        context: Context,
        packageName: String,
    ): Preference =
        Preference(context).let {
            it.icon = context.packageManager.getApplicationIcon(packageName)
            it.title =
                context.packageManager.getApplicationLabel(
                    context.packageManager.getApplicationInfo(
                        packageName,
                        PackageManager.GET_META_DATA,
                    ),
                )
            it.summary = packageName
            if (ForcedSet.getInstance(context).contains(packageName)) {
                it.title = it.title as String +
                    " " +
                    context.resources.getString(R.string.main_forced)
            }
            it
        }

    internal fun generatePreference(
        context: Context,
        applicationInfo: ApplicationInfo,
    ): Preference =
        Preference(context).let {
            it.icon = applicationInfo.loadIcon(context.packageManager)
            it.title = applicationInfo.loadLabel(context.packageManager)
            it.summary = applicationInfo.packageName
            it
        }

    private fun hibernateApps(
        context: Context,
        playingMusicPackage: String?,
    ) {
        val appArray =
            JSONArray(
                PreferenceManager.getDefaultSharedPreferences(context).getString(
                    P.PREF_APP_LIST,
                    P.PREF_APP_LIST_DEFAULT,
                ),
            )
        val commandArray: ArrayList<String> = ArrayList(appArray.length() / 2)
        val services = Root.getServices()
        val focused =
            if (
                PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
                    P.PREF_IGNORE_FOCUSED_APPS,
                    P.PREF_IGNORE_FOCUSED_APPS_DEFAULT,
                )
            ) {
                Root.getFocusedApps()
            } else {
                PseudoHashSet()
            }
        for (i in 0 until appArray.length()) {
            try {
                val packageName = appArray.getString(i)
                @Suppress("ComplexCondition")
                if (
                    !packageName.equals(playingMusicPackage) &&
                    !focused.contains(packageName) &&
                    context.packageManager.getApplicationInfo(
                        packageName,
                        PackageManager.GET_META_DATA,
                    ).flags and ApplicationInfo.FLAG_STOPPED == 0 &&
                    (services.contains(packageName) || ForcedSet.getInstance(context).contains(packageName))
                ) {
                    commandArray.add("am force-stop $packageName")
                }
            } catch (e: PackageManager.NameNotFoundException) {
                Log.w(Global.LOG_TAG, e)
                continue
            }
        }
        if (commandArray.isNotEmpty()) Root.shell(commandArray.toTypedArray())
    }

    internal fun hibernate(c: Context) {
        val whitelistMusicApps =
            PreferenceManager.getDefaultSharedPreferences(c)
                .getBoolean(P.PREF_ALLOW_MUSIC, P.PREF_ALLOW_MUSIC_DEFAULT)
        if (whitelistMusicApps) {
            NotificationService.getInstance()?.getPlayingPackageName { packageName ->
                hibernateApps(
                    c,
                    packageName,
                )
            }
        } else {
            hibernateApps(c, null)
        }
    }
}
