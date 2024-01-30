package io.github.domi04151309.batterytool.services

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.media.session.MediaSession
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.media2.common.SessionPlayer
import androidx.media2.session.MediaController
import androidx.media2.session.SessionCommandGroup
import io.github.domi04151309.batterytool.helpers.Global
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.concurrent.Executors

class NotificationService : NotificationListenerService() {
    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int = Service.START_STICKY

    private fun getNotifications(): List<StatusBarNotification> =
        try {
            activeNotifications.sortedBy { it.postTime }
        } catch (exception: SecurityException) {
            Log.w(Global.LOG_TAG, exception)
            emptyList()
        }

    fun getPlayingPackageName(callback: (String?) -> Unit) {
        try {
            val notification =
                getNotifications().filter {
                    it.notification.category == Notification.CATEGORY_TRANSPORT ||
                        it.notification.category == Notification.CATEGORY_SERVICE
                }.findLast {
                    it.notification.extras[NotificationCompat.EXTRA_MEDIA_SESSION] as? MediaSession.Token != null
                }
            if (notification == null) {
                callback(null)
                return
            }
            val token =
                notification.notification.extras[NotificationCompat.EXTRA_MEDIA_SESSION] as? MediaSession.Token
            var mediaController: MediaController? = null
            val mediaSessionCallback =
                object : MediaController.ControllerCallback() {
                    override fun onConnected(
                        controller: MediaController,
                        allowedCommands: SessionCommandGroup,
                    ) {
                        super.onConnected(controller, allowedCommands)
                        if (controller != mediaController) return
                        if (controller.playerState == SessionPlayer.PLAYER_STATE_PLAYING) {
                            callback(notification.packageName)
                        } else {
                            callback(null)
                        }
                        try {
                            mediaController?.close()
                        } catch (exception: IOException) {
                            Log.w(Global.LOG_TAG, exception)
                        }
                    }
                }
            mediaController =
                MediaController.Builder(this)
                    .setSessionCompatToken(MediaSessionCompat.Token.fromToken(token))
                    .setControllerCallback(
                        Executors.newSingleThreadExecutor(),
                        mediaSessionCallback,
                    )
                    .build()
        } catch (exception: SecurityException) {
            Log.w(Global.LOG_TAG, exception)
            callback(null)
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        instance = WeakReference(this)
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        instance = null
    }

    companion object {
        private var instance: WeakReference<NotificationService>? = null

        internal fun getInstance(): NotificationService? = instance?.get()
    }
}
