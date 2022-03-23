package io.github.domi04151309.batterytool.services
import android.app.Notification
import android.app.Service
import android.content.Intent
import android.media.session.MediaSession
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import java.lang.ref.WeakReference

class NotificationService : NotificationListenerService() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return Service.START_STICKY
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        instance = WeakReference(this)
    }

    private fun getNotifications(): List<StatusBarNotification> {
        return try {
            activeNotifications.sortedBy { it.postTime }
        } catch (e: SecurityException) {
            emptyList<StatusBarNotification>()
        }
    }
    fun getPlayingPackageName(): String? {
        return try {
            var notifications = getNotifications().filter {
                it.notification.category == Notification.CATEGORY_TRANSPORT || it.notification.category == Notification.CATEGORY_SERVICE
            }
            var notification = notifications.findLast {
                    it.notification.extras[NotificationCompat.EXTRA_MEDIA_SESSION] as? MediaSession.Token != null
            }
            return notification?.packageName ?: null
        } catch (e: SecurityException) {
            null
        }
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        instance = null
    }

    companion object {
        private var instance: WeakReference<NotificationService>? = null
        internal fun getInstance(): NotificationService? {
            return instance?.get()
        }
    }
}