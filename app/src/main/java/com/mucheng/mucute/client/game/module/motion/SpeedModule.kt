package com.mucheng.mucute.client.game.module.motion


import com.mucheng.mucute.client.game.InterceptablePacket
import com.mucheng.mucute.client.game.Module
import com.mucheng.mucute.client.game.ModuleCategory
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.data.Ability
import org.cloudburstmc.protocol.bedrock.data.AbilityLayer
import org.cloudburstmc.protocol.bedrock.data.PlayerAuthInputData
import org.cloudburstmc.protocol.bedrock.data.PlayerPermission
import org.cloudburstmc.protocol.bedrock.data.command.CommandPermission
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.SetEntityMotionPacket
import org.cloudburstmc.protocol.bedrock.packet.UpdateAbilitiesPacket

class SpeedModule : Module("speed", ModuleCategory.Motion) {

    private var speedValue by floatValue("speed", 1.3f, 0.1f..5f)

    override fun onDisabled() {
        val abilities = MotionVarModule.LastUpdateAbilitiesPacket.value?.clone() ?: return

        abilities.abilityLayers[0].walkSpeed = 0.1f
        session.clientBound(abilities)
        super.onDisabled()
    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet

        if (packet is PlayerAuthInputPacket) {
// add option for speeds. ( Vanilla <-> Motion )
// DEPRECATED: This code is no longer used as it causes undesired behavior where it feels like the player is slipping.
            // ( multiplication of motion causes velocity to decrease slower than intended )

            // Check if motion is greater than 0.0 and inputData contains VERTICAL_COLLISION
//            if (packet.motion.length() > 0.0 && packet.inputData.contains(PlayerAuthInputData.VERTICAL_COLLISION)) {
//
//                // Create and send the SetEntityMotionPacket with current player's motion
//                val motionPacket = SetEntityMotionPacket().apply {
//                    runtimeEntityId = session.localPlayer.runtimeEntityId
//                    motion = Vector3f.from(
//                        session.localPlayer.motionX.toDouble() * speedValue,
//                        session.localPlayer.motionY.toDouble(),
//                        session.localPlayer.motionZ.toDouble() * speedValue
//                    )
//                }
//
//                // Send the motion update packet
//                session.clientBound(motionPacket)
//            }
//             This code is used instead to fix the velocity decrease issue as we're setting the speed as expected by the client.
            val abilities = MotionVarModule.LastUpdateAbilitiesPacket.value?.clone() ?: return
            if (speedValue == abilities.abilityLayers[0].walkSpeed) return

            abilities.abilityLayers[0].walkSpeed = speedValue
            session.clientBound(abilities)
        }
    }
}
