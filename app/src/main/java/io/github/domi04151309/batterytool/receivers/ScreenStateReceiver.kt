package io.github.domi04151309.batterytool.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import androidx.preference.PreferenceManager
import io.github.domi04151309.batterytool.helpers.AppHelper
import io.github.domi04151309.batterytool.helpers.P
import io.github.domi04151309.batterytool.helpers.Root

class ScreenStateReceiver : BroadcastReceiver() {
    private var isScreenOn = true
    private var isInDozeMode = false

    private fun getPrefs(c: Context): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(c)

    private fun onScreenOff(context: Context) {
        isScreenOn = false
        if (getPrefs(context).getBoolean(P.PREF_AUTO_STOP, P.PREF_AUTO_STOP_DEFAULT)) {
            Handler(Looper.getMainLooper()).postDelayed(
                { if (!isScreenOn) AppHelper.hibernate(context) },
                getPrefs(context).getInt(P.PREF_AUTO_STOP_DELAY, P.PREF_AUTO_STOP_DELAY_DEFAULT)
                    .toLong() * SECONDS_TO_MILLIS,
            )
        }
        if (getPrefs(context).getBoolean(P.PREF_AGGRESSIVE_DOZE, P.PREF_AGGRESSIVE_DOZE_DEFAULT)) {
            Handler(Looper.getMainLooper()).postDelayed(
                {
                    if (!isScreenOn) {
                        isInDozeMode = true
                        Root.shell("dumpsys deviceidle force-idle")
                    }
                },
                getPrefs(context).getInt(
                    P.PREF_AGGRESSIVE_DOZE_DELAY,
                    P.PREF_AGGRESSIVE_DOZE_DELAY_DEFAULT,
                )
                    .toLong() * SECONDS_TO_MILLIS,
            )
        }
    }

    private fun onScreenOn() {
        isScreenOn = true
        if (isInDozeMode) {
            Root.shell("dumpsys deviceidle unforce")
            isInDozeMode = false
        }
    }

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        if (intent.action == Intent.ACTION_SCREEN_OFF) {
            onScreenOff(context)
        } else if (intent.action == Intent.ACTION_SCREEN_ON) {
            onScreenOn()
        }
    }

    companion object {
        private const val SECONDS_TO_MILLIS = 1000L
    }
}
