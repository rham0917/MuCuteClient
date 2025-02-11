package com.mucheng.mucute.client.service

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.mucheng.mucute.relay.MuCuteRelay
import com.mucheng.mucute.client.model.CaptureModeModel
import com.mucheng.mucute.relay.util.captureMuCuteRelay
import com.mucheng.mucute.client.game.GameSession
import com.mucheng.mucute.client.game.ModuleManager
import com.mucheng.mucute.relay.address.MuCuteAddress
import com.mucheng.mucute.relay.MuCuteRelaySession
import com.mucheng.mucute.client.overlay.OverlayManager
import com.mucheng.mucute.relay.definition.Definitions
import com.mucheng.mucute.relay.listener.AutoCodecPacketListener
import com.mucheng.mucute.relay.listener.EncryptedLoginPacketListener
import com.mucheng.mucute.relay.listener.GamingPacketHandler
import com.mucheng.mucute.relay.listener.XboxLoginPacketListener
import com.mucheng.mucute.relay.util.XboxIdentityTokenCacheFileSystem
import java.io.File
import com.mucheng.mucute.client.game.AccountManager
import android.widget.Toast
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
        
        this.thread = thread(name = "MuCuteRelayThread") {
            // Load module configurations
            runCatching {
                ModuleManager.loadConfig()
            }.exceptionOrNull()?.let { 
                it.printStackTrace()
                context.toast("Load configuration error: ${it.message}");
            }
            
            runCatching {
                Definitions.loadBlockPalette()
            }.exceptionOrNull()?.let { 
                it.printStackTrace()
                context.toast("Load block palette error: ${it.message}");
            }
            
            val sessionEncryptor = if (AccountManager.currentAccount == null) {
                EncryptedLoginPacketListener()
            } else {
                AccountManager.currentAccount?.let { account ->
                Log.e("MuCuteRelay", "Logged in as ${account.remark}")
                    XboxLoginPacketListener({ account.refresh() }, account.platform).also {
                        it.tokenCache = XboxIdentityTokenCacheFileSystem(tokenCacheFile, account.remark)
                    }
                }
            }
            
            // Start MuCuteRelay to capture game packets
            runCatching {
                muCuteRelay = captureMuCuteRelay(
                    remoteAddress = MuCuteAddress(
                        captureModeModel.serverHostName,
                        captureModeModel.serverPort
                    )
                ) {
                    initModules(this)
                    
                    listeners.add(AutoCodecPacketListener(this))
                    sessionEncryptor?.let {
                        it.muCuteRelaySession = this
                        listeners.add(it)
                    }
                    listeners.add(GamingPacketHandler(this))
                }
            }.exceptionOrNull()?.let { 
                it.printStackTrace()
                context.toast("Start MuCuteRelay error: ${it.stackTraceToString()}");
            }
            
        }
    }
    
    private fun off() {
        thread(name = "ConfigurationThread") {
            ModuleManager.saveConfig()
        }
        
        isActive = false
        muCuteRelay?.disconnect()
        thread?.interrupt()
        thread = null
        handler.post {
            OverlayManager.dismiss()
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
    }

}