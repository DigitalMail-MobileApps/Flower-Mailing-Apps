package org.lsm.flower_mailing.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.lsm.flower_mailing.MainActivity
import org.lsm.flower_mailing.R
import org.lsm.flower_mailing.data.local.AppDatabase
import org.lsm.flower_mailing.data.local.NotificationEntity

class AppFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val data = message.data
        if (data.isNotEmpty()) {
            val letterId = data["letter_id"]
            val action = data["action"]
            val status = data["status"]

            val title = message.notification?.title ?: when (action) {
                "letter_created" -> "Surat Masuk Baru"
                "status_change" -> "Update Status Surat"
                else -> "Notifikasi Surat"
            }

            val body = message.notification?.body ?: when (action) {
                "letter_created" -> "Ada surat baru yang perlu diproses."
                "status_change" -> "Status surat berubah menjadi: ${formatStatus(status)}"
                else -> "Ketuk untuk melihat detail."
            }

            saveNotificationToDb(title, body, letterId)
            showNotification(letterId, title, body)
        }
    }

    private fun saveNotificationToDb(title: String, body: String, letterId: String?) {
        val db = AppDatabase.getDatabase(applicationContext)
        CoroutineScope(Dispatchers.IO).launch {
            db.notificationDao().insert(
                NotificationEntity(
                    title = title,
                    body = body,
                    letterId = letterId
                )
            )
        }
    }

    private fun formatStatus(status: String?): String {
        return status?.replace("_", " ")?.replaceFirstChar { it.uppercase() } ?: "Unknown"
    }

    private fun showNotification(letterId: String?, title: String, body: String) {
        val context = applicationContext
        val channelId = "mailing_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Mailing Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                setSound(
                    android.provider.Settings.System.DEFAULT_NOTIFICATION_URI,
                    android.media.AudioAttributes.Builder()
                        .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                        .build()
                )
            }
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("letterIdFromNotification", letterId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            (letterId ?: "0").hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_SOUND or NotificationCompat.DEFAULT_VIBRATE)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()

        try {
            NotificationManagerCompat.from(context).notify(
                (letterId ?: System.currentTimeMillis().toString()).hashCode(),
                notification
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}