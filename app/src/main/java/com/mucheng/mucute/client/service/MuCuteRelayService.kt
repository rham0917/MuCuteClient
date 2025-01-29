package com.mucheng.mucute.client.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.mucheng.mucute.client.R
import com.mucheng.mucute.client.activity.MainActivity
import com.mucheng.mucute.client.application.AppContext
import com.mucheng.mucute.client.game.ModuleManager
import com.mucheng.mucute.client.model.GameSettingsModel
import com.mucheng.mucute.client.overlay.OverlayManager
import com.mucheng.mucute.relay.MuCuteRelay
import com.mucheng.mucute.relay.definition.Definitions
import com.mucheng.mucute.relay.listener.NecessaryPacketListener
import com.mucheng.mucute.relay.util.captureMuCuteRelay
import java.net.InetSocketAddress
import kotlin.concurrent.thread

class MuCuteRelayService : Service() {

    companion object {

        private const val CHANNEL_ID = "com.mucheng.mucute.client.NOTIFICATION_CHANNEL_ID"

        const val ACTION_START = "com.mucheng.mucute.relay.start"
        const val ACTION_STOP = "com.mucheng.mucute.relay.stop"

        var isActive by mutableStateOf(false)
            private set

    }

    private lateinit var notificationManagerCompat: NotificationManagerCompat

    private var muCuteRelay: MuCuteRelay? = null

    private val handler = Handler(Looper.getMainLooper())

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        notificationManagerCompat = NotificationManagerCompat.from(this)

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

    override fun onDestroy() {
        super.onDestroy()
        stopMuCuteRelay()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent ?: return START_NOT_STICKY

        val action = intent.action
        try {
            if (action == ACTION_START) {
                startMuCuteRelay()
            } else {
                stopMuCuteRelay()
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun stopMuCuteRelay() {
        muCuteRelay?.disconnect()
        isActive = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        thread {
            ModuleManager.saveConfig()
        }
        OverlayManager.dismiss()
    }

    private fun startMuCuteRelay() {
        thread(
            name = "MuCuteRelayThread",
            priority = Thread.MAX_PRIORITY
        ) {
            val gameSettingsSharedPreferences =
                AppContext.instance.getSharedPreferences("game_settings", Context.MODE_PRIVATE)
            val gameSettingsModel = GameSettingsModel.from(gameSettingsSharedPreferences)
            val captureModeModel = gameSettingsModel.captureModeModel

            ModuleManager.loadConfig()

            runCatching {
                muCuteRelay = captureMuCuteRelay(
                    authSession = gameSettingsModel.selectedAccount,
                    remoteAddress = InetSocketAddress(
                        captureModeModel.serverHostName,
                        captureModeModel.serverPort
                    ),
                    beforeCapture = {
                        isActive = true
                        if (Build.VERSION.SDK_INT >= 34) {
                            startForeground(
                                1,
                                createNotification(),
                                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                            )
                        } else {
                            startForeground(
                                1,
                                createNotification()
                            )
                        }

                        handler.post {
                            OverlayManager.show(this)
                        }
                    }
                ) {
                    ModuleManager.initModules(this)

                    listeners.add(ModuleManager)
                    listeners.add(NecessaryPacketListener(this))
                }
            }
        }
    }

    private fun createNotification(): Notification {
        val flag = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT

        val intent = Intent(this, MainActivity::class.java)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        intent.action = Intent.ACTION_MAIN

        val goHomePendingIntent = PendingIntent.getActivity(this, 0, intent, flag)

        val stopIntent = Intent(ACTION_STOP)
        stopIntent.setPackage(packageName)

        val stopPendingIntent = PendingIntent.getForegroundService(this, 1, stopIntent, flag)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
            .setContentTitle("MuCuteRelay")
            .setContentText(getString(R.string.capturing_game_packets))
            .setOngoing(true)
            .setContentIntent(goHomePendingIntent)
            .addAction(R.mipmap.ic_launcher, getString(R.string.stop), stopPendingIntent)
            .build()
    }

}