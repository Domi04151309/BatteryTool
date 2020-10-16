package io.github.domi04151309.batterytool.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import androidx.preference.PreferenceManager
import io.github.domi04151309.batterytool.helpers.AppHelper
import io.github.domi04151309.batterytool.helpers.P

class ScreenStateReceiver : BroadcastReceiver() {

    private var isScreenOn = true

    override fun onReceive(c: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_SCREEN_OFF
            && PreferenceManager.getDefaultSharedPreferences(c)
                .getBoolean(P.PREF_AUTO_STOP, P.PREF_AUTO_STOP_DEFAULT)
        ) {
            isScreenOn = false
            Handler().postDelayed(
                { if (!isScreenOn) AppHelper.hibernate(c) },
                PreferenceManager.getDefaultSharedPreferences(c)
                    .getInt(P.PREF_AUTO_STOP_DELAY, P.PREF_AUTO_STOP_DELAY_DEFAULT)
                    .toLong()
            )
        } else if (intent.action == Intent.ACTION_SCREEN_ON
            && PreferenceManager.getDefaultSharedPreferences(c)
                .getBoolean(P.PREF_AUTO_STOP, P.PREF_AUTO_STOP_DEFAULT)
        ) {
            isScreenOn = true
        }
    }
}