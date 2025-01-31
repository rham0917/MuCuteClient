package com.mucheng.mucute.client.game

import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket

interface ComposedPacketHandler {

    fun onReceived(packet: BedrockPacket): Boolean

    fun onDisconnect(reason: String) {}

}