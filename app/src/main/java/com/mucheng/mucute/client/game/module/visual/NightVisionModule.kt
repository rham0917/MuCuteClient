package com.mucheng.mucute.client.game.module.visual

import com.mucheng.mucute.client.game.Module
import com.mucheng.mucute.client.game.ModuleCategory
import com.mucheng.mucute.client.game.data.Effect
import org.cloudburstmc.protocol.bedrock.data.entity.EntityFlag
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket
import org.cloudburstmc.protocol.bedrock.packet.MobEffectPacket
import org.cloudburstmc.protocol.bedrock.packet.SetEntityDataPacket

class NightVisionModule : Module("full_bright", ModuleCategory.Visual) {

    private val nightVision by boolValue("nightvision", true)
    private val removeFire by boolValue("removefire", false)

    override fun onDisabled() {
        if (nightVision && isInGame) {
            session.clientBound(MobEffectPacket().apply {
                event = MobEffectPacket.Event.REMOVE
                runtimeEntityId = localPlayer.runtimeEntityId
                effectId = Effect.NIGHT_VISION
            })
        }
    }

    override fun onReceived(packet: BedrockPacket): Boolean {
        if (packet is SetEntityDataPacket) {
            if (packet.runtimeEntityId == localPlayer.runtimeEntityId && packet.metadata?.flags != null) {
                if (removeFire && packet.metadata.flags.contains(EntityFlag.ON_FIRE)) {
                    packet.metadata.setFlag(EntityFlag.ON_FIRE, false)
                }
            }
        }

        if (isEnabled && nightVision && localPlayer.tickExists % 20 == 0L) {
            session.clientBound(MobEffectPacket().apply {
                runtimeEntityId = localPlayer.runtimeEntityId
                event = MobEffectPacket.Event.ADD
                effectId = Effect.NIGHT_VISION
                amplifier = 0
                isParticles = false
                duration = 360000
            })
        }

        return false
    }
}
