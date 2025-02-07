package com.mucheng.mucute.client.game.module.visual

import com.mucheng.mucute.client.game.Module
import com.mucheng.mucute.client.game.ModuleCategory
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.data.Ability
import org.cloudburstmc.protocol.bedrock.data.AbilityLayer
import org.cloudburstmc.protocol.bedrock.data.PlayerPermission
import org.cloudburstmc.protocol.bedrock.data.command.CommandPermission
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.SetEntityMotionPacket
import org.cloudburstmc.protocol.bedrock.packet.UpdateAbilitiesPacket
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.cloudburstmc.protocol.bedrock.packet.TextPacket

class FreeCamModule : Module("freecam", ModuleCategory.Visual) {

    private var originalPosition: Vector3f? = null

    private val enableFlyNoClipPacket = UpdateAbilitiesPacket().apply {
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
                    Ability.MAY_FLY,
                    Ability.FLY_SPEED,
                    Ability.WALK_SPEED,
                    Ability.NO_CLIP,
                    Ability.OPERATOR_COMMANDS
                )
            )
            walkSpeed = 0.1f
            flySpeed = 0.15f
        })
    }

    private val disableFlyNoClipPacket = UpdateAbilitiesPacket().apply {
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

    private var isFlyNoClipEnabled = false

    @OptIn(DelicateCoroutinesApi::class)
    override fun onEnabled() {
        if (isSessionCreated) {
            sendToggleMessage(true)

            GlobalScope.launch {
                for (i in 5 downTo 1) {
                    val countdownMessage = "§l§b[MuCute] §r§7FreeCam will enable in §e$i §7seconds"
                    sendCountdownMessage(countdownMessage)
                    delay(1000)
                }

                // Store original position when enabled after countdown
                originalPosition = Vector3f.from(
                    session.localPlayer.posX,
                    session.localPlayer.posY,
                    session.localPlayer.posZ
                )
                enableFlyNoClipPacket.uniqueEntityId = session.localPlayer.uniqueEntityId
                session.clientBound(enableFlyNoClipPacket)
                isFlyNoClipEnabled = true
            }
        }
    }

    override fun onDisabled() {
        if (isSessionCreated && originalPosition != null) {
            sendToggleMessage(false)

            // Return to original position when disabled
            val motionPacket = SetEntityMotionPacket().apply {
                runtimeEntityId = session.localPlayer.runtimeEntityId
                motion = originalPosition
            }
            session.clientBound(motionPacket)
            originalPosition = null

            disableFlyNoClipPacket.uniqueEntityId = session.localPlayer.uniqueEntityId
            session.clientBound(disableFlyNoClipPacket)
            isFlyNoClipEnabled = false
        }
    }

    private fun sendToggleMessage(enabled: Boolean) {
        val status = if (enabled) "§aEnabled" else "§cDisabled"
        val message = "§l§b[MuCute] §r§7FreeCam §8» $status"

        val textPacket = TextPacket().apply {
            type = TextPacket.Type.RAW
            isNeedsTranslation = false
            this.message = message
            xuid = ""
            sourceName = ""
        }

        session.clientBound(textPacket)
    }

    private fun sendCountdownMessage(message: String) {
        val textPacket = TextPacket().apply {
            type = TextPacket.Type.RAW
            isNeedsTranslation = false
            this.message = message
            xuid = ""
            sourceName = ""
        }

        session.clientBound(textPacket)
    }

    override fun beforePacketBound(packet: BedrockPacket): Boolean {
        return packet is PlayerAuthInputPacket && isEnabled
    }
}
