package com.mucheng.mucute.client.game.module.motion

import com.mucheng.mucute.client.game.Module
import com.mucheng.mucute.client.game.ModuleCategory
import com.mucheng.mucute.client.game.data.Effect
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket
import org.cloudburstmc.protocol.bedrock.packet.MobEffectPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket

class LevitationModule : Module("levitation", ModuleCategory.Motion) {

    override fun onReceived(packet: BedrockPacket): Boolean {
        if (packet is PlayerAuthInputPacket && isEnabled) {
            if (localPlayer.tickExists % 20 == 0L) {
                session.clientBound(MobEffectPacket().apply {
                    runtimeEntityId = localPlayer.runtimeEntityId
                    event = MobEffectPacket.Event.ADD
                    effectId = Effect.LEVITATION
                    amplifier = 5
                    isParticles = false
                    duration = 21 * 20
                })
            }
        }
        return false
    }

    override fun onDisabled() {
        if (isInGame) {
            session.clientBound(MobEffectPacket().apply {
                runtimeEntityId = localPlayer.runtimeEntityId
                event = MobEffectPacket.Event.REMOVE
                effectId = Effect.LEVITATION
            })
        }
    }
}
