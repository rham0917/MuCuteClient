package com.mucheng.mucute.client.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.mucheng.mucute.client.application.AppContext
import com.mucheng.mucute.client.game.AccountManager
import com.mucheng.mucute.client.game.GameSession
import com.mucheng.mucute.client.game.ModuleManager
import com.mucheng.mucute.client.model.GameSettingsModel
import com.mucheng.mucute.client.overlay.OverlayManager
import com.mucheng.mucute.relay.MuCuteRelaySession
import com.mucheng.mucute.relay.address.MuCuteAddress
import com.mucheng.mucute.relay.definition.Definitions
import com.mucheng.mucute.relay.listener.AutoCodecPacketListener
import com.mucheng.mucute.relay.listener.EncryptedLoginPacketListener
import com.mucheng.mucute.relay.listener.GamingPacketHandler
import com.mucheng.mucute.relay.listener.XboxLoginPacketListener
import com.mucheng.mucute.relay.util.XboxIdentityTokenCacheFileSystem
import com.mucheng.mucute.relay.util.captureMuCuteRelay
import java.io.File
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

        Services.handler.post {
            OverlayManager.dismiss()
        }
    }

    private fun startMuCuteRelay() {
        thread(name = "RakThread") {
            val tokenCacheFile = File(cacheDir, "token_cache.json")
            val gameSettingsSharedPreferences =
                AppContext.instance.getSharedPreferences("game_settings", Context.MODE_PRIVATE)
            val gameSettingsModel = GameSettingsModel.from(gameSettingsSharedPreferences)
            val captureModeModel = gameSettingsModel.captureModeModel

            ModuleManager.loadConfig()

            runCatching {
                Services.muCuteRelay = captureMuCuteRelay(
                    remoteAddress = MuCuteAddress(
                        captureModeModel.serverHostName,
                        captureModeModel.serverPort
                    )
                ) {
                    initModules(this)

                    Definitions.loadBlockPalette()

                    listeners.add(AutoCodecPacketListener(this))
                    val sessionEncryptor = if (AccountManager.currentAccount == null) {
                        EncryptedLoginPacketListener()
                    } else {
                        AccountManager.currentAccount?.let { account ->
                            Log.e("MuCuteRelay", "logged in as ${account.remark}")
                            XboxLoginPacketListener({
                                account.refresh()
                            }, account.platform).also {
                                it.tokenCache =
                                    XboxIdentityTokenCacheFileSystem(tokenCacheFile, account.remark)
                            }
                        }
                    }
                    sessionEncryptor?.let {
                        it.muCuteRelaySession = this
                        listeners.add(it)
                    }
                    listeners.add(GamingPacketHandler(this))
                }

                Services.isActive = true
                Services.handler.post {
                    OverlayManager.show(this@CaptureModeService)
                }

            }.exceptionOrNull()?.let {
                it.printStackTrace()
                Services.handler.post {
                    Toast.makeText(this, it.stackTraceToString(), Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
    }

    private fun initModules(muCuteRelaySession: MuCuteRelaySession) {
        val session = GameSession(muCuteRelaySession)
        muCuteRelaySession.listeners.add(session)

        for (module in ModuleManager.modules) {
            module.session = session
        }
    }

}
