package io.github.domi04151309.batterytool.services
import android.app.Service
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import java.lang.ref.WeakReference

class NotificationService : NotificationListenerService() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return Service.START_STICKY
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d("NotificationService", "Notification listener connected")
        instance = WeakReference(this)
//        val notifications = getNotifications().sortedBy { it.postTime }
//        notificationRepository.setNotifications(notifications)
    }

    private fun getNotifications(): Array<StatusBarNotification> {
        return try {
            getNotifications()
        } catch (e: SecurityException) {
            emptyArray()
        }
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        instance = null
        Log.d("NotificationService", "Notification listener disconnected")
    }

    companion object {
        private var instance: WeakReference<NotificationService>? = null
        internal fun getInstance(): NotificationService? {
            return instance?.get()
        }
    }
}