package com.mucheng.mucute.client.game.module.combat

import com.mucheng.mucute.client.game.Module
import com.mucheng.mucute.client.game.ModuleCategory
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket

class KillauraModule : Module("killaura", ModuleCategory.Combat) {

    override fun beforePacketBound(packet: BedrockPacket): Boolean {

        return false
    }

}