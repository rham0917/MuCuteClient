package com.mucheng.mucute.client.game.module.motion

import com.mucheng.mucute.client.game.Module
import com.mucheng.mucute.client.game.ModuleCategory
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.SetEntityMotionPacket
import org.cloudburstmc.math.vector.Vector3f
import kotlin.random.Random
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RandomMoveModule : Module("random_move", ModuleCategory.Motion) {

    private val maxMoveDistance = 4.5f // Max movement per tick
    private val moveCooldown = 200L // Delay between movements (in ms)

    override fun onReceived(packet: BedrockPacket): Boolean {
        if (isEnabled && packet is PlayerAuthInputPacket) {
            GlobalScope.launch {
                while (isEnabled) {
                    delay(moveCooldown)

                    // Generate random movement only for X and Z (no Y-axis movement)
                    val randomX = Random.nextFloat() * maxMoveDistance - (maxMoveDistance / 2)
                    val randomZ = Random.nextFloat() * maxMoveDistance - (maxMoveDistance / 2)

                    val motionPacket = SetEntityMotionPacket().apply {
                        runtimeEntityId = localPlayer.runtimeEntityId
                        motion = Vector3f.from(
                            randomX,  // X-axis movement
                            localPlayer.motionY, // Keep current Y motion
                            randomZ   // Z-axis movement
                        )
                    }

                    session.clientBound(motionPacket)
                }
            }
        }
        return false
    }
}
