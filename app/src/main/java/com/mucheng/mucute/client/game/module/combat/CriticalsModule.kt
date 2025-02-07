package com.mucheng.mucute.client.game.module.combat

import com.mucheng.mucute.client.game.Module
import com.mucheng.mucute.client.game.ModuleCategory
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket

class CriticalsModule : Module("critic", ModuleCategory.Combat) {

    override fun beforePacketBound(packet: BedrockPacket): Boolean {

        if (!isEnabled){

            return false
        }


      else if (packet is MovePlayerPacket && isEnabled){

            packet.position = packet.position.add(0.2,0.2,0.2)
            packet.isOnGround = false
  }

        return false


    }

}