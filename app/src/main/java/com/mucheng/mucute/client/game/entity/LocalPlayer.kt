package com.mucheng.mucute.client.game.entity

import com.mucheng.mucute.relay.MuCuteRelaySession
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.data.SoundEvent
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemData
import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.InventoryTransactionType
import org.cloudburstmc.protocol.bedrock.packet.AnimatePacket
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket
import org.cloudburstmc.protocol.bedrock.packet.InventoryTransactionPacket
import org.cloudburstmc.protocol.bedrock.packet.LevelSoundEventPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.StartGamePacket
import java.util.UUID

@Suppress("MemberVisibilityCanBePrivate")
class LocalPlayer : Player(0L, 0L, UUID.randomUUID(), "") {

    override var runtimeEntityId: Long = 0L
        private set

    override var uniqueEntityId: Long = 0L
        private set

    override var uuid: UUID = UUID.randomUUID()
        private set

    override fun beforePacketBound(packet: BedrockPacket): Boolean {
        super.beforePacketBound(packet)
        if (packet is StartGamePacket) {
            runtimeEntityId = packet.runtimeEntityId
            uniqueEntityId = packet.uniqueEntityId
        }
        if (packet is PlayerAuthInputPacket) {
            move(packet.position)
            rotate(packet.rotation)
            tickExists = packet.tick
        }
        return false
    }

    fun attack(
        session: MuCuteRelaySession,
        entity: Entity,
        heldItemSlot: Int,
        itemInHand: ItemData
    ) {
        val animatePacket = AnimatePacket()
        animatePacket.action = AnimatePacket.Action.SWING_ARM
        animatePacket.runtimeEntityId = runtimeEntityId

        session.clientBound(animatePacket)
        session.serverBound(animatePacket)

        val levelSoundEventPacket = LevelSoundEventPacket()
        levelSoundEventPacket.sound = SoundEvent.ATTACK_NODAMAGE
        levelSoundEventPacket.position = vec3Position
        levelSoundEventPacket.extraData = -1
        levelSoundEventPacket.identifier = "minecraft:player"
        levelSoundEventPacket.isBabySound = false
        levelSoundEventPacket.isRelativeVolumeDisabled = false

        session.serverBound(levelSoundEventPacket)

        val inventoryTransactionPacket = InventoryTransactionPacket()
        inventoryTransactionPacket.transactionType = InventoryTransactionType.ITEM_USE_ON_ENTITY
        inventoryTransactionPacket.actionType = 1
        inventoryTransactionPacket.runtimeEntityId = entity.runtimeEntityId
        inventoryTransactionPacket.hotbarSlot = heldItemSlot
        inventoryTransactionPacket.itemInHand = itemInHand
        inventoryTransactionPacket.playerPosition = vec3Position
        inventoryTransactionPacket.clickPosition = Vector3f.ZERO

        session.serverBound(inventoryTransactionPacket)
    }

    override fun onDisconnect(reason: String) {
        super.onDisconnect(reason)
        reset()
    }

}
