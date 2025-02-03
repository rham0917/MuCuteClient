package com.mucheng.mucute.client.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.mucheng.mucute.client.R
import com.mucheng.mucute.client.activity.MainActivity
import com.mucheng.mucute.client.util.WorkModes
import com.mucheng.mucute.relay.MuCuteRelay

@Suppress("MemberVisibilityCanBePrivate")
object Services {

    private const val CHANNEL_ID = "com.mucheng.mucute.client.NOTIFICATION_CHANNEL_ID"

    const val ACTION_CAPTURE_START = "com.mucheng.mucute.relay.capture.start"
    const val ACTION_CAPTURE_STOP = "com.mucheng.mucute.relay.capture.stop"
    const val ACTION_PROXY_START = "com.mucheng.mucute.relay.proxy.start"
    const val ACTION_PROXY_STOP = "com.mucheng.mucute.relay.proxy.stop"

    var isActive by mutableStateOf(false)

    @SuppressLint("StaticFieldLeak")
    private lateinit var notificationManagerCompat: NotificationManagerCompat

    val handler = Handler(Looper.getMainLooper())

    var muCuteRelay: MuCuteRelay? = null

    fun createNotificationChannel(service: Service) {
        notificationManagerCompat = NotificationManagerCompat.from(service)

        if (notificationManagerCompat.getNotificationChannel(CHANNEL_ID) == null) {
            notificationManagerCompat.createNotificationChannel(
                NotificationChannelCompat.Builder(
                    CHANNEL_ID,
                    NotificationManagerCompat.IMPORTANCE_DEFAULT
                )
                    .setName("MuCuteRelay")
                    .setDescription("Create foreground notification and keep MuCuteRelay running.")
                    .build()
            )
        }
    }

    fun createNotification(service: Service, workMode: WorkModes): Notification {
        val flag = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT

        val intent = Intent(service, MainActivity::class.java)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        intent.action = Intent.ACTION_MAIN

        val goHomePendingIntent = PendingIntent.getActivity(service, 0, intent, flag)

        val stopIntent = Intent(ACTION_CAPTURE_STOP)
        stopIntent.setPackage(service.packageName)

        val stopPendingIntent = PendingIntent.getForegroundService(service, 1, stopIntent, flag)

        return NotificationCompat.Builder(service, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setLargeIcon(BitmapFactory.decodeResource(service.resources, R.mipmap.ic_launcher))
            .setContentTitle("MuCuteRelay")
            .setContentText(service.getString(when (workMode) {
                WorkModes.CaptureMode -> R.string.capturing_game_packets
                WorkModes.ProxyMode -> R.string.proxying_game_packets
            }))
            .setOngoing(true)
            .setContentIntent(goHomePendingIntent)
            .addAction(R.mipmap.ic_launcher, service.getString(R.string.stop), stopPendingIntent)
            .build()
    }

}