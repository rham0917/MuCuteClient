package com.mucheng.mucute.client.game.module.combat

import com.mucheng.mucute.client.game.InterceptablePacket
import com.mucheng.mucute.client.game.Module
import com.mucheng.mucute.client.game.ModuleCategory
import com.mucheng.mucute.client.game.entity.Entity
import com.mucheng.mucute.client.game.entity.EntityUnknown
import com.mucheng.mucute.client.game.entity.LocalPlayer
import com.mucheng.mucute.client.game.entity.Player
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket

class KillauraModule : Module("killaura", ModuleCategory.Combat) {

    private var rangeValue by floatValue("range", 3.7f, 2f..7f)
    private var attackInterval by intValue("Delay", 5, 1..20)

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) {
            return
        }

        val packet = interceptablePacket.packet
        if (packet is PlayerAuthInputPacket) {
            if (packet.tick % attackInterval == 0L) {
                val closestEntities = searchForClosestEntities()
                if (closestEntities.isEmpty()) {
                    return
                }

                closestEntities.forEach { entity ->
                    session.localPlayer.attack(entity)
                }
            }
        }
    }

    private fun Entity.isTarget(): Boolean {
        return when (this) {
            is LocalPlayer -> false
            is Player -> !this.isBot()
            is EntityUnknown -> true
            else -> false
        }
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
