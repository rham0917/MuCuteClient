package com.mucheng.mucute.client.game.module.visual

import com.mucheng.mucute.client.game.InterceptablePacket
import com.mucheng.mucute.client.game.Module
import com.mucheng.mucute.client.game.ModuleCategory
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.SetTimePacket

class TimeShiftModule : Module("time_shift", ModuleCategory.Visual) {

    private val timeOfDay by intValue("time", 6000, 0..24000)
    private var lastTimeUpdate = 0L

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) {
            return
        }

        val packet = interceptablePacket.packet
        if (packet is PlayerAuthInputPacket) {
            val currentTime = System.currentTimeMillis()

            // Update time every 100ms
            if (currentTime - lastTimeUpdate >= 100) {
                lastTimeUpdate = currentTime

                val timePacket = SetTimePacket()
                timePacket.setTime(timeOfDay)
                session.clientBound(timePacket)
            }
        }
    }

    override fun onDisabled() {
        super.onDisabled()
        if (isSessionCreated) {
            val timePacket = SetTimePacket()
            timePacket.setTime(0)
            session.clientBound(timePacket)
        }
    }
}
