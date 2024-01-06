package io.github.domi04151309.batterytool.receivers

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.service.quicksettings.TileService
import androidx.core.content.ContextCompat
import io.github.domi04151309.batterytool.services.ForegroundService
import io.github.domi04151309.batterytool.services.QuickTileService

class StartReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        if (intent.action == Intent.ACTION_MY_PACKAGE_REPLACED || intent.action == Intent.ACTION_BOOT_COMPLETED) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, ForegroundService::class.java),
            )
            if (Build.VERSION.SDK_INT >= 24) {
                TileService.requestListeningState(
                    context,
                    ComponentName(context, QuickTileService::class.java),
                )
            }
        }
    }
}
