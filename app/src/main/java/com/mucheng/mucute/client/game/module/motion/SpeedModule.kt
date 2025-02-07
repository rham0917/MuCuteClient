package com.mucheng.mucute.client.game.module.motion

import com.mucheng.mucute.client.game.Module
import com.mucheng.mucute.client.game.ModuleCategory
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.SetEntityMotionPacket
import org.cloudburstmc.protocol.bedrock.packet.TextPacket
import kotlin.math.cos
import kotlin.math.sin

class SpeedModule : Module("speed", ModuleCategory.Motion) {

    private val speedValue by floatValue("speed", 1.4f, 0.1f..3.0f)

    override fun onEnabled() {
        if (isSessionCreated) {
            sendToggleMessage(true)
        }
    }

    override fun onDisabled() {
        if (isSessionCreated) {
            sendToggleMessage(false)
        }
    }

    private fun sendToggleMessage(enabled: Boolean) {
        val status = if (enabled) "§aEnabled" else "§cDisabled"
        val message = "§l§b[MuCute] §r§7Speed §8» $status"

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