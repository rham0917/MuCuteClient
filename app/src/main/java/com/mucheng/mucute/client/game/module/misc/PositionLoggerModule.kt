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

class PositionLoggerModule : Module("whereisit", ModuleCategory.Misc) {

    private var playerPosition = Vector3f.from(0f, 0f, 0f)
    private val entityPositions = mutableMapOf<Long, Vector3f>()

    init {
        println("PositionLogger Module initialized")
    }

    override fun onReceived(packet: BedrockPacket): Boolean {
        if (!isEnabled) return false

        return handleReceivedPacket(packet)
    }

    private fun handleReceivedPacket(packet: BedrockPacket): Boolean {
        return when (packet) {
            is PlayerAuthInputPacket -> {
                handlePlayerAuthInputPacket(packet)
                true
            }
            is MoveEntityAbsolutePacket -> {
                handleMoveEntityAbsolutePacket(packet)
                true
            }
            else -> false
        }
    }

    private fun handlePlayerAuthInputPacket(packet: PlayerAuthInputPacket) {
        updatePlayerPosition(packet)
        val closestEntityId = findClosestEntityToPlayer()
        closestEntityId?.let {
            val entityData = fetchEntityData(it)
            val direction = computeDirection(playerPosition, entityData.first)
            sendEntityDetails(entityData.first, entityData.second, direction)
        }
    }

    private fun handleMoveEntityAbsolutePacket(packet: MoveEntityAbsolutePacket) {
        updateEntityPosition(packet.runtimeEntityId, packet.position)
    }

    private fun updatePlayerPosition(packet: PlayerAuthInputPacket) {
        playerPosition = packet.position
    }

    private fun findClosestEntityToPlayer(): Long? {
        return searchForClosestEntity()
    }

    private fun searchForClosestEntity(): Long? {
        var closestEntityId: Long? = null
        var closestDistance = Float.MAX_VALUE
        entityPositions.forEach { (entityId, entityPosition) ->
            val distance = calculateEuclideanDistance(playerPosition, entityPosition)
            if (distance < closestDistance) {
                closestDistance = distance
                closestEntityId = entityId
            }
        }
        return closestEntityId
    }

    private fun fetchEntityData(entityId: Long): Pair<Vector3f, Float> {
        val position = getEntityPosition(entityId)
        val distance = calculateEuclideanDistance(playerPosition, position)
        return position to distance
    }

    private fun getEntityPosition(entityId: Long): Vector3f {
        return entityPositions[entityId] ?: Vector3f.from(0f, 0f, 0f)
    }

    private fun computeDirection(from: Vector3f, to: Vector3f): String {
        val angle = calculateAngle(from, to)
        return determineDirection(angle)
    }

    private fun calculateAngle(from: Vector3f, to: Vector3f): Double {
        val dx = to.x - from.x
        val dz = to.z - from.z
        return atan2(dz.toDouble(), dx.toDouble()) // Cast to Double
    }

    private fun determineDirection(angle: Double): String {
        val normalizedAngle = normalizeAngle(angle)
        return mapAngleToCompassDirection(normalizedAngle)
    }

    private fun normalizeAngle(angle: Double): Double {
        val angleInDegrees = (angle * 180 / Math.PI + 360) % 360
        return angleInDegrees
    }

    private fun mapAngleToCompassDirection(angle: Double): String {
        return when {
            angle < 11.25 || angle >= 348.75 -> "N"
            angle in 11.25..33.75 -> "NNE"
            angle in 33.75..56.25 -> "NE"
            angle in 56.25..78.75 -> "ENE"
            angle in 78.75..101.25 -> "E"
            angle in 101.25..123.75 -> "ESE"
            angle in 123.75..146.25 -> "SE"
            angle in 146.25..168.75 -> "SSE"
            angle in 168.75..191.25 -> "S"
            angle in 191.25..213.75 -> "SSW"
            angle in 213.75..236.25 -> "SW"
            angle in 236.25..258.75 -> "WSW"
            angle in 258.75..281.25 -> "W"
            angle in 281.25..303.75 -> "WNW"
            angle in 303.75..326.25 -> "NW"
            else -> "NNW"
        }
    }

    private fun sendEntityDetails(position: Vector3f, distance: Float, direction: String) {
        val roundedPosition = roundPositionCoordinates(position)
        val roundedDistance = roundDistance(distance)
        sendMessage("§l§b[PositionLogger]§r §eClosest entity at §a$roundedPosition §e| Distance: §c$roundedDistance §e| Direction: §d$direction")
    }

    private fun roundPositionCoordinates(position: Vector3f): String {
        val x = ceil(position.x.toDouble()).toInt()  // Cast to Double before calling ceil
        val y = ceil(position.y.toDouble()).toInt()  // Cast to Double before calling ceil
        val z = ceil(position.z.toDouble()).toInt()  // Cast to Double before calling ceil
        return "$x, $y, $z"
    }

    private fun roundDistance(distance: Float): Int {
        return ceil(distance.toDouble()).toInt()  // Cast to Double before calling ceil
    }

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

    private fun updateEntityPosition(entityId: Long, position: Vector3f) {
        entityPositions[entityId] = position
    }

    // Calculate Euclidean distance
    private fun calculateEuclideanDistance(from: Vector3f, to: Vector3f): Float {
        val dx = from.x - to.x
        val dy = from.y - to.y
        val dz = from.z - to.z
        val squaredDistance = (dx * dx + dy * dy + dz * dz).toDouble()  // Convert to Double for sqrt calculation
        return sqrt(squaredDistance).toFloat()
    }
}
