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
         if (packet is PlayerAuthInputPacket){
            move(packet.position)
            rotate (packet.rotation)
            tickExists = packet.tick
        }
        return false
    }

    fun attack(session: MuCuteRelaySession, entity: Entity, heldItemSlot: Int, itemInHand: ItemData) {
        val animatePacket = AnimatePacket().apply {
            action = AnimatePacket.Action.SWING_ARM
            runtimeEntityId = runtimeEntityId
        }
        session.clientBound(animatePacket)
        session.serverBound(animatePacket)

        val levelSoundEventPacket = LevelSoundEventPacket().apply {
            sound = SoundEvent.ATTACK_NODAMAGE
            position = vec3Position
            extraData = -1
            identifier = "minecraft:player"
            isBabySound = false
            isRelativeVolumeDisabled = false
        }
        session.serverBound(levelSoundEventPacket)

        val inventoryTransactionPacket = InventoryTransactionPacket().apply {
            transactionType = InventoryTransactionType.ITEM_USE_ON_ENTITY
            actionType = 1
            runtimeEntityId = entity.runtimeEntityId
            hotbarSlot = heldItemSlot
            this.itemInHand = itemInHand
            playerPosition = vec3Position
            clickPosition = Vector3f.ZERO
        }
        session.serverBound(inventoryTransactionPacket)
    }

    override fun onDisconnect(reason: String) {
        super.onDisconnect(reason)
        reset()
    }

}
