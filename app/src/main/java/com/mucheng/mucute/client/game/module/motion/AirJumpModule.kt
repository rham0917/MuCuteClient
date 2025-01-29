import com.mucheng.mucute.client.game.Module
import com.mucheng.mucute.client.game.ModuleCategory
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.data.PlayerAuthInputData
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.SetEntityMotionPacket

class AirJumpModule : Module("air_jump", ModuleCategory.Motion) {

    override fun onReceived(packet: BedrockPacket): Boolean {
        if (packet is PlayerAuthInputPacket && isEnabled) {
            if (packet.inputData.contains(PlayerAuthInputData.JUMP_DOWN)) {
                // Use a coroutine to delay sending the motion packet
                GlobalScope.launch {
                    // Delay in milliseconds (500 ms delay in this case)
                    delay(500)

                    // Send motion packet to simulate jump after the delay
                    val motionPacket = SetEntityMotionPacket().apply {
                        runtimeEntityId = localPlayer.runtimeEntityId
                        motion = Vector3f.from(localPlayer.motionX, 0.42f, localPlayer.motionZ)
                    }
                    session.clientBound(motionPacket)
                }
            }
        }
        return false
    }
}
