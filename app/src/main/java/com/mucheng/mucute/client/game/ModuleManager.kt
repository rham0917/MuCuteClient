package com.mucheng.mucute.client.game

import com.mucheng.mucute.client.application.AppContext
import com.mucheng.mucute.client.game.entity.LocalPlayer
import com.mucheng.mucute.client.game.module.combat.AntiKnockbackModule
import com.mucheng.mucute.client.game.module.combat.KillauraModule
import com.mucheng.mucute.client.game.module.misc.PositionLoggerModule
import com.mucheng.mucute.client.game.module.effect.HasteModule
import com.mucheng.mucute.client.game.module.effect.LevitationModule
import com.mucheng.mucute.client.game.module.effect.PoseidonModule
import com.mucheng.mucute.client.game.module.effect.RegenModule
import com.mucheng.mucute.client.game.module.misc.NoClipModule
import com.mucheng.mucute.client.game.module.motion.AirJumpModule
import com.mucheng.mucute.client.game.module.motion.AutoJumpModule
import com.mucheng.mucute.client.game.module.motion.AutoWalkModule
import com.mucheng.mucute.client.game.module.misc.DesyncModule
import com.mucheng.mucute.client.game.module.motion.FlyModule
import com.mucheng.mucute.client.game.module.motion.HighJumpModule
import com.mucheng.mucute.client.game.module.motion.JetPackModule
import com.mucheng.mucute.client.game.module.motion.RandomMoveModule
import com.mucheng.mucute.client.game.module.effect.SlowFallModule
import com.mucheng.mucute.client.game.module.motion.SpeedModule
import com.mucheng.mucute.client.game.module.motion.SprintModule
import com.mucheng.mucute.client.game.module.visual.NightVisionModule
import com.mucheng.mucute.client.game.module.visual.NoHurtCamModule
import com.mucheng.mucute.client.game.module.visual.ZoomModule
import com.mucheng.mucute.client.game.world.Level
import com.mucheng.mucute.relay.MuCuteRelaySession
import com.mucheng.mucute.relay.listener.MuCuteRelayPacketListener
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.TextPacket

object ModuleManager : MuCuteRelayPacketListener {

    private val _modules: MutableList<Module> = ArrayList()

    val modules: List<Module> = _modules

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    private var localPlayer: LocalPlayer? = null

    private var level: Level? = null

    private var session: MuCuteRelaySession? = null

    private val versionName by lazy {
        AppContext.instance.packageManager.getPackageInfo(
            AppContext.instance.packageName, 0
        ).versionName
    }

    init {
        with(_modules) {
            add(FlyModule())
            add(ZoomModule())
            add(AirJumpModule())
            add(NoClipModule())
            add(NightVisionModule())
            add(HasteModule())
            add(SpeedModule())
            add(JetPackModule())
            add(LevitationModule())
            add(HighJumpModule())
            add(SlowFallModule())
            add(PoseidonModule())
            add(AntiKnockbackModule())
            add(RegenModule())
            add(AutoJumpModule())
            add(SprintModule())
            add(NoHurtCamModule())
            add(AutoWalkModule())
            add(RandomMoveModule())
            add(DesyncModule())
            add(PositionLoggerModule())
            add(KillauraModule())
        }
    }

    fun initModules(muCuteRelaySession: MuCuteRelaySession) {
        this.session = muCuteRelaySession

        val localPlayer = LocalPlayer()
        val level = Level(muCuteRelaySession)

        for (module in _modules) {
            module.session = muCuteRelaySession
            module.localPlayer = localPlayer
            module.level = level
        }

        this.localPlayer = localPlayer
        this.level = level
    }

    fun saveConfig() {
        val configsDir = AppContext.instance.filesDir.resolve("configs")
        configsDir.mkdirs()

        val config = configsDir.resolve("UserConfig.json")
        val jsonObject = buildJsonObject {
            put("modules", buildJsonObject {
                _modules.forEach {
                    put(it.name, it.toJson())
                }
            })
        }

        config.writeText(json.encodeToString(jsonObject))
    }

    fun loadConfig() {
        val configsDir = AppContext.instance.filesDir.resolve("configs")
        configsDir.mkdirs()

        val config = configsDir.resolve("UserConfig.json")
        if (!config.exists()) {
            return
        }

        val jsonString = config.readText()
        if (jsonString.isEmpty()) {
            return
        }

        val jsonObject = json.parseToJsonElement(jsonString).jsonObject
        val modules = jsonObject["modules"]!!.jsonObject
        _modules.forEach { module ->
            (modules[module.name] as? JsonObject)?.let {
                module.fromJson(it)
            }
        }
    }

    override fun beforeClientBound(packet: BedrockPacket): Boolean {
        localPlayer?.beforePacketBound(packet)
        level?.beforePacketBound(packet)

        if (packet is PlayerAuthInputPacket && packet.tick % 20 == 0L) {
            session!!.clientBound(TextPacket().apply {
                type = TextPacket.Type.TIP
                isNeedsTranslation = false
                sourceName = ""
                message = "[MuCuteClient] $versionName"
                xuid = ""
                platformChatId = ""
                filteredMessage = ""
            })
        }

        for (module in _modules) {
            if (module.beforePacketBound(packet)) {
                return true
            }
        }

        return false
    }

    override fun beforeServerBound(packet: BedrockPacket): Boolean {
        localPlayer?.beforePacketBound(packet)
        level?.beforePacketBound(packet)

        for (module in _modules) {
            if (module.beforePacketBound(packet)) {
                return true
            }
        }

        return false
    }

    override fun afterClientBound(packet: BedrockPacket) {
        for (module in _modules) {
            module.afterPacketBound(packet)
        }
    }

    override fun afterServerBound(packet: BedrockPacket) {
        for (module in _modules) {
            module.afterPacketBound(packet)
        }
    }

}
