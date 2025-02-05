package com.mucheng.mucute.client.game.module.motion

import com.mucheng.mucute.client.game.Module
import com.mucheng.mucute.client.game.ModuleCategory
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.data.Ability
import org.cloudburstmc.protocol.bedrock.data.AbilityLayer
import org.cloudburstmc.protocol.bedrock.data.PlayerAuthInputData
import org.cloudburstmc.protocol.bedrock.data.PlayerPermission
import org.cloudburstmc.protocol.bedrock.data.command.CommandPermission
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.SetEntityMotionPacket
import org.cloudburstmc.protocol.bedrock.packet.UpdateAbilitiesPacket

class MotionFlyModule : Module("motion_fly", ModuleCategory.Motion) {

    private val verticalSpeedUp by floatValue("Vertical Speed (Up)", 7.0f, 1.0f..20.0f)
    private val verticalSpeedDown by floatValue("Vertical Speed (Down)", 7.0f, 1.0f..20.0f)
    private var motionInterval by floatValue("Hop Delay", 100.0f, 1.0f..600.0f)
    private var jitterState = false

    private val flyAbilitiesPacket = UpdateAbilitiesPacket().apply {
        playerPermission = PlayerPermission.OPERATOR
        commandPermission = CommandPermission.OWNER
        abilityLayers.add(AbilityLayer().apply {
            layerType = AbilityLayer.Type.BASE
            abilitiesSet.addAll(Ability.entries.toTypedArray())
            abilityValues.addAll(Ability.entries)
            abilityValues.add(Ability.MAY_FLY)
            walkSpeed = 0.1f
            flySpeed = 2.19f
        })
    }

    private val resetAbilitiesPacket = UpdateAbilitiesPacket().apply {
        playerPermission = PlayerPermission.OPERATOR
        commandPermission = CommandPermission.OWNER
        abilityLayers.add(AbilityLayer().apply {
            layerType = AbilityLayer.Type.BASE
            abilitiesSet.addAll(Ability.entries.toTypedArray())
            abilityValues.removeAll { it == Ability.MAY_FLY || it == Ability.NO_CLIP }
            walkSpeed = 0.1f
            flySpeed = 0f
        })
    }

    private fun updateFlyAbilities() {
        flyAbilitiesPacket.uniqueEntityId = session.localPlayer.uniqueEntityId
        resetAbilitiesPacket.uniqueEntityId = session.localPlayer.uniqueEntityId

        if (isEnabled) {
            session.clientBound(flyAbilitiesPacket)
        } else {
            session.clientBound(resetAbilitiesPacket)
        }
    }

    override fun beforePacketBound(packet: BedrockPacket): Boolean {
        if (packet is PlayerAuthInputPacket) {
            updateFlyAbilities()

            if (isEnabled) {
                val vertical = when {
                    packet.inputData.contains(PlayerAuthInputData.WANT_UP) -> {
                        motionInterval = 1f
                        verticalSpeedUp
                    }
                    packet.inputData.contains(PlayerAuthInputData.WANT_DOWN) -> {
                        motionInterval = 1f
                        -verticalSpeedDown
                    }
                    else -> {
                        motionInterval = 100f
                        0f
                    }
                }

                val motionPacket = SetEntityMotionPacket().apply {
                    runtimeEntityId = session.localPlayer.runtimeEntityId
                    motion = Vector3f.from(0f, vertical + (if (jitterState) 0.1f else -0.1f), 0f)
                }
                session.clientBound(motionPacket)

                jitterState = !jitterState
            }
        }
        return false
    }
}