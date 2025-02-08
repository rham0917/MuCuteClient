package com.mucheng.mucute.client.game.module.motion

import com.mucheng.mucute.client.game.InterceptablePacket
import com.mucheng.mucute.client.game.Module
import com.mucheng.mucute.client.game.ModuleCategory
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.SetEntityMotionPacket
import kotlin.random.Random

class AntiAFKModule : Module("anti_afk", ModuleCategory.Motion) {

    private val maxMoveDistance by floatValue("repeat", 4.5f, 2.0f..10.0f)
    private val moveCooldown by intValue("delay", 300, 100..1000)

    @OptIn(DelicateCoroutinesApi::class)
    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) {
            return
        }

        val packet = interceptablePacket.packet
        if (packet is PlayerAuthInputPacket) {
            GlobalScope.launch {
                while (isEnabled) {
                    delay(moveCooldown.toLong())

                    // Generate random movement only for X and Z (no Y-axis movement)
                    val randomX = Random.nextFloat() * maxMoveDistance - (maxMoveDistance / 2)
                    val randomZ = Random.nextFloat() * maxMoveDistance - (maxMoveDistance / 2)

                    val motionPacket = SetEntityMotionPacket().apply {
                        runtimeEntityId = session.localPlayer.runtimeEntityId
                        motion = Vector3f.from(
                            randomX,  // X-axis movement
                            session.localPlayer.motionY, // Keep current Y motion
                            randomZ   // Z-axis movement
                        )
                    }

                    session.clientBound(motionPacket)
                }
            }
        }
    }
}