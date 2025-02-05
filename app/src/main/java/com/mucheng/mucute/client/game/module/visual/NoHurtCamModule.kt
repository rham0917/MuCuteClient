package com.mucheng.mucute.client.game.module.visual

import com.mucheng.mucute.client.game.Module
import com.mucheng.mucute.client.game.ModuleCategory
import org.cloudburstmc.protocol.bedrock.data.entity.EntityEventType
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket
import org.cloudburstmc.protocol.bedrock.packet.EntityEventPacket

class NoHurtCamModule : Module("no_hurt_cam", ModuleCategory.Visual) {

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
