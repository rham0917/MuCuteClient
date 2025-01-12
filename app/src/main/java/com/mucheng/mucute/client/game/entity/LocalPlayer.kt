package com.mucheng.mucute.client.game.entity

import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket
import org.cloudburstmc.protocol.bedrock.packet.LoginPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.StartGamePacket
import org.cloudburstmc.protocol.bedrock.util.EncryptionUtils
import java.util.UUID

@Suppress("MemberVisibilityCanBePrivate")
class LocalPlayer : Player(0L, 0L, UUID.randomUUID(), "") {

    override var runtimeEntityId: Long = 0L
        private set

    override var uniqueEntityId: Long = 0L
        private set

    override var uuid: UUID = UUID.randomUUID()
        private set

    override fun onReceived(packet: BedrockPacket): Boolean {
        super.onReceived(packet)
        if (packet is StartGamePacket) {
            runtimeEntityId = packet.runtimeEntityId
            uniqueEntityId = packet.uniqueEntityId
        }
        return false
    }

    override fun onDisconnect(reason: String) {
        super.onDisconnect(reason)
        reset()
    }

}