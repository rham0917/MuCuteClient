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

    private val speedValue by floatValue("speed", 1.4f, 0.1f..3.0f)

    override fun beforePacketBound(packet: BedrockPacket): Boolean {
        if (packet is PlayerAuthInputPacket && isEnabled) {
            if (packet.motion.length() > 0) {
                val angle = Math.toRadians(packet.rotation.y.toDouble())
                val motionPacket = SetEntityMotionPacket().apply {
                    runtimeEntityId = session.localPlayer.runtimeEntityId
                    motion = Vector3f.from(
                        -sin(angle).toFloat() * speedValue,
                        session.localPlayer.motionY,
                        cos(angle).toFloat() * speedValue
                    )
                }
                session.clientBound(motionPacket)
            }
        }
        return false
    }
}
