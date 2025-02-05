package com.mucheng.mucute.client.game.module.misc

import com.mucheng.mucute.client.game.Module
import com.mucheng.mucute.client.game.ModuleCategory
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.MoveEntityAbsolutePacket
import org.cloudburstmc.protocol.bedrock.packet.TextPacket
import kotlin.math.sqrt
import kotlin.math.*

class PositionLoggerModule : Module("positionLogger", ModuleCategory.Misc) {

    private var playerPosition = Vector3f.from(0f, 0f, 0f)
    private val entityPositions = mutableMapOf<Long, Vector3f>()

    init {
        println("PositionLogger Module initialized")
    }

    override fun beforePacketBound(packet: BedrockPacket): Boolean {
        if (!isEnabled) return false

        if (packet is PlayerAuthInputPacket) {
            playerPosition = packet.position

            // Find the closest entity
            var closestEntityId: Long? = null
            var closestDistance = Float.MAX_VALUE
            var closestEntityPosition: Vector3f? = null

            entityPositions.forEach { (entityId, entityPos) ->
                val distance = calculateDistance(playerPosition, entityPos)
                if (distance < closestDistance) {
                    closestDistance = distance
                    closestEntityId = entityId
                    closestEntityPosition = entityPos
                }
            }

            // If a closest entity is found, send the message
            if (closestEntityId != null && closestEntityPosition != null) {
                val roundedPosition = closestEntityPosition!!.roundUpCoordinates()
                val roundedDistance = ceil(closestDistance) // Round up distance
                val direction = getCompassDirection(playerPosition, closestEntityPosition!!)

                sendMessage("§l§b[PositionLogger]§r §eClosest entity at §a$roundedPosition §e| Distance: §c$roundedDistance §e| Direction: §d$direction")
            }
        }

        if (packet is MoveEntityAbsolutePacket) {
            val entityId = packet.runtimeEntityId
            val entityPosition = packet.position
            entityPositions[entityId] = entityPosition
        }

        return false
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

        // Calculate angle in degrees (-180 to 180)
        val angle = (atan2(dz, dx) * (180 / PI) + 360) % 360

        // Map the angle to 16 compass directions
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

    // Send message in chat with Minecraft Bedrock colors
    private fun sendMessage(msg: String) {
        val textPacket = TextPacket().apply {
            type = TextPacket.Type.RAW
            isNeedsTranslation = false
            message = msg
            xuid = ""
            sourceName = ""
        }
        session.clientBound(textPacket)
    }


}