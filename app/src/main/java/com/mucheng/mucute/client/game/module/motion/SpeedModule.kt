package com.mucheng.mucute.client.game.module.motion


import com.mucheng.mucute.client.game.InterceptablePacket
import com.mucheng.mucute.client.game.Module
import com.mucheng.mucute.client.game.ModuleCategory
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.data.PlayerAuthInputData
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.SetEntityMotionPacket

class SpeedModule : Module("speed", ModuleCategory.Motion) {

    private var speedValue by floatValue("speed", 1.3f, 0.1f..5f)


    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet

        if (packet is PlayerAuthInputPacket) {
            // Check if motion is greater than 0.0 and inputData contains VERTICAL_COLLISION
            if (packet.motion.length() > 0.0 && packet.inputData.contains(PlayerAuthInputData.VERTICAL_COLLISION)) {

                // Create and send the SetEntityMotionPacket with current player's motion
                val motionPacket = SetEntityMotionPacket().apply {
                    runtimeEntityId = session.localPlayer.runtimeEntityId
                    motion = Vector3f.from(
                        session.localPlayer.motionX.toDouble() * speedValue,
                        session.localPlayer.motionY.toDouble(),
                        session.localPlayer.motionZ.toDouble() * speedValue
                    )
                }

                // Send the motion update packet
                session.clientBound(motionPacket)
            }
        }
    }
}
