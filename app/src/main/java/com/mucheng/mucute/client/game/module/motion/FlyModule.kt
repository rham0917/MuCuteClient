package com.mucheng.mucute.client.game.module.motion

import com.mucheng.mucute.client.game.InterceptablePacket
import com.mucheng.mucute.client.game.Module
import com.mucheng.mucute.client.game.ModuleCategory
import org.cloudburstmc.protocol.bedrock.data.Ability
import org.cloudburstmc.protocol.bedrock.data.AbilityLayer
import org.cloudburstmc.protocol.bedrock.data.PlayerPermission
import org.cloudburstmc.protocol.bedrock.data.command.CommandPermission
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.RequestAbilityPacket
import org.cloudburstmc.protocol.bedrock.packet.UpdateAbilitiesPacket

class FlyModule : Module("fly", ModuleCategory.Motion) {

    private var flySpeed by floatValue("flySpeed", 0.15f, 0.1f..1.5f)

    private val enableFlyAbilitiesPacket = UpdateAbilitiesPacket().apply {
        playerPermission = PlayerPermission.OPERATOR
        commandPermission = CommandPermission.OWNER
        abilityLayers.add(AbilityLayer().apply {
            layerType = AbilityLayer.Type.BASE
            abilitiesSet.addAll(Ability.entries.toTypedArray())
            abilityValues.addAll(
                arrayOf(
                    Ability.BUILD,
                    Ability.MINE,
                    Ability.DOORS_AND_SWITCHES,
                    Ability.OPEN_CONTAINERS,
                    Ability.ATTACK_PLAYERS,
                    Ability.ATTACK_MOBS,
                    Ability.OPERATOR_COMMANDS,
                    Ability.MAY_FLY,
                    Ability.FLY_SPEED,
                    Ability.WALK_SPEED
                )
            )
            walkSpeed = 0.1f
            flySpeed = this@FlyModule.flySpeed
        })
    }

    private val disableFlyAbilitiesPacket = UpdateAbilitiesPacket().apply {
        playerPermission = PlayerPermission.OPERATOR
        commandPermission = CommandPermission.OWNER
        abilityLayers.add(AbilityLayer().apply {
            layerType = AbilityLayer.Type.BASE
            abilitiesSet.addAll(Ability.entries.toTypedArray())
            abilityValues.addAll(
                arrayOf(
                    Ability.BUILD,
                    Ability.MINE,
                    Ability.DOORS_AND_SWITCHES,
                    Ability.OPEN_CONTAINERS,
                    Ability.ATTACK_PLAYERS,
                    Ability.ATTACK_MOBS,
                    Ability.OPERATOR_COMMANDS,
                    Ability.FLY_SPEED,
                    Ability.WALK_SPEED
                )
            )
            walkSpeed = 0.1f
        })
    }

    private var canFly = false

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        val packet = interceptablePacket.packet
        if (packet is RequestAbilityPacket && packet.ability == Ability.FLYING) {
            interceptablePacket.intercept()
            return
        }

        if ((
                    canFly != isEnabled ||
                            // check if the fly speed has changed since the last update
                            MotionVarModule.lastUpdateAbilitiesPacket?.abilityLayers?.get(0)?.flySpeed != this@FlyModule.flySpeed) &&
            packet is PlayerAuthInputPacket
        ) {
            var abilitiesPacket = MotionVarModule.lastUpdateAbilitiesPacket?.clone()
            if (abilitiesPacket == null) {
                enableFlyAbilitiesPacket.uniqueEntityId = session.localPlayer.uniqueEntityId
                disableFlyAbilitiesPacket.uniqueEntityId = session.localPlayer.uniqueEntityId
                abilitiesPacket =
                    if (isEnabled) enableFlyAbilitiesPacket else disableFlyAbilitiesPacket
            }
            val abilityLayer = abilitiesPacket.abilityLayers[0]
            abilityLayer.abilityValues.addAll(
//                     these are probably added by default, but just in case...
                arrayOf(
                    Ability.FLY_SPEED,
                    Ability.WALK_SPEED
                )
            )
            if (isEnabled) {
                abilityLayer.abilityValues.add(Ability.MAY_FLY)
                abilityLayer.flySpeed = this@FlyModule.flySpeed
            } else {
                abilityLayer.abilityValues.remove(Ability.MAY_FLY)
            }

            session.clientBound(abilitiesPacket)
            canFly = isEnabled
        }
    }
}