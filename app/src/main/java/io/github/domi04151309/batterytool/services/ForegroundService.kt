package io.github.domi04151309.batterytool.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.*
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import io.github.domi04151309.batterytool.R

class ForegroundService : Service() {

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        createNotificationChannel()
        startForeground(
            1,
            NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentText(getString(R.string.service_text))
                .setSmallIcon(R.drawable.ic_spa)
                .setColor(getColor(R.color.colorAccent))
                .setShowWhen(false)
                .build()
        )
        return START_REDELIVER_INTENT
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.service_channel),
                NotificationManager.IMPORTANCE_LOW
            )
            channel.setShowBadge(false)
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID: String = "service_channel"
    }
}