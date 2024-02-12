package io.github.domi04151309.batterytool.helpers

import android.content.Context
import androidx.preference.PreferenceManager

object P {
    private const val APP_SET: String = "app_set"
    private const val FORCED_SET: String = "forced_set"
    private val APP_SET_DEFAULT: Set<String> = HashSet()
    private val FORCED_SET_DEFAULT: Set<String> = HashSet()
    internal const val SETUP_COMPLETE: String = "setup_complete"
    internal const val AUTO_STOP: String = "auto_stop"
    internal const val AUTO_STOP_DELAY: String = "auto_stop_delay"
    internal const val AGGRESSIVE_DOZE: String = "aggressive_doze"
    internal const val AGGRESSIVE_DOZE_DELAY: String = "aggressive_doze_delay"
    internal const val IGNORE_FOCUSED_APPS: String = "ignore_focused_apps"
    internal const val ALLOW_MUSIC: String = "allow_music"
    internal const val SETUP_COMPLETE_DEFAULT: Boolean = false
    internal const val AUTO_STOP_DEFAULT: Boolean = true
    internal const val AUTO_STOP_DELAY_DEFAULT: Int = 10
    internal const val AGGRESSIVE_DOZE_DEFAULT: Boolean = false
    internal const val IGNORE_FOCUSED_APPS_DEFAULT: Boolean = false
    internal const val ALLOW_MUSIC_DEFAULT: Boolean = false
    internal const val AGGRESSIVE_DOZE_DELAY_DEFAULT: Int = 60 * 10

    internal fun getBlacklist(context: Context): Set<String> =
        PreferenceManager.getDefaultSharedPreferences(context).getStringSet(
            APP_SET,
            APP_SET_DEFAULT,
        ) ?: APP_SET_DEFAULT

    internal fun getBlacklistMutable(context: Context): MutableSet<String> = HashSet(getForced(context))

    internal fun setBlacklist(
        context: Context,
        forced: Set<String>,
    ) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putStringSet(APP_SET, forced)
            .apply()
    }

    internal fun getForced(context: Context): Set<String> =
        PreferenceManager.getDefaultSharedPreferences(context).getStringSet(
            FORCED_SET,
            FORCED_SET_DEFAULT,
        ) ?: FORCED_SET_DEFAULT

    internal fun getForcedMutable(context: Context): MutableSet<String> = HashSet(getForced(context))

    internal fun setForced(
        context: Context,
        forced: Set<String>,
    ) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putStringSet(FORCED_SET, forced)
            .apply()
    }
}
