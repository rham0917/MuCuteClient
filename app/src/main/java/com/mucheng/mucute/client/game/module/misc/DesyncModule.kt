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
    private val updateDelay = 1000L // Shorter delay before resending stored packets (1 sec)
    private val minResendInterval = 100L
    private val maxResendInterval = 300L

    override fun onEnabled() {
        isDesynced = true
        sendChatMessage("§7[Desync] §aEnabled! Movement packets will be delayed.")
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onDisabled() {
        isDesynced = false
        sendChatMessage("§7[Desync] §cDisabled! Resending stored packets...")

        GlobalScope.launch {
            delay(updateDelay)
            while (storedPackets.isNotEmpty()) {
                val packet = storedPackets.poll()
                if (packet != null) {
                    // Send movement packet
                    session.clientBound(packet)
                    sendChatMessage("§7[Desync] §fSent packet: ${packet.inputData}") // Log packet in chat
                }

                delay(Random.nextLong(minResendInterval, maxResendInterval))
            }
        }
    }

    override fun beforePacketBound(packet: BedrockPacket): Boolean {
        if (isEnabled && isDesynced && packet is PlayerAuthInputPacket) {
            storedPackets.add(packet) // Store movement packet
            sendChatMessage("§7[Desync] §eStored packet: ${packet.inputData}") // Log stored packet in chat
            return true // Prevent packet from being sent immediately
        }
        return false
    }

    private fun sendChatMessage(message: String) {
        if (!isEnabled) {
            return  // Don't send messages if the module is disabled
        }

        if (!isSessionCreated) {
            return
        }

        // Prepare the chat packet
        val chatPacket = TextPacket().apply {
            type = TextPacket.Type.RAW  // Set message type as RAW
            this.message = message  // Set the message content
            platformChatId = ""
            sourceName = "MuCuteBot"  // Optionally set source name (e.g., your bot’s name)
        }

        // Send the message
        session.clientBound(chatPacket)
    }


}