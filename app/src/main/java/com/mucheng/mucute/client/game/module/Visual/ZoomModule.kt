package com.mucheng.mucute.client.game.module.visual

import com.mucheng.mucute.client.game.Module
import com.mucheng.mucute.client.game.ModuleCategory
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.UpdateAbilitiesPacket
import org.cloudburstmc.protocol.bedrock.data.PlayerPermission
import org.cloudburstmc.protocol.bedrock.data.command.CommandPermission
import org.cloudburstmc.protocol.bedrock.data.Ability
import org.cloudburstmc.protocol.bedrock.data.AbilityLayer

class ZoomModule : Module("zoom", ModuleCategory.Visual) {

    // Packet to enable zooming
    private val enableZoomPacket = UpdateAbilitiesPacket().apply {
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
                    Ability.OPERATOR_COMMANDS
                )
            )
            walkSpeed = 7.0f
        })
    }

    // Packet to disable zooming
    private val disableZoomPacket = UpdateAbilitiesPacket().apply {
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
                    Ability.OPERATOR_COMMANDS

                )
            )

            walkSpeed = 0.1f
        })
    }

    private var isZoomEnabled = false

    private var lastZoomPacketTime = System.currentTimeMillis()

    override fun onReceived(packet: BedrockPacket): Boolean {
        if (packet is PlayerAuthInputPacket) {
            val currentTime = System.currentTimeMillis()

            // Prevent sending too many packets in a short amount of time
            if (currentTime - lastZoomPacketTime < 200) {
                return false
            }

            if (!isZoomEnabled && isEnabled) {
                // Enable zoom
                enableZoomPacket.uniqueEntityId = localPlayer.uniqueEntityId
                session.clientBound(enableZoomPacket)
                isZoomEnabled = true
            } else if (isZoomEnabled && !isEnabled) {
                // Disable zoom
                disableZoomPacket.uniqueEntityId = localPlayer.uniqueEntityId
                session.clientBound(disableZoomPacket)
                isZoomEnabled = false
            }

            lastZoomPacketTime = currentTime
        }
        return false
    }

}
