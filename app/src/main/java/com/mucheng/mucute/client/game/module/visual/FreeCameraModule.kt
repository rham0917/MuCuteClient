package com.mucheng.mucute.client.game.module.visual

import com.mucheng.mucute.client.game.InterceptablePacket
import com.mucheng.mucute.client.game.Module
import com.mucheng.mucute.client.game.ModuleCategory
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.data.Ability
import org.cloudburstmc.protocol.bedrock.data.AbilityLayer
import org.cloudburstmc.protocol.bedrock.data.GameType
import org.cloudburstmc.protocol.bedrock.data.PlayerPermission
import org.cloudburstmc.protocol.bedrock.data.command.CommandPermission
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.SetEntityMotionPacket
import org.cloudburstmc.protocol.bedrock.packet.SetPlayerGameTypePacket
import org.cloudburstmc.protocol.bedrock.packet.TextPacket
import org.cloudburstmc.protocol.bedrock.packet.UpdateAbilitiesPacket

class FreeCameraModule : Module("free_camera", ModuleCategory.Visual) {

    private var originalPosition: Vector3f? = null

    private val enableFlyNoClipPacket = SetPlayerGameTypePacket().apply {
        gamemode = GameType.SPECTATOR.ordinal
    }

    private val disableFlyNoClipPacket = SetPlayerGameTypePacket().apply {
        gamemode = GameType.SURVIVAL.ordinal
    }

    override fun onEnabled() {
        super.onEnabled()
        if (isSessionCreated) {
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
                session.clientBound(enableFlyNoClipPacket)
            }
        }
    }

    override fun onDisabled() {
        super.onDisabled()
        if (isSessionCreated && originalPosition != null) {
            // Return to original position when disabled
            val motionPacket = SetEntityMotionPacket().apply {
                runtimeEntityId = session.localPlayer.runtimeEntityId
                motion = originalPosition
            }
            session.clientBound(motionPacket)
            originalPosition = null

            session.clientBound(disableFlyNoClipPacket)
        }
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

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        val packet = interceptablePacket.packet
        if (packet is PlayerAuthInputPacket && isEnabled) {
            interceptablePacket.intercept()
        }
    }
}
