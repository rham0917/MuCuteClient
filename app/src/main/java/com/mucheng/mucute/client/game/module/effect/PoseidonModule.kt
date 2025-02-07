package com.mucheng.mucute.client.game.module.effect

import com.mucheng.mucute.client.game.Module
import com.mucheng.mucute.client.game.ModuleCategory
import com.mucheng.mucute.client.game.data.Effect
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.data.PlayerAuthInputData
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket
import org.cloudburstmc.protocol.bedrock.packet.MobEffectPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.SetEntityMotionPacket
import org.cloudburstmc.protocol.bedrock.packet.TextPacket
import kotlin.math.cos
import kotlin.math.sin

class PoseidonModule : Module("poseidon", ModuleCategory.Effect) {

    private val speedMultiplier = 1.5f  // How much faster to move in water

    override fun onEnabled() {

        if (isSessionCreated) {

            sendToggleMessage(true)

        }

    }

    override fun onDisabled() {
        if (isSessionCreated) {
            // Remove effects
            session.clientBound(MobEffectPacket().apply {
                event = MobEffectPacket.Event.REMOVE
                runtimeEntityId = session.localPlayer.runtimeEntityId
                effectId = Effect.NIGHT_VISION
            })
            session.clientBound(MobEffectPacket().apply {
                event = MobEffectPacket.Event.REMOVE
                runtimeEntityId = session.localPlayer.runtimeEntityId
                effectId = Effect.WATER_BREATHING
            })
            sendToggleMessage(false)
        }
    }

    private fun sendToggleMessage(enabled: Boolean) {

        val status = if (enabled) "§aEnabled" else "§cDisabled"

        val message = "§l§b[MuCute] §r§7Poseidon §8» $status"


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
        if (packet is PlayerAuthInputPacket && isEnabled) {
            // Check for water or sinking
            if (packet.inputData.contains(PlayerAuthInputData.START_SWIMMING) ||
                packet.inputData.contains(PlayerAuthInputData.AUTO_JUMPING_IN_WATER) ||
                session.localPlayer.motionY < 0
            ) {

                // Calculate speed based on look direction
                val yaw = Math.toRadians(packet.rotation.y.toDouble())
                val pitch = Math.toRadians(packet.rotation.x.toDouble())

                // Calculate motion vector
                val motionX = -sin(yaw) * cos(pitch) * speedMultiplier
                val motionZ = cos(yaw) * cos(pitch) * speedMultiplier

                // Set motion with speed boost and anti-sink
                val motionPacket = SetEntityMotionPacket().apply {
                    runtimeEntityId = session.localPlayer.runtimeEntityId
                    motion = Vector3f.from(
                        motionX.toFloat(),
                        0.05f,  // Anti-sink
                        motionZ.toFloat()
                    )
                }
                session.clientBound(motionPacket)

                // Remove swimming states
                packet.inputData.remove(PlayerAuthInputData.START_SWIMMING)
                packet.inputData.remove(PlayerAuthInputData.AUTO_JUMPING_IN_WATER)
            }
        }

        // Apply effects every second
        if (isEnabled && session.localPlayer.tickExists % 20 == 0L) {
            // Night vision
            session.clientBound(MobEffectPacket().apply {
                runtimeEntityId = session.localPlayer.runtimeEntityId
                event = MobEffectPacket.Event.ADD
                effectId = Effect.NIGHT_VISION
                amplifier = 0
                isParticles = false
                duration = 360000
            })

            // Water breathing
            session.clientBound(MobEffectPacket().apply {
                runtimeEntityId = session.localPlayer.runtimeEntityId
                event = MobEffectPacket.Event.ADD
                effectId = Effect.WATER_BREATHING
                amplifier = 0
                isParticles = false
                duration = 360000
            })
        }
        return false
    }


} 