package com.mucheng.mucute.client.game.module.motion

import com.mucheng.mucute.client.game.Module
import com.mucheng.mucute.client.game.ModuleCategory
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.SetEntityMotionPacket
import kotlin.math.cos
import kotlin.math.sin

class SpeedModule : Module("speed", ModuleCategory.Motion) {

    private val speedValue = 1.4f  // Base speed value

    override fun onReceived(packet: BedrockPacket): Boolean {
        if (packet is PlayerAuthInputPacket && isEnabled) {
            if (packet.motion.length() > 0) {
                val angle = Math.toRadians(packet.rotation.y.toDouble())
                val motionPacket = SetEntityMotionPacket().apply {
                    runtimeEntityId = localPlayer.runtimeEntityId
                    motion = Vector3f.from(
                        -sin(angle).toFloat() * speedValue,
                        localPlayer.motionY,
                        cos(angle).toFloat() * speedValue
                    )
                }
                session.clientBound(motionPacket)
            }
        }
        return false
    }
}
