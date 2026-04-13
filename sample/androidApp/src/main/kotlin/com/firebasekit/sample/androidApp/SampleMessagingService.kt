package com.firebasekit.sample.androidApp

import android.Manifest
import android.app.PendingIntent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

class SampleMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        val notification = message.notification ?: return
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName) ?: return

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val localNotification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(notification.title ?: "Firebase Kit sample")
            .setContentText(notification.body ?: "New Firebase Messaging sample push")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // A background service cannot request runtime permissions. AppActivity does that
            // when the sample app is opened on Android 13+.
            return
        }

        NotificationManagerCompat.from(this).notify(Random.nextInt(), localNotification)
    }

    override fun onNewToken(token: String) {
        // Skipped for simplicity
    }

    companion object {
        const val CHANNEL_ID = "firebase-kit-sample"
    }
}
