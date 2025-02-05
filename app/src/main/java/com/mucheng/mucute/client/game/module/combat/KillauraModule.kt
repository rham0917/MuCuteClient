package com.mucheng.mucute.client.game.module.combat

import android.util.Log
import com.mucheng.mucute.client.game.Module
import com.mucheng.mucute.client.game.ModuleCategory
import com.mucheng.mucute.client.game.entity.Entity
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.data.SoundEvent
import org.cloudburstmc.protocol.bedrock.data.inventory.ContainerId
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemData
import org.cloudburstmc.protocol.bedrock.data.inventory.itemstack.request.action.PlaceAction
import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.InventoryActionData
import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.InventorySource
import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.InventoryTransactionType
import org.cloudburstmc.protocol.bedrock.packet.AnimatePacket
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket
import org.cloudburstmc.protocol.bedrock.packet.InventoryContentPacket
import org.cloudburstmc.protocol.bedrock.packet.InventorySlotPacket
import org.cloudburstmc.protocol.bedrock.packet.InventoryTransactionPacket
import org.cloudburstmc.protocol.bedrock.packet.ItemStackRequestPacket
import org.cloudburstmc.protocol.bedrock.packet.LevelSoundEventPacket
import org.cloudburstmc.protocol.bedrock.packet.MobEquipmentPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerHotbarPacket

class KillauraModule : Module("killaura", ModuleCategory.Combat) {

    private var rangeValue by floatValue("Range", 3.7f, 2f..7f)

    private var heldItemSlot = 0

    private val content = Array(41) { ItemData.AIR }

    override fun beforePacketBound(packet: BedrockPacket): Boolean {
        if (packet is PlayerHotbarPacket) {
            heldItemSlot = packet.selectedHotbarSlot
        }

        if (packet is MobEquipmentPacket && packet.runtimeEntityId == localPlayer.runtimeEntityId) {
            heldItemSlot = packet.hotbarSlot
        }

        if (packet is InventoryTransactionPacket && packet.runtimeEntityId == localPlayer.runtimeEntityId && packet.transactionType == InventoryTransactionType.NORMAL) {
            packet.actions.filter { it is InventoryActionData && it.source.type == InventorySource.Type.CONTAINER }
                .forEach {
                    val containerId =
                        getOffsetByContainerId(it.source.containerId) ?: return@forEach
                    content[it.slot + containerId] = it.toItem
                }
        }

        if (packet is InventorySlotPacket) {
            getOffsetByContainerId(packet.containerId)?.let {
                content[packet.slot + it] = packet.item
            }
        }

        if (packet is InventoryContentPacket) {
            getOffsetByContainerId(packet.containerId)?.let {
                fillContent(packet.contents, it)
            }
        }

        if (!isEnabled) {
            return false
        }

        if (packet is PlayerAuthInputPacket && packet.tick % 5 == 0L) {
            val closestEntities = searchForClosestEntities()
            if (closestEntities.isEmpty()) {
                return false
            }

            Log.e("itemInHand", content[heldItemSlot].toString())

            closestEntities.forEach { entity ->
                localPlayer.attack(entity, heldItemSlot, content[heldItemSlot])
            }
        }
        return false
    }

    private fun fillContent(contents: List<ItemData>, offset: Int) {
        contents.forEachIndexed { i, item ->
            content[offset + i] = item
        }
    }

    private fun getOffsetByContainerId(container: Int): Int? {
        return when (container) {
            0 -> 0
            ContainerId.ARMOR -> 36
            ContainerId.OFFHAND -> 40
            else -> null
        }
    }

    private fun searchForClosestEntities(): List<Entity> {
        return level.entityMap.values
            .filter { it.distance(localPlayer) < rangeValue }
    }

}