package com.mucheng.mucute.client.game


// import com.mucheng.mucute.client.game.module.misc.CriticModule
// import com.mucheng.mucute.client.game.module.misc.CrasherModule
import com.mucheng.mucute.client.application.AppContext
import com.mucheng.mucute.client.game.module.combat.AntiCrystalModule
import com.mucheng.mucute.client.game.module.combat.AntiKnockbackModule
import com.mucheng.mucute.client.game.module.combat.KillauraModule
import com.mucheng.mucute.client.game.module.effect.AbsorptionModule
import com.mucheng.mucute.client.game.module.effect.BadOmenModule
import com.mucheng.mucute.client.game.module.effect.BlindnessModule
import com.mucheng.mucute.client.game.module.effect.ConduitPowerModule
import com.mucheng.mucute.client.game.module.effect.DarknessModule
import com.mucheng.mucute.client.game.module.effect.FatalPoisonModule
import com.mucheng.mucute.client.game.module.effect.FireResistanceModule
import com.mucheng.mucute.client.game.module.effect.HasteModule
import com.mucheng.mucute.client.game.module.effect.HealthBoostModule
import com.mucheng.mucute.client.game.module.effect.HungerModule
import com.mucheng.mucute.client.game.module.effect.InstantDamageModule
import com.mucheng.mucute.client.game.module.effect.InstantHealthModule
import com.mucheng.mucute.client.game.module.effect.InvisibilityModule
import com.mucheng.mucute.client.game.module.effect.JumpBoostModule
import com.mucheng.mucute.client.game.module.effect.LevitationModule
import com.mucheng.mucute.client.game.module.effect.NauseaModule
import com.mucheng.mucute.client.game.module.effect.NightVisionModule
import com.mucheng.mucute.client.game.module.effect.PoisonModule
import com.mucheng.mucute.client.game.module.effect.PoseidonModule
import com.mucheng.mucute.client.game.module.effect.RegenerationModule
import com.mucheng.mucute.client.game.module.effect.ResistanceModule
import com.mucheng.mucute.client.game.module.effect.SaturationModule
import com.mucheng.mucute.client.game.module.effect.SlowFallingModule
import com.mucheng.mucute.client.game.module.effect.StrengthModule
import com.mucheng.mucute.client.game.module.effect.SwiftnessModule
import com.mucheng.mucute.client.game.module.effect.VillageHeroModule
import com.mucheng.mucute.client.game.module.effect.WeaknessModule
import com.mucheng.mucute.client.game.module.effect.WitherModule
import com.mucheng.mucute.client.game.module.misc.DesyncModule
import com.mucheng.mucute.client.game.module.misc.NoClipModule
import com.mucheng.mucute.client.game.module.misc.PositionLoggerModule
import com.mucheng.mucute.client.game.module.motion.AirJumpModule
import com.mucheng.mucute.client.game.module.motion.AntiAFKModule
import com.mucheng.mucute.client.game.module.motion.AutoWalkModule
import com.mucheng.mucute.client.game.module.motion.BhopModule
import com.mucheng.mucute.client.game.module.motion.FlyModule
import com.mucheng.mucute.client.game.module.motion.HighJumpModule
import com.mucheng.mucute.client.game.module.motion.JetPackModule
import com.mucheng.mucute.client.game.module.motion.MotionFlyModule
import com.mucheng.mucute.client.game.module.motion.SpeedModule
import com.mucheng.mucute.client.game.module.motion.SprintModule
import com.mucheng.mucute.client.game.module.visual.FreeCameraModule
import com.mucheng.mucute.client.game.module.visual.NoHurtCameraModule
import com.mucheng.mucute.client.game.module.visual.TimeShiftModule
import com.mucheng.mucute.client.game.module.visual.ZoomModule
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject

object ModuleManager {

    private val _modules: MutableList<Module> = ArrayList()

    val modules: List<Module> = _modules

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
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
            add(SlowFallingModule())
            add(PoseidonModule())
            add(AntiKnockbackModule())
            add(RegenerationModule())
            add(BhopModule())
            add(SprintModule())
            add(NoHurtCameraModule())
            add(AutoWalkModule())
            add(AntiAFKModule())
            add(DesyncModule())
            add(PositionLoggerModule())
            add(MotionFlyModule())
            add(FreeCameraModule())
            add(KillauraModule())
            // add(CriticModule())
            add(NauseaModule())
            add(HealthBoostModule())
            add(JumpBoostModule())
            add(ResistanceModule())
            add(FireResistanceModule())
            add(SwiftnessModule())
            add(InstantHealthModule())
            add(StrengthModule())
            add(InstantDamageModule())
            add(InvisibilityModule())
            add(SaturationModule())
            add(AbsorptionModule())
            add(BlindnessModule())
            add(AntiCrystalModule())
            add(HungerModule())
            add(WeaknessModule())
            add(PoisonModule())
            add(WitherModule())
            add(FatalPoisonModule())
            add(ConduitPowerModule())
            add(BadOmenModule())
            add(VillageHeroModule())
            add(DarknessModule())
            add(TimeShiftModule())
            //  add(CrasherModule())


        }
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

}
