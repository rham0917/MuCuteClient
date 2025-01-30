package com.mucheng.mucute.client.game.module.player

import com.mucheng.mucute.client.game.Module
import com.mucheng.mucute.client.game.ModuleCategory
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket
import org.cloudburstmc.protocol.bedrock.packet.SetEntityMotionPacket
import org.cloudburstmc.math.vector.Vector3f

class AntiKnockbackModule : Module("anti_knockback", ModuleCategory.Player) {

    override fun onReceived(packet: BedrockPacket): Boolean {
        if (packet is SetEntityMotionPacket) {
            // Reset horizontal motion to prevent knockback
            packet.motion = Vector3f.from(
                0f,  // Reset horizontal motion
                packet.motion.y,  // Maintain vertical motion
                0f   // Reset horizontal motion
            )
            return true // Indicate that the packet was handled
        }
        return false // Indicate that the packet was not handled
    }
} 