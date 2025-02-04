package com.mucheng.mucute.client.service

import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import com.mucheng.mucute.client.application.AppContext
import com.mucheng.mucute.client.game.ModuleManager
import com.mucheng.mucute.client.model.GameSettingsModel
import com.mucheng.mucute.client.overlay.OverlayManager
import com.mucheng.mucute.relay.definition.Definitions
import com.mucheng.mucute.relay.listener.NecessaryPacketListener
import com.mucheng.mucute.relay.util.proxyMuCuteRelay
import kotlin.concurrent.thread

// TODO: CANNOT WORK SUCCESSFULLY, NEED TO FIX
class ProxyModeService : VpnService() {

    private var vpnDescriptor: ParcelFileDescriptor? = null

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
            if (action == Services.ACTION_PROXY_START) {
                startMuCuteRelay()
            } else if (action == Services.ACTION_PROXY_STOP) {
                stopMuCuteRelay()
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun stopMuCuteRelay() {
        vpnDescriptor?.close()
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

            val builder = Builder()
            builder.setBlocking(true)
            builder.setMtu(1500)
            builder.setSession("MuCuteRelay")
            builder.addAllowedApplication(gameSettingsModel.selectedGame)
            builder.addDnsServer("8.8.8.8")
            builder.addAddress("10.13.37.1", 30)
            builder.addRoute("0.0.0.0", 0)
            builder.addAddress("1337::1", 126)
            builder.addRoute("::", 0)

            vpnDescriptor = builder.establish()!!

            ModuleManager.loadConfig()

            runCatching {
                Services.muCuteRelay = proxyMuCuteRelay(
                    authSession = gameSettingsModel.selectedAccount,
                    beforeProxy = {
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