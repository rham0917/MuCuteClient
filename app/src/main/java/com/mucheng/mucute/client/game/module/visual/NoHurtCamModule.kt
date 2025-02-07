package com.mucheng.mucute.client.game.module.visual

import com.mucheng.mucute.client.game.Module
import com.mucheng.mucute.client.game.ModuleCategory
import org.cloudburstmc.protocol.bedrock.data.entity.EntityEventType
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket
import org.cloudburstmc.protocol.bedrock.packet.EntityEventPacket
import org.cloudburstmc.protocol.bedrock.packet.TextPacket

class NoHurtCamModule : Module("no_hurt_cam", ModuleCategory.Visual) {

    override fun onEnabled() {
        if (isSessionCreated) {
            sendToggleMessage(true)
        }
    }

    override fun onDisabled() {
        if (isSessionCreated) {
            sendToggleMessage(false)
        }
    }

    private fun sendToggleMessage(enabled: Boolean) {
        val status = if (enabled) "§aEnabled" else "§cDisabled"
        val message = "§l§b[MuCute] §r§7No Hurt Cam §8» $status"

        val textPacket = TextPacket().apply {
            type = TextPacket.Type.RAW
            isNeedsTranslation = false
            this.message = message
            xuid = ""
            sourceName = ""
        }

        session.clientBound(textPacket)
    }

    override fun beforePacketBound(packet: BedrockPacket): Boolean {
        if (packet is EntityEventPacket && isEnabled) {
            if (packet.runtimeEntityId == session.localPlayer.runtimeEntityId
                && packet.type == EntityEventType.HURT
            ) {
                return true
            }
        }
        return false
    }
}