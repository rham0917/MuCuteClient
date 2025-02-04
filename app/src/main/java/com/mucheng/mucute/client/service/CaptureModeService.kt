package com.mucheng.mucute.client.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import com.mucheng.mucute.client.application.AppContext
import com.mucheng.mucute.client.game.ModuleManager
import com.mucheng.mucute.client.model.GameSettingsModel
import com.mucheng.mucute.client.overlay.OverlayManager
import com.mucheng.mucute.relay.definition.Definitions
import com.mucheng.mucute.relay.listener.NecessaryPacketListener
import com.mucheng.mucute.relay.util.captureMuCuteRelay
import java.net.InetSocketAddress
import kotlin.concurrent.thread

open class CaptureModeService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Services.createNotificationChannel(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopMuCuteRelay()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent ?: return START_NOT_STICKY

        val action = intent.action
        try {
            if (action == Services.ACTION_CAPTURE_START) {
                startMuCuteRelay()
            } else if (action == Services.ACTION_CAPTURE_STOP) {
                stopMuCuteRelay()
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun stopMuCuteRelay() {
        Services.muCuteRelay?.disconnect()
        Services.isActive = false
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
                Services.muCuteRelay = captureMuCuteRelay(
                    authSession = gameSettingsModel.selectedAccount,
                    remoteAddress = InetSocketAddress(
                        captureModeModel.serverHostName,
                        captureModeModel.serverPort
                    ),
                    beforeCapture = {
                        Services.isActive = true
                        if (Build.VERSION.SDK_INT >= 34) {
                            startForeground(
                                1,
                                Services.createNotification(this, gameSettingsModel.workMode),
                                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                            )
                        } else {
                            startForeground(
                                1,
                                Services.createNotification(this, gameSettingsModel.workMode)
                            )
                        }

                        Services.handler.post {
                            OverlayManager.show(this)
                        }

                        Definitions.loadBlockPalette()
                    }
                ) {
                    ModuleManager.initModules(this)

                    listeners.add(ModuleManager)
                    listeners.add(NecessaryPacketListener(this))
                }
            }
        }
    }

}