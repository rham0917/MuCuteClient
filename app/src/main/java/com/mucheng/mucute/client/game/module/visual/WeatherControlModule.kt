package com.mucheng.mucute.client.game.module.visual

import com.mucheng.mucute.client.game.InterceptablePacket
import com.mucheng.mucute.client.game.Module
import com.mucheng.mucute.client.game.ModuleCategory
import org.cloudburstmc.protocol.bedrock.packet.LevelEventPacket
import org.cloudburstmc.protocol.bedrock.data.LevelEvent
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.math.vector.Vector3f

class WeatherControlModule : Module("weather_control", ModuleCategory.Visual) {

    private var clearWeather by boolValue("clear", true)
    private var rainWeather by boolValue("rain", false)
    private var thunderWeather by boolValue("thunderstorm", false)

    private var lastUpdate = 0L
    private var lastPosition = Vector3f.ZERO

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) {
            return
        }

        val packet = interceptablePacket.packet
        if (packet is PlayerAuthInputPacket) {
            lastPosition = Vector3f.from(packet.position.x, packet.position.y, packet.position.z)
            val currentTime = System.currentTimeMillis()

            if (currentTime - lastUpdate >= 100) {
                lastUpdate = currentTime

                session.clientBound(LevelEventPacket().apply {
                    type = LevelEvent.STOP_RAINING
                    position = lastPosition
                    data = 0
                })
                session.clientBound(LevelEventPacket().apply {
                    type = LevelEvent.STOP_THUNDERSTORM
                    position = lastPosition
                    data = 0
                })

                when {
                    clearWeather -> {

                    }
                    rainWeather -> {
                        session.clientBound(LevelEventPacket().apply {
                            type = LevelEvent.START_RAINING
                            position = lastPosition
                            data = 10000
                        })
                    }
                    thunderWeather -> {
                        session.clientBound(LevelEventPacket().apply {
                            type = LevelEvent.START_RAINING
                            position = lastPosition
                            data = 10000
                        })
                        session.clientBound(LevelEventPacket().apply {
                            type = LevelEvent.START_THUNDERSTORM
                            position = lastPosition
                            data = 10000
                        })
                    }
                }
            }
        }
    }

    override fun onDisabled() {
        super.onDisabled()
        if (isSessionCreated) {
            session.clientBound(LevelEventPacket().apply {
                type = LevelEvent.STOP_RAINING
                position = lastPosition
                data = 0
            })
            session.clientBound(LevelEventPacket().apply {
                type = LevelEvent.STOP_THUNDERSTORM
                position = lastPosition
                data = 0
            })
        }
    }
}
