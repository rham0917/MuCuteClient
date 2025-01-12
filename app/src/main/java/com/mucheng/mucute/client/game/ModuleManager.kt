package com.mucheng.mucute.client.game

import android.util.Log
import com.mucheng.mucute.client.application.AppContext
import com.mucheng.mucute.client.game.entity.LocalPlayer
import com.mucheng.mucute.client.game.module.motion.FlyModule
import com.mucheng.mucute.relay.MinecraftRelaySession
import com.mucheng.mucute.relay.handler.MinecraftRelayPacketHandler
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.TextPacket

object ModuleManager : MinecraftRelayPacketHandler {

    private val _modules: MutableList<Module> = ArrayList()

    val modules: List<Module> = _modules

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    private val localPlayer = LocalPlayer()

    private var session: MinecraftRelaySession? = null

    init {
        with(_modules) {
            add(FlyModule())
        }
    }

    fun initModules(minecraftRelaySession: MinecraftRelaySession) {
        this.session = minecraftRelaySession

        for (module in _modules) {
            module.session = minecraftRelaySession
            module.localPlayer = localPlayer
        }
    }

    fun saveConfig() {
        val configsDir = AppContext.instance.filesDir.resolve("configs")
        configsDir.mkdirs()

        val config = configsDir.resolve("UserConfig.json")
        val jsonObject = buildJsonObject {
            put("modules", buildJsonObject {
                _modules.forEach {
                    put(it.name, it.toJson())
                }
            })
        }

        config.writeText(json.encodeToString(jsonObject))
    }

    fun loadConfig() {
        val configsDir = AppContext.instance.filesDir.resolve("configs")
        configsDir.mkdirs()

        val config = configsDir.resolve("UserConfig.json")
        if (!config.exists()) {
            return
        }

        val jsonString = config.readText()
        if (jsonString.isEmpty()) {
            return
        }

        val jsonObject = json.parseToJsonElement(jsonString).jsonObject
        val modules = jsonObject["modules"]!!.jsonObject
        _modules.forEach { module ->
            (modules[module.name] as? JsonObject)?.let {
                module.fromJson(it)
            }
        }
    }

    override fun onReceivedFromClient(packet: BedrockPacket): Boolean {
        localPlayer.onReceived(packet)

        for (module in _modules) {
            if (module.onReceived(packet)) {
                return true
            }
        }

        return false
    }

    override fun onReceivedFromServer(packet: BedrockPacket): Boolean {
        localPlayer.onReceived(packet)

        for (module in _modules) {
            if (module.onReceived(packet)) {
                return true
            }
        }

        return false
    }

}