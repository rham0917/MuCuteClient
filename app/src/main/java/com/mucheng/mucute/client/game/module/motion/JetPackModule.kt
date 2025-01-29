package com.mucheng.mucute.client.game.module.motion

import com.mucheng.mucute.client.game.Module
import com.mucheng.mucute.client.game.ModuleCategory
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.packet.SetEntityMotionPacket
import kotlin.math.cos
import kotlin.math.sin

class JetPackModule : Module("jetpack", ModuleCategory.Motion) {
    
    private val speed = 0.6f  // Movement speed

    override fun onReceived(packet: BedrockPacket): Boolean {
        if (packet is PlayerAuthInputPacket && isEnabled) {
            // Convert angles to radians
            val yaw = Math.toRadians(packet.rotation.y.toDouble())
            val pitch = Math.toRadians(packet.rotation.x.toDouble())
            
            // Calculate direction vector based on where player is looking
            val motionX = -sin(yaw) * cos(pitch) * speed
            val motionY = -sin(pitch) * speed
            val motionZ = cos(yaw) * cos(pitch) * speed
            
            // Apply motion in look direction
            val motionPacket = SetEntityMotionPacket().apply {
                runtimeEntityId = localPlayer.runtimeEntityId
                motion = Vector3f.from(
                    motionX.toFloat(),
                    motionY.toFloat(),
                    motionZ.toFloat()
                )
            }
            session.clientBound(motionPacket)
        }
        return false
    }
}