package com.mucheng.mucute.client.game

import com.mucheng.mucute.relay.listener.MuCuteRelayPacketListener
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket

interface ComposedPacketHandler : MuCuteRelayPacketListener {

    fun beforePacketBound(packet: BedrockPacket): Boolean

    fun afterPacketBound(packet: BedrockPacket) {}

    override fun beforeClientBound(packet: BedrockPacket): Boolean {
        return beforePacketBound(packet)
    }

    override fun beforeServerBound(packet: BedrockPacket): Boolean {
        return beforePacketBound(packet)
    }

    override fun afterClientBound(packet: BedrockPacket) {
        afterPacketBound(packet)
    }

    override fun afterServerBound(packet: BedrockPacket) {
        afterPacketBound(packet)
    }

}