package com.mucheng.mucute.client.game

import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket

interface ComposedPacketHandler {

    fun beforePacketBound(packet: BedrockPacket): Boolean

    fun afterPacketBound(packet: BedrockPacket) {}

    fun onDisconnect(reason: String) {}

}