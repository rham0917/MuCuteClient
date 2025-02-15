package com.mucheng.mucute.client.service

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.mucheng.mucute.client.game.AccountManager
import com.mucheng.mucute.client.game.GameSession
import com.mucheng.mucute.client.game.ModuleManager
import com.mucheng.mucute.client.model.CaptureModeModel
import com.mucheng.mucute.client.overlay.OverlayManager
import com.mucheng.mucute.relay.MuCuteRelay
import com.mucheng.mucute.relay.MuCuteRelaySession
import com.mucheng.mucute.relay.address.MuCuteAddress
import com.mucheng.mucute.relay.definition.Definitions
import com.mucheng.mucute.relay.listener.AutoCodecPacketListener
import com.mucheng.mucute.relay.listener.GamingPacketHandler
import com.mucheng.mucute.relay.listener.OfflineLoginPacketListener
import com.mucheng.mucute.relay.listener.OnlineLoginPacketListener
import com.mucheng.mucute.relay.util.captureGamePacket
import java.io.File
import kotlin.concurrent.thread

@Suppress("MemberVisibilityCanBePrivate")
object Services {

    private val handler = Handler(Looper.getMainLooper())

    private var muCuteRelay: MuCuteRelay? = null

    private var thread: Thread? = null

    var isActive by mutableStateOf(false)

    fun toggle(context: Context, captureModeModel: CaptureModeModel) {
        if (!isActive) {
            on(context, captureModeModel)
            return
        }

        off()
    }

    private fun on(context: Context, captureModeModel: CaptureModeModel) {
        if (this.thread != null) {
            return
        }

        val tokenCacheFile = File(context.cacheDir, "token_cache.json")

        isActive = true
        handler.post {
            OverlayManager.show(context)
        }

        this.thread = thread(
            name = "MuCuteRelayThread",
            priority = Thread.MAX_PRIORITY
        ) {
            // Load module configurations
            runCatching {
                ModuleManager.loadConfig()
            }.exceptionOrNull()?.let {
                it.printStackTrace()
                context.toast("Load configuration error: ${it.message}")
            }

            runCatching {
                Definitions.loadBlockPalette()
            }.exceptionOrNull()?.let {
                it.printStackTrace()
                context.toast("Load block palette error: ${it.message}")
            }

            val selectedAccount = AccountManager.selectedAccount
            // Start MuCuteRelay to capture game packets
            runCatching {
                muCuteRelay = captureGamePacket(
                    remoteAddress = MuCuteAddress(
                        captureModeModel.serverHostName,
                        captureModeModel.serverPort
                    )
                ) {
                    initModules(this)

                    listeners.add(AutoCodecPacketListener(this))
                    listeners.add(
                        if (selectedAccount == null) OfflineLoginPacketListener(this) else OnlineLoginPacketListener(
                            this,
                            selectedAccount
                        )
                    )
                    listeners.add(GamingPacketHandler(this))
                }
            }.exceptionOrNull()?.let {
                it.printStackTrace()
                context.toast("Start MuCuteRelay error: ${it.stackTraceToString()}")
            }

        }
    }

    private fun off() {
        thread(name = "MuCuteRelayThread") {
            ModuleManager.saveConfig()
            handler.post {
                OverlayManager.dismiss()
            }
            isActive = false
            muCuteRelay?.disconnect()
            thread?.interrupt()
            thread = null
        }
    }

    private fun Context.toast(message: String) {
        handler.post {
            Toast.makeText(this, message, Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun initModules(muCuteRelaySession: MuCuteRelaySession) {
        val session = GameSession(muCuteRelaySession)
        muCuteRelaySession.listeners.add(session)

        for (module in ModuleManager.modules) {
            module.session = session
        }
        Log.e("Services", "Init session")
    }

}