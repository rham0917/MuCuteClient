package com.mucheng.mucute.client.game.module.motion

import com.mucheng.mucute.client.game.InterceptablePacket
import com.mucheng.mucute.client.game.Module
import com.mucheng.mucute.client.game.ModuleCategory
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.SetEntityMotionPacket
import kotlin.concurrent.timer
import kotlin.math.cos
import kotlin.math.sin

class AutoWalkModule : Module("auto_walk", ModuleCategory.Motion) {

    private val speed = 0.5f  // Walking speed
    private var lastJumpTime = System.currentTimeMillis()  // Track time for periodic jumps
    private var isJumping = false  // Flag to track if the player is mid-jump
    private var disableYAxis = false  // Flag to disable Y-axis motion and allow gravity

    init {
        // Start a timer to periodically apply the jump effect every 2 seconds if the module is enabled
        timer(period = 2000) {
            if (isEnabled) {  // Only apply jump if the module is enabled
                // Check if it's time to apply a jump every 2 seconds
                if (System.currentTimeMillis() - lastJumpTime >= 2000) {
                    lastJumpTime = System.currentTimeMillis()  // Reset the jump timer
                    applyJump()  // Apply a slight jump
                }
            }
        }
    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) {
            return
        }

        val packet = interceptablePacket.packet
        if (packet is PlayerAuthInputPacket) {  // Only execute if the module is enabled
            // Call the function to control X and Z axis if module is enabled
            controlXZMovement(packet)

            // Call the function to control Y axis (jump) if module is enabled
            if (!disableYAxis) {
                controlYMovement()  // Apply upward motion if needed
            }
        }
    }

    // Function to control X and Z movement based on the player's look direction
    private fun controlXZMovement(packet: PlayerAuthInputPacket) {
        // Convert angles to radians (use Float for the calculations)
        val yaw = Math.toRadians(packet.rotation.y.toDouble())
            .toFloat()  // Horizontal direction (left/right)
        val pitch =
            Math.toRadians(packet.rotation.x.toDouble()).toFloat()  // Vertical direction (up/down)

        // Calculate direction vector based on the player's look direction
        val motionX = -sin(yaw) * cos(pitch) * speed  // Movement along X (forward/backward)
        val motionZ = cos(yaw) * cos(pitch) * speed  // Movement along Z (forward/backward)

        // Send the updated packet for movement input
        val motionPacket = SetEntityMotionPacket().apply {
            runtimeEntityId = session.localPlayer.runtimeEntityId
            motion = Vector3f.from(
                motionX.toFloat(),
                0f,  // We are not manually controlling the Y-axis yet
                motionZ.toFloat()
            )
        }
        session.clientBound(motionPacket)
    }

    // Function to control Y axis (jumping)
    private fun controlYMovement() {
        if (!isJumping) {  // Ensure we're not already jumping
            isJumping = true  // Mark as jumping

            // Apply a slight upward motion for the jump
            val motionPacket = SetEntityMotionPacket().apply {
                runtimeEntityId = session.localPlayer.runtimeEntityId
                motion = Vector3f.from(0f, 1.5f, 0f)  // 0.5f is the jump height (adjust as needed)
            }
            session.clientBound(motionPacket)

            // Disable Y-axis control for a brief period and allow gravity to take over
            disableYAxis = true

            // Reset the disableYAxis flag after 1 second (time to fall back down)
            timer(period = 1000, initialDelay = 1000) {
                disableYAxis = false  // Enable Y-axis control again after the jump
            }
        }
    }

    // Apply a slight jump every 2 seconds
    private fun applyJump() {
        if (!isSessionCreated) {
            return
        }

        if (!isJumping) {  // Ensure we're not already jumping
            isJumping = true  // Mark as jumping

            // Apply a slight upward motion for the jump
            val motionPacket = SetEntityMotionPacket().apply {
                runtimeEntityId = session.localPlayer.runtimeEntityId
                motion = Vector3f.from(0f, 1.5f, 0f)  // 0.5f is the jump height (adjust as needed)
            }
            session.clientBound(motionPacket)

            // Disable Y-axis control for a brief period and allow gravity to take over
            disableYAxis = true

            // Reset the disableYAxis flag after 1 second (time to fall back down)
            timer(period = 1000, initialDelay = 1000) {
                disableYAxis = false  // Enable Y-axis control again after the jump
            }
        }
    }
}