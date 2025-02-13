package com.mucheng.mucute.client.game.module.motion

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.mucheng.mucute.client.game.InterceptablePacket
import com.mucheng.mucute.client.game.Module
import com.mucheng.mucute.client.game.ModuleCategory
import org.cloudburstmc.protocol.bedrock.packet.UpdateAbilitiesPacket

// this module is intended to be used to save packets which are used across the motion modules, and should NOT be displayed in the module list.
// e.g. UpdateAbilitiesPacket is used by speed and fly, we can use this module to intercept it and save a copy of the packet for later use.
//      this ensures the modules don't collide with each other and rather operate independently, if we don't do this speed would disable fly and fly would disable speed.. ( abilities shouldn't collide )
class MotionVarModule :
    Module("_var_", ModuleCategory.Motion, defaultEnabled = true, private = true) {

    companion object {
        var lastUpdateAbilitiesPacket: UpdateAbilitiesPacket? by mutableStateOf(null)
    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (interceptablePacket.packet is UpdateAbilitiesPacket) {
            lastUpdateAbilitiesPacket = interceptablePacket.packet
        }
    }

}