package com.mucheng.mucute.client.game.module.combat

import com.mucheng.mucute.client.game.InterceptablePacket
import com.mucheng.mucute.client.game.Module
import com.mucheng.mucute.client.game.ModuleCategory
import com.mucheng.mucute.client.game.entity.Entity
import com.mucheng.mucute.client.game.entity.EntityUnknown
import com.mucheng.mucute.client.game.entity.LocalPlayer
import com.mucheng.mucute.client.game.entity.MobList
import com.mucheng.mucute.client.game.entity.Player
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import kotlin.math.cos
import kotlin.math.sin

class KillauraModule : Module("killaura", ModuleCategory.Combat) {

    private var playersOnly by boolValue("players_only", true)
    private var mobsOnly by boolValue("mobs_only", true)
    private var tpAuraEnabled by boolValue("tp_aura", false) // TP Aura toggle
    private var strafe by boolValue("strafe", false)
    private var teleportBehind by boolValue("teleport_behind", false) // Default to true
    private var rangeValue by floatValue("range", 3.7f, 2f..7f)
    private var attackInterval by intValue("delay", 5, 1..20)
    private var cpsValue by intValue("cps", 5, 1..20)
    private var packets by intValue("packets", 1, 1..10)
    private var tpSpeed by intValue("tp_speed", 500, 100..2000)

    private var distanceToKeep by floatValue("keep_distance", 2.0f, 1f..5f)
    private var strafeAngle = 0.0f
    private val strafeSpeed by floatValue("strafe_speed", 1.0f, 0.1f..2.0f)
    private val strafeRadius by floatValue("strafe_radius", 1.0f, 0.1f..5.0f)
    private var lastAttackTime = 0L
    private var tpCooldown = 0L // Cooldown for teleportation to prevent spamming

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet
        if (packet is PlayerAuthInputPacket) {
            val currentTime = System.currentTimeMillis()
            val minAttackDelay = 1000L / cpsValue

            if (packet.tick % attackInterval == 0L && (currentTime - lastAttackTime) >= minAttackDelay) {
                val closestEntities = searchForClosestEntities()
                if (closestEntities.isEmpty()) return

                closestEntities.forEach { entity ->
                    // Handle teleportation once when TP Aura is enabled
                    if (tpAuraEnabled && (currentTime - tpCooldown) >= tpSpeed) { // Add a cooldown for teleportation
                        teleportTo(entity, distanceToKeep)
                        tpCooldown = currentTime // Update teleportation cooldown
                    }

                    repeat(packets) {
                        session.localPlayer.attack(entity) // Attack the entity multiple times
                    }
                    if (strafe) {
                        strafeAroundTarget(entity)
                    }
                    lastAttackTime = currentTime
                }
            }
        }
    }

    private fun strafeAroundTarget(entity: Entity) {
        val targetPos = entity.vec3Position

        // Calculate the new strafe position
        strafeAngle += strafeSpeed
        if (strafeAngle >= 360.0) {
            strafeAngle -= 360.0f
        }

        // Calculate the circular motion offset
        val offsetX = strafeRadius * cos(strafeAngle)
        val offsetZ = strafeRadius * sin(strafeAngle)

        // Adjust the player's position using MovePlayerPacket
        val newPosition = targetPos.add(offsetX.toFloat(), 0f, offsetZ.toFloat())

        val movePlayerPacket = MovePlayerPacket().apply {
            runtimeEntityId = session.localPlayer.runtimeEntityId
            position = newPosition
            rotation = Vector3f.from(0f, 0f, 0f) // Keep the current rotation (optional)
            mode = MovePlayerPacket.Mode.NORMAL
            isOnGround = true
            ridingRuntimeEntityId = 0
            tick = session.localPlayer.tickExists
        }

        session.clientBound(movePlayerPacket)
    }

    private fun teleportTo(entity: Entity, distance: Float) {
        val targetPosition = entity.vec3Position
        val playerPosition = session.localPlayer.vec3Position

        val newPosition = if (teleportBehind) {
            val targetYaw = Math.toRadians(entity.vec3Rotation.y.toDouble()).toFloat()

            // Corrected direction calculation for behind
            val direction = Vector3f.from(
                sin(targetYaw),  // Fixed: Removed negative sign to get correct direction
                0f,
                -cos(targetYaw)
            )

            val length = direction.length()
            val normalizedDirection = if (length != 0f) {
                Vector3f.from(direction.x / length, 0f, direction.z / length)
            } else {
                direction
            }

            Vector3f.from(
                targetPosition.x + normalizedDirection.x * distance,
                targetPosition.y,
                targetPosition.z + normalizedDirection.z * distance
            )
        } else {
            // Calculate direction vector from the player to the target
            val direction = Vector3f.from(
                targetPosition.x - playerPosition.x,
                0f,  // No modification to Y-axis
                targetPosition.z - playerPosition.z
            )

            // Normalize the direction to make it a unit vector
            val length = direction.length()
            val normalizedDirection = if (length != 0f) {
                Vector3f.from(
                    direction.x / length,
                    0f,
                    direction.z / length
                )  // No normalization for Y
            } else {
                direction
            }

            // Calculate new position, offsetting by 'distance' blocks away from the target
            Vector3f.from(
                targetPosition.x - normalizedDirection.x * distance,
                targetPosition.y,  // Follow the target's Y-axis
                targetPosition.z - normalizedDirection.z * distance
            )
        }

        // Create the MovePlayerPacket to teleport the player
        val movePlayerPacket = MovePlayerPacket().apply {
            runtimeEntityId = session.localPlayer.runtimeEntityId
            position = newPosition
            rotation = entity.vec3Rotation
            mode = MovePlayerPacket.Mode.NORMAL // Teleport mode
            isOnGround = false // Typically set to false when teleporting
            ridingRuntimeEntityId = 0 // Ensure no riding entity
            tick = session.localPlayer.tickExists // Use current player's tick
        }

        // Send the teleportation packet
        session.clientBound(movePlayerPacket)
    }


    private fun Entity.isTarget(): Boolean {
        return when (this) {
            is LocalPlayer -> false
            is Player -> {
                if (playersOnly || (playersOnly && mobsOnly)) {
                    !this.isBot()
                } else {
                    false
                }
            }

            is EntityUnknown -> {
                if (mobsOnly || (playersOnly && mobsOnly)) {
                    isMob()
                } else {
                    false
                }
            }

            else -> false
        }
    }


    private fun EntityUnknown.isMob(): Boolean {
        return this.identifier in MobList.mobTypes
    }

    private fun Player.isBot(): Boolean {
        if (this is LocalPlayer) return false
        val playerList = session.level.playerMap[this.uuid] ?: return true
        return playerList.name.isBlank()
    }

    private fun searchForClosestEntities(): List<Entity> {
        return session.level.entityMap.values
            .filter { entity -> entity.distance(session.localPlayer) < rangeValue && entity.isTarget() }
    }
}