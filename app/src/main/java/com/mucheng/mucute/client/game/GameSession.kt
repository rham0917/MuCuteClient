package com.mucheng.mucute.client.game

import com.mucheng.mucute.client.game.entity.LocalPlayer
import com.mucheng.mucute.client.game.world.Level
import com.mucheng.mucute.relay.MuCuteRelaySession
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket
import org.cloudburstmc.protocol.bedrock.packet.TextPacket

@Suppress("MemberVisibilityCanBePrivate")
class GameSession(val muCuteRelaySession: MuCuteRelaySession) : ComposedPacketHandler {

    val localPlayer = LocalPlayer(this)

    val level = Level(this)

    fun clientBound(packet: BedrockPacket) {
        muCuteRelaySession.clientBound(packet)
    }

    fun serverBound(packet: BedrockPacket) {
        muCuteRelaySession.serverBound(packet)
    }

    override fun beforePacketBound(packet: BedrockPacket): Boolean {
        localPlayer.onPacketBound(packet)
        level.onPacketBound(packet)

        for (module in ModuleManager.modules) {
            if (module.beforePacketBound(packet)) {
                return true
            }
        }
        return false
    }

    override fun afterPacketBound(packet: BedrockPacket) {
        for (module in ModuleManager.modules) {
            module.afterPacketBound(packet)
        }
    }

    override fun onDisconnect(reason: String) {
        localPlayer.onDisconnect()
        level.onDisconnect()

        for (module in ModuleManager.modules) {
            module.onDisconnect(reason)
        }
    }

    fun displayClientMessage(message: String, type: TextPacket.Type = TextPacket.Type.TIP) {
        val textPacket = TextPacket()
        textPacket.type = type
        textPacket.isNeedsTranslation = false
        textPacket.sourceName = ""
        textPacket.message = message
        textPacket.xuid = ""
        textPacket.platformChatId = ""
        textPacket.filteredMessage = ""
        clientBound(textPacket)
    }

}