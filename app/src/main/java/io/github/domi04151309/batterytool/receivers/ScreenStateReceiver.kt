package io.github.domi04151309.batterytool.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import io.github.domi04151309.batterytool.helpers.AppHelper
import io.github.domi04151309.batterytool.helpers.P
import io.github.domi04151309.batterytool.helpers.Root

class ScreenStateReceiver : BroadcastReceiver() {
    private var isScreenOn = true
    private var isInDozeMode = false

    private fun onScreenOff(context: Context) {
        isScreenOn = false
        if (P.getPreferences(context).getBoolean(P.AUTO_STOP, P.AUTO_STOP_DEFAULT)) {
            Handler(Looper.getMainLooper()).postDelayed(
                { if (!isScreenOn) AppHelper.hibernate(context) },
                P.getPreferences(context).getInt(P.AUTO_STOP_DELAY, P.AUTO_STOP_DELAY_DEFAULT)
                    .toLong() * SECONDS_TO_MILLIS,
            )
        }
        if (P.getPreferences(context).getBoolean(P.AGGRESSIVE_DOZE, P.AGGRESSIVE_DOZE_DEFAULT)) {
            Handler(Looper.getMainLooper()).postDelayed(
                {
                    if (!isScreenOn) {
                        isInDozeMode = true
                        Root.shell("dumpsys deviceidle force-idle")
                    }
                },
                P.getPreferences(context).getInt(
                    P.AGGRESSIVE_DOZE_DELAY,
                    P.AGGRESSIVE_DOZE_DELAY_DEFAULT,
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
