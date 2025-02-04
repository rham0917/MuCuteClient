package com.mucheng.mucute.client.game.module.combat

import android.util.Log
import com.mucheng.mucute.client.game.Module
import com.mucheng.mucute.client.game.ModuleCategory
import com.mucheng.mucute.client.game.entity.Entity
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemData
import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.InventoryTransactionType
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket
import org.cloudburstmc.protocol.bedrock.packet.InventoryTransactionPacket
import org.cloudburstmc.protocol.bedrock.packet.MobEquipmentPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerHotbarPacket
import kotlin.math.pow

class KillauraModule : Module("killaura", ModuleCategory.Combat) {

    private var rangeValue by floatValue("Range", 3.7f, 2f..7f)

    private var heldItemSlot = 0

    override fun beforePacketBound(packet: BedrockPacket): Boolean {
        if (!isEnabled) {
            return false
        }

        if (packet is PlayerHotbarPacket) {
            heldItemSlot = packet.selectedHotbarSlot
        }

        if (packet is MobEquipmentPacket) {
            heldItemSlot = packet.hotbarSlot
        }

        if (packet is PlayerAuthInputPacket && packet.tick % 10 == 0L) {
            val closestEntities = searchForClosestEntities()
            if (closestEntities.isEmpty()) {
                return false
            }

            Log.e("Closest", closestEntities.toString())

            closestEntities.forEach {
                attack(it)
            }
        }
        return false
    }

    private fun attack(entity: Entity) {
        session.serverBound(InventoryTransactionPacket().apply {
            transactionType = InventoryTransactionType.ITEM_USE_ON_ENTITY
            actionType = 1
            runtimeEntityId = entity.runtimeEntityId
            hotbarSlot = heldItemSlot
            itemInHand = ItemData.AIR
            playerPosition = localPlayer.vec3Position
            clickPosition = Vector3f.ZERO
        })
    }

    private fun searchForClosestEntities(): List<Entity> {
        return level.entityMap.values
            .filter { it.distanceSq(localPlayer) < rangeValue.pow(2) }
    }

}