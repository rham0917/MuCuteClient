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

    private var playersOnly by boolValue("players_only", true)

    private var mobsOnly by boolValue("mobs_only", false)

    private var rangeValue by floatValue("range", 3.7f, 2f..7f)
    private var attackInterval by intValue("delay", 5, 1..20)
    private var cpsValue by intValue("cps", 10, 1..20)
    private var boost by intValue("boost", 1, 1..10)

    private var lastAttackTime = 0L

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) {
            return
        }

        val packet = interceptablePacket.packet
        if (packet is PlayerAuthInputPacket) {
            val currentTime = System.currentTimeMillis()
            val minAttackDelay = 1000L / cpsValue

            if (packet.tick % attackInterval == 0L && (currentTime - lastAttackTime) >= minAttackDelay) {
                val closestEntities = searchForClosestEntities()
                if (closestEntities.isEmpty()) {
                    return
                }

                closestEntities.forEach { entity ->
                    repeat(boost) {
                        session.localPlayer.attack(entity)
                    }
                    lastAttackTime = currentTime
                }
            }
        }
    }

    private fun Entity.isTarget(): Boolean {
        return when (this) {
            is LocalPlayer -> false
            is Player -> {
                if (mobsOnly) {
                    false
                } else if (playersOnly) {
                    !this.isBot()
                } else {
                    !this.isBot()
                }
            }
            is EntityUnknown -> {
                if (mobsOnly) {
                    isMob()
                } else if (playersOnly) {
                    false
                } else {
                    true
                }
            }
            else -> false
        }
    }

    // Add new function to check if entity is a mob
    private fun EntityUnknown.isMob(): Boolean {
        // Add mob entity types here
        val mobTypes = listOf(
            "minecraft:armadillo",
            "minecraft:bat",
            "minecraft:bee",
            "minecraft:blaze",
            "minecraft:bogged",
            "minecraft:camel",
            "minecraft:cat",
            "minecraft:cave_spider",
            "minecraft:chicken",
            "minecraft:cod",
            "minecraft:cow",
            "minecraft:creeper",
            "minecraft:dolphin",
            "minecraft:donkey",
            "minecraft:drowned",
            "minecraft:elder_guardian",
            "minecraft:enderman",
            "minecraft:endermite",
            "minecraft:evoker",
            "minecraft:fox",
            "minecraft:frog",
            "minecraft:ghast",
            "minecraft:glow_squid",
            "minecraft:goat",
            "minecraft:guardian",
            "minecraft:hoglin",
            "minecraft:horse",
            "minecraft:husk",
            "minecraft:illusioner",
            "minecraft:iron_golem",
            "minecraft:llama",
            "minecraft:magma_cube",
            "minecraft:mooshroom",
            "minecraft:mule",
            "minecraft:ocelot",
            "minecraft:panda",
            "minecraft:parrot",
            "minecraft:phantom",
            "minecraft:pig",
            "minecraft:piglin",
            "minecraft:piglin_brute",
            "minecraft:pillager",
            "minecraft:polar_bear",
            "minecraft:pufferfish",
            "minecraft:rabbit",
            "minecraft:ravager",
            "minecraft:salmon",
            "minecraft:sheep",
            "minecraft:shulker",
            "minecraft:silverfish",
            "minecraft:skeleton",
            "minecraft:skeleton_horse",
            "minecraft:slime",
            "minecraft:snow_golem",
            "minecraft:spider",
            "minecraft:squid",
            "minecraft:stray",
            "minecraft:strider",
            "minecraft:tadpole",
            "minecraft:trader_llama",
            "minecraft:tropical_fish",
            "minecraft:turtle",
            "minecraft:vex",
            "minecraft:villager",
            "minecraft:vindicator",
            "minecraft:wandering_trader",
            "minecraft:warden",
            "minecraft:witch",
            "minecraft:wither",
            "minecraft:wither_skeleton",
            "minecraft:wolf",
            "minecraft:zoglin",
            "minecraft:zombie",
            "minecraft:zombie_horse",
            "minecraft:zombie_villager",
            "minecraft:zombified_piglin"
        )
        return this.identifier in mobTypes
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
