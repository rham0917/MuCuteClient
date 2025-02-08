package com.mucheng.mucute.client.game.module.misc

import com.mucheng.mucute.client.game.InterceptablePacket
import com.mucheng.mucute.client.game.Module
import com.mucheng.mucute.client.game.ModuleCategory
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.data.skin.SerializedSkin
import org.cloudburstmc.protocol.bedrock.packet.MoveEntityAbsolutePacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerListPacket
import org.cloudburstmc.protocol.bedrock.packet.TextPacket
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.sqrt

class PlayerTracerModule : Module("player_tracer", ModuleCategory.Misc) {
    // Store player info by entityId
    private val playersInfo = mutableMapOf<Long, PlayerInfo>()
    private var playerPosition = Vector3f.from(0f, 0f, 0f)

    // Store previous positions and timestamps to calculate velocity
    private val previousPositions = mutableMapOf<Long, Vector3f>()
    private val previousTimestamps = mutableMapOf<Long, Long>()

    // Define a constant for the scan radius (in blocks)
    private val scanRadius = intValue("scanRadius", 500, 100..100000)

    // Data class to hold player information
    data class PlayerInfo(
        val entityId: Long,
        val name: String,
        val xuid: String,
        val platformChatId: String,
        val buildPlatform: Int,
        val skin: SerializedSkin
    )

    // Function to calculate velocity
    private fun calculateVelocity(
        entityId: Long,
        currentPosition: Vector3f,
        currentTime: Long
    ): Vector3f? {
        val previousPosition = previousPositions[entityId]
        val previousTimestamp = previousTimestamps[entityId]

        return if (previousPosition != null && previousTimestamp != null) {
            val timeDelta = currentTime - previousTimestamp
            if (timeDelta > 0) {
                // Calculate velocity: (current position - previous position) / time
                val velocity = Vector3f.from(
                    (currentPosition.x - previousPosition.x) / timeDelta,
                    (currentPosition.y - previousPosition.y) / timeDelta,
                    (currentPosition.z - previousPosition.z) / timeDelta
                )
                velocity
            } else {
                null
            }
        } else {
            null
        }
    }

    // Function to update the last known position and timestamp
    private fun updatePositionAndTimestamp(entityId: Long, currentPosition: Vector3f) {
        val currentTime = System.currentTimeMillis()
        previousPositions[entityId] = currentPosition
        previousTimestamps[entityId] = currentTime
    }

    // Function to send a message with detailed information
    private fun sendMessage(
        playerInfo: PlayerInfo,
        entityPosition: Vector3f,
        distance: Float,
        direction: String,
        velocity: Vector3f?,
        actionState: String?
    ) {
        val timestamp = System.currentTimeMillis()
        val lastKnownPosition =
            previousPositions[playerInfo.entityId]?.roundUpCoordinates() ?: "N/A"

        val textPacket = TextPacket().apply {
            type = TextPacket.Type.RAW
            isNeedsTranslation = false
            message = """
        §l§b[CutieAI]§r §ePlayer Gamertag: §a${playerInfo.name} §e| §eEntity ID: §c${playerInfo.entityId} §e| §ePosition: §a${entityPosition.roundUpCoordinates()} §e| §eDistance: §c${
                ceil(
                    distance
                )
            } §e| §eDirection: §d$direction
        §l§b[CutieAI]§r §7Additional Info: §fXbox UID: §7${playerInfo.xuid} §e| §7Height Difference: §f${
                ceil(
                    entityPosition.y - playerPosition.y
                )
            } Blocks §e| §7Last Known Position: §f$lastKnownPosition
    """.trimIndent()
            xuid = ""
            sourceName = ""
        }

        session.clientBound(textPacket) // Ensure session is properly initialized
    }

    // Handle incoming packets
    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) {
            return
        }

        // Process PlayerListPacket to store player information
        val packet = interceptablePacket.packet
        if (packet is PlayerListPacket) {
            packet.entries.forEach { entry ->
                playersInfo[entry.entityId] = PlayerInfo(
                    entityId = entry.entityId,
                    name = entry.name,
                    xuid = entry.xuid,
                    platformChatId = entry.platformChatId,
                    buildPlatform = entry.buildPlatform,
                    skin = entry.skin
                )
            }
        }

        // Process PlayerAuthInputPacket to get our player's position
        if (packet is PlayerAuthInputPacket) {
            playerPosition = packet.position
        }

        // Process MoveEntityAbsolutePacket to get position info for other entities
        if (packet is MoveEntityAbsolutePacket) {
            val entityId = packet.runtimeEntityId
            val entityPosition = packet.position
            val currentTime = System.currentTimeMillis()

            // Update last known position and timestamp
            updatePositionAndTimestamp(entityId, entityPosition)

            // Calculate velocity for the entity
            val velocity = calculateVelocity(entityId, entityPosition, currentTime)

            // Check if the entityId matches any stored player info
            val storedPlayerInfo = playersInfo[entityId]
            if (storedPlayerInfo != null) {
                // Calculate the distance between our player and the other player
                val distance = calculateDistance(playerPosition, entityPosition)

                // Only send the message if the entity is within the scan radius
                if (distance <= scanRadius.value.toFloat()) {
                    val direction = getCompassDirection(playerPosition, entityPosition)

                    // Send message with player info, entityId, position, and distance
                    sendMessage(
                        storedPlayerInfo,
                        entityPosition,
                        distance,
                        direction,
                        velocity,
                        null
                    )
                }
            }
        }
    }

    // Calculate Euclidean distance
    private fun calculateDistance(from: Vector3f, to: Vector3f): Float {
        val dx = from.x - to.x
        val dy = from.y - to.y
        val dz = from.z - to.z
        return sqrt((dx * dx + dy * dy + dz * dz).toDouble()).toFloat()
    }

    // Convert position to rounded-up string format
    private fun Vector3f.roundUpCoordinates(): String {
        val roundedX = ceil(this.x).toInt()
        val roundedY = ceil(this.y).toInt()
        val roundedZ = ceil(this.z).toInt()
        return "$roundedX, $roundedY, $roundedZ"
    }

    // Determine the 16-direction compass heading
    private fun getCompassDirection(from: Vector3f, to: Vector3f): String {
        val dx = to.x - from.x
        val dz = to.z - from.z
        val angle = (atan2(dz, dx) * (180 / PI) + 360) % 360
        return when {
            angle >= 348.75 || angle < 11.25 -> "N"
            angle >= 11.25 && angle < 33.75 -> "NNE"
            angle >= 33.75 && angle < 56.25 -> "NE"
            angle >= 56.25 && angle < 78.75 -> "ENE"
            angle >= 78.75 && angle < 101.25 -> "E"
            angle >= 101.25 && angle < 123.75 -> "ESE"
            angle >= 123.75 && angle < 146.25 -> "SE"
            angle >= 146.25 && angle < 168.75 -> "SSE"
            angle >= 168.75 && angle < 191.25 -> "S"
            angle >= 191.25 && angle < 213.75 -> "SSW"
            angle >= 213.75 && angle < 236.25 -> "SW"
            angle >= 236.25 && angle < 258.75 -> "WSW"
            angle >= 258.75 && angle < 281.25 -> "W"
            angle >= 281.25 && angle < 303.75 -> "WNW"
            angle >= 303.75 && angle < 326.25 -> "NW"
            else -> "NNW"
        }
    }
}
