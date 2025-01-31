package com.mucheng.mucute.client.game

object TranslationManager {

    private val map = HashMap<String, Map<String, String>>()

    init {
        map["en"] = en()
        map["zh"] = zh()
    }

    private fun en() = buildMap {
        put("fly", "Fly")
        put("no_clip", "No Clip")
        put("zoom", "Zoom")
        put("air_jump",  "Air Jump")
        put("speed", "Speed Boost")
        put("full_bright", "Fullbright")
        put("haste", "Haste")
        put("jetpack", "Jetpack")
        put("levitation", "Levitation")
        put("high_jump", "High Jump")
        put("slow_fall", "Slow Fall")
        put("anti_knockback","Velocity")
        put("poseidon","Poseidon")
        put("regen","Regen")
        put("auto_jump","BHOP")
        put("sprint","Sprint")
        put("no_hurt_camera","NoHurtCam")

        // Below these are for module values
        put("nightvision", "Night Vision")
        put("removefire", "Remove fire")
    }

    private fun zh() = buildMap {
        put("fly", "飞行")
        put("no_clip", "穿墙")
        put("zoom", "缩放")
        put("air_jump",  "空中跳跃")
        put("speed", "速度")
        put("full_bright", "夜视")
        put("haste", "极速")
        put("jetpack", "喷气背包")
        put("levitation", "漂浮")
        put("high_jump", "高跳")
        put("slow_fall", "缓慢下落")
        put("anti_knockback","防击退")
        put("poseidon","海神")
        put("regen","再生")
        put("auto_jump","自动跳跃")
        put("sprint","疾跑")
        put("no_hurt_camera","无伤害抖动")

        // Below these are for module values
        put("nightvision", "夜视")
        put("removefire", "移除火焰")
    }

    fun getTranslationMap(language: String): Map<String, String> {
        return map[language] ?: map["en"]!!
    }

}
