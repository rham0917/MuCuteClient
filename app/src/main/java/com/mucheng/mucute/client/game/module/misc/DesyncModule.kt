package com.mucheng.mucute.client.game.module.misc

import com.mucheng.mucute.client.game.Module
import com.mucheng.mucute.client.game.ModuleCategory
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.TextPacket
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.random.Random

class DesyncModule : Module("desync", ModuleCategory.Misc) {

    private var isDesynced = false
    private val storedPackets = ConcurrentLinkedQueue<PlayerAuthInputPacket>()
    private val updateDelay = 1000L
    private val minResendInterval = 100L
    private val maxResendInterval = 300L

    override fun onEnabled() {
        if (isSessionCreated) {
            sendToggleMessage(true)
        }
        isDesynced = true
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onDisabled() {
        if (isSessionCreated) {
            sendToggleMessage(false)
        }
        isDesynced = false

        GlobalScope.launch {
            delay(updateDelay)
            while (storedPackets.isNotEmpty()) {
                val packet = storedPackets.poll()
                if (packet != null) {
                    session.clientBound(packet)
                }
                delay(Random.nextLong(minResendInterval, maxResendInterval))
            }
        }
    }

    override fun beforePacketBound(packet: BedrockPacket): Boolean {
        if (isEnabled && isDesynced && packet is PlayerAuthInputPacket) {
            storedPackets.add(packet)
            return true
        }
        return false
    }

    private fun sendToggleMessage(enabled: Boolean) {
        val status = if (enabled) "§aEnabled" else "§cDisabled"
        val message = "§l§b[MuCute] §r§7Desync §8» $status"

        val textPacket = TextPacket().apply {
            type = TextPacket.Type.RAW
            isNeedsTranslation = false
            this.message = message
            xuid = ""
            sourceName = ""
        }

        session.clientBound(textPacket)
    }
}