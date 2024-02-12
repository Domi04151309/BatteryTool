package io.github.domi04151309.batterytool.helpers

import android.content.Context
import androidx.preference.PreferenceManager

object P {
    private const val PREF_APP_SET: String = "app_set"
    private const val PREF_FORCED_SET: String = "forced_set"
    private val PREF_APP_SET_DEFAULT: Set<String> = HashSet()
    private val PREF_FORCED_SET_DEFAULT: Set<String> = HashSet()
    internal const val PREF_SETUP_COMPLETE: String = "setup_complete"
    internal const val PREF_AUTO_STOP: String = "auto_stop"
    internal const val PREF_AUTO_STOP_DELAY: String = "auto_stop_delay"
    internal const val PREF_AGGRESSIVE_DOZE: String = "aggressive_doze"
    internal const val PREF_AGGRESSIVE_DOZE_DELAY: String = "aggressive_doze_delay"
    internal const val PREF_IGNORE_FOCUSED_APPS: String = "ignore_focused_apps"
    internal const val PREF_ALLOW_MUSIC: String = "allow_music"
    internal const val PREF_SETUP_COMPLETE_DEFAULT: Boolean = false
    internal const val PREF_AUTO_STOP_DEFAULT: Boolean = true
    internal const val PREF_AUTO_STOP_DELAY_DEFAULT: Int = 10
    internal const val PREF_AGGRESSIVE_DOZE_DEFAULT: Boolean = false
    internal const val PREF_IGNORE_FOCUSED_APPS_DEFAULT: Boolean = false
    internal const val PREF_ALLOW_MUSIC_DEFAULT: Boolean = false
    internal const val PREF_AGGRESSIVE_DOZE_DELAY_DEFAULT: Int = 60 * 10

    internal fun getBlacklist(context: Context): Set<String> =
        PreferenceManager.getDefaultSharedPreferences(context).getStringSet(
            PREF_APP_SET,
            PREF_APP_SET_DEFAULT,
        ) ?: PREF_APP_SET_DEFAULT

    internal fun getBlacklistMutable(context: Context): MutableSet<String> = HashSet(getForced(context))

    internal fun setBlacklist(
        context: Context,
        forced: Set<String>,
    ) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putStringSet(PREF_APP_SET, forced)
            .apply()
    }

    internal fun getForced(context: Context): Set<String> =
        PreferenceManager.getDefaultSharedPreferences(context).getStringSet(
            PREF_FORCED_SET,
            PREF_FORCED_SET_DEFAULT,
        ) ?: PREF_FORCED_SET_DEFAULT

    internal fun getForcedMutable(context: Context): MutableSet<String> = HashSet(getForced(context))

    internal fun setForced(
        context: Context,
        forced: Set<String>,
    ) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putStringSet(PREF_FORCED_SET, forced)
            .apply()
    }
}
