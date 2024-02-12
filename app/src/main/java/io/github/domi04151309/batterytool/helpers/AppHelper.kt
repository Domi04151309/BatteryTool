package io.github.domi04151309.batterytool.helpers

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import androidx.preference.PreferenceManager
import io.github.domi04151309.batterytool.R
import io.github.domi04151309.batterytool.data.SimpleListItem
import io.github.domi04151309.batterytool.services.NotificationService

object AppHelper {
    fun generateListItem(
        context: Context,
        packageName: String,
    ): SimpleListItem =
        SimpleListItem(
            context.packageManager.getApplicationLabel(
                context.packageManager.getApplicationInfo(
                    packageName,
                    PackageManager.GET_META_DATA,
                ),
            ).toString() +
                if (ForcedSet.getInstance(context).contains(packageName)) {
                    " " + context.resources.getString(R.string.main_forced)
                } else {
                    ""
                },
            packageName,
            context.packageManager.getApplicationIcon(packageName),
        )

    fun generateListItem(
        context: Context,
        applicationInfo: ApplicationInfo,
    ): SimpleListItem =
        SimpleListItem(
            applicationInfo.loadLabel(context.packageManager).toString(),
            applicationInfo.packageName,
            applicationInfo.loadIcon(context.packageManager),
        )

    private fun hibernateApps(
        context: Context,
        playingMusicPackage: String?,
    ) {
        val apps = P.getBlacklist(context)
        val commands: ArrayList<String> = ArrayList(apps.length() / 2)
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
        for (i in 0 until apps.length()) {
            try {
                val packageName = apps.getString(i)
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
                    commands.add("am force-stop $packageName")
                }
            } catch (exception: PackageManager.NameNotFoundException) {
                Log.w(Global.LOG_TAG, exception)
                continue
            }
        }
        if (commands.isNotEmpty()) Root.shell(commands.toTypedArray())
    }

    internal fun hibernate(context: Context) {
        if (PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(P.PREF_ALLOW_MUSIC, P.PREF_ALLOW_MUSIC_DEFAULT)
        ) {
            NotificationService.getInstance()?.getPlayingPackageName { packageName ->
                hibernateApps(
                    context,
                    packageName,
                )
            }
        } else {
            hibernateApps(context, null)
        }
    }
}
