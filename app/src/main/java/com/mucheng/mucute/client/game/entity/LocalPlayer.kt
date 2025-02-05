package com.mucheng.mucute.client.game.entity

import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.StartGamePacket
import java.util.UUID

@Suppress("MemberVisibilityCanBePrivate")
class LocalPlayer : Player(0L, 0L, UUID.randomUUID(), "") {

    override var runtimeEntityId: Long = 0L
        private set

    override var uniqueEntityId: Long = 0L
        private set

    override var uuid: UUID = UUID.randomUUID()
        private set

    override fun beforePacketBound(packet: BedrockPacket): Boolean {
        super.beforePacketBound(packet)
        if (packet is StartGamePacket) {
            runtimeEntityId = packet.runtimeEntityId
            uniqueEntityId = packet.uniqueEntityId
        }
         if (packet is PlayerAuthInputPacket){
            move(packet.position)
            rotate (packet.rotation)
            tickExists = packet.tick
        }
        return false
    }


    override fun onDisconnect(reason: String) {
        super.onDisconnect(reason)
        reset()
    }

}
