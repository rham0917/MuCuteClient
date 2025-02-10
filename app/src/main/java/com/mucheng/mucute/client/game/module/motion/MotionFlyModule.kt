package com.mucheng.mucute.client.game.module.motion

import com.mucheng.mucute.client.game.Module
import com.mucheng.mucute.client.game.ModuleCategory
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.data.Ability
import org.cloudburstmc.protocol.bedrock.data.AbilityLayer
import org.cloudburstmc.protocol.bedrock.data.PlayerAuthInputData
import org.cloudburstmc.protocol.bedrock.data.PlayerPermission
import org.cloudburstmc.protocol.bedrock.data.command.CommandPermission
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.SetEntityMotionPacket
import org.cloudburstmc.protocol.bedrock.packet.UpdateAbilitiesPacket
import kotlin.math.cos
import kotlin.math.sin

class MotionFlyModule : Module("motion_fly", ModuleCategory.Motion) {

    private val verticalSpeedUp by floatValue("Vertical Speed (Up)", 7.0f, 1.0f..20.0f)
    private val verticalSpeedDown by floatValue("Vertical Speed (Down)", 7.0f, 1.0f..20.0f)
    private var motionInterval by floatValue("Hop Delay", 100f, 100..600)
    private val speedValue by floatValue("speed", 0.8f, 0.1f..5.0f)
    private var jitterState = false
    private var lastMotionTime = 0L


    private val enableFlyAbilitiesPacket = UpdateAbilitiesPacket().apply {
            playerPermission = PlayerPermission.OPERATOR
            commandPermission = CommandPermission.OWNER
            abilityLayers.add(AbilityLayer().apply {
                layerType = AbilityLayer.Type.BASE
                abilitiesSet.addAll(Ability.entries.toTypedArray())
                abilityValues.addAll(
                    arrayOf(
                        Ability.BUILD,
                        Ability.MINE,
                        Ability.DOORS_AND_SWITCHES,
                        Ability.OPEN_CONTAINERS,
                        Ability.ATTACK_PLAYERS,
                        Ability.ATTACK_MOBS,
                        Ability.OPERATOR_COMMANDS,
                        Ability.MAY_FLY,
                        Ability.FLY_SPEED,
                        Ability.WALK_SPEED
                    )
                )
                walkSpeed = 0.1f
                flySpeed = 2.19f
            })
        }

        private val disableFlyAbilitiesPacket = UpdateAbilitiesPacket().apply {
            playerPermission = PlayerPermission.OPERATOR
            commandPermission = CommandPermission.OWNER
            abilityLayers.add(AbilityLayer().apply {
                layerType = AbilityLayer.Type.BASE
                abilitiesSet.addAll(Ability.entries.toTypedArray())
                abilityValues.addAll(
                    arrayOf(
                        Ability.BUILD,
                        Ability.MINE,
                        Ability.DOORS_AND_SWITCHES,
                        Ability.OPEN_CONTAINERS,
                        Ability.ATTACK_PLAYERS,
                        Ability.ATTACK_MOBS,
                        Ability.OPERATOR_COMMANDS,
                        Ability.FLY_SPEED,
                        Ability.WALK_SPEED
                    )
                )
                walkSpeed = 0.1f
            })
        }

        private var canFly = false

        override fun beforePacketBound(packet: BedrockPacket): Boolean {
            if (packet is PlayerAuthInputPacket) {
                if (!canFly && isEnabled) {
                    enableFlyAbilitiesPacket.uniqueEntityId = localPlayer.uniqueEntityId
                    session.clientBound(enableFlyAbilitiesPacket)
                    canFly = true
                } else if (canFly && !isEnabled) {
                    disableFlyAbilitiesPacket.uniqueEntityId = localPlayer.uniqueEntityId
                    session.clientBound(disableFlyAbilitiesPacket)
                    canFly = false
                }
            }

            if (isEnabled) {
                val currentTime = System.currentTimeMillis()

                if (currentTime - lastMotionTime >= motionInterval) {
                    val yaw = localPlayer.rotationYaw.toFloat()

                    var moveX = 0f
                    var moveZ = 0f

                    moveX -= 0
                    moveZ += 0

                    var vertical = 0f
                    if (packet is PlayerAuthInputPacket) {
                        val inputData = packet.inputData
                        if (inputData.contains(PlayerAuthInputData.WANT_UP)) {
                            vertical = verticalSpeedUp
                        } else if (inputData.contains(PlayerAuthInputData.WANT_DOWN)) {
                            vertical = -verticalSpeedDown
                        }
                    }

                    if (jitterState) {
                        vertical += 0.1f
                    } else {
                        vertical -= 0.1f
                    }

                    jitterState = !jitterState

                    val motionPacket = SetEntityMotionPacket().apply {
                        runtimeEntityId = localPlayer.runtimeEntityId
                        motion = Vector3f.from(moveX * speedValue, vertical, moveZ * speedValue)
                    }

                    session.clientBound(motionPacket)

                    lastMotionTime = currentTime
                }
            }

            return false
        }
    }
