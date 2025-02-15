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

class MotionFlyModule : Module("motion_fly", ModuleCategory.Motion) {

    private val verticalSpeedUp by floatValue("verticalUpSpeed", 7.0f, 1.0f..20.0f)
    private val verticalSpeedDown by floatValue("verticalDownSpeed", 7.0f, 1.0f..20.0f)
    private val motionInterval by floatValue("delay", 10f, 10f..600f)

    private val flySpeedValue by floatValue("speed", 1.0f, 0.1f..10.0f).apply {
        onChange { newValue -> 
            flyAbilitiesPacket.abilityLayers[0].flySpeed = newValue
            if (isEnabled) {
                session.clientBound(flyAbilitiesPacket)
            }
        }
    }
    
    private var lastMotionTime = 0L
    private var jitterState = false
    private var canFly = false

    private val flyAbilitiesPacket = UpdateAbilitiesPacket().apply {
        playerPermission = PlayerPermission.OPERATOR
        commandPermission = CommandPermission.OWNER
        abilityLayers.add(AbilityLayer().apply {
            layerType = AbilityLayer.Type.BASE
            abilitiesSet.addAll(Ability.entries.toTypedArray())
            abilityValues.addAll(Ability.entries)
            walkSpeed = 0.1f
            flySpeed = flySpeedValue
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

    private fun handleFlyAbilities(isEnabled: Boolean) {
        if (canFly != isEnabled) {
            flyAbilitiesPacket.uniqueEntityId = session.localPlayer.uniqueEntityId
            resetAbilitiesPacket.uniqueEntityId = session.localPlayer.uniqueEntityId
            flyAbilitiesPacket.abilityLayers[0].flySpeed = flySpeedValue
            if (isEnabled) {
                session.clientBound(flyAbilitiesPacket)
            } else {
                session.clientBound(resetAbilitiesPacket)
            }
            canFly = isEnabled
        }
    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        val packet = interceptablePacket.packet

        if (packet is PlayerAuthInputPacket) {
            handleFlyAbilities(isEnabled)
            if (isEnabled && System.currentTimeMillis() - lastMotionTime >= motionInterval) {
                val vertical = when {
                    packet.inputData.contains(PlayerAuthInputData.WANT_UP) -> verticalSpeedUp
                    packet.inputData.contains(PlayerAuthInputData.WANT_DOWN) -> -verticalSpeedDown
                    else -> 0f
                }
                val motionPacket = SetEntityMotionPacket().apply {
                    runtimeEntityId = session.local
