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
        put("air_jump",  "AirJump")
        put("speed_hack", "Speed Boost")
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
        put("auto_sprint","Auto Sprint")

    }

    private fun zh() = buildMap {
        put("fly", "飞行")
        put("no_clip", "穿墙")
        put("zoom", "缩放")
        put("air_jump",  "空中跳跃")
        put("speed_hack", "速度")
        put("full_bright", "夜视")
        put("haste", "快攻")
        put("jetpack", "喷气背包")
        put("levitate", "悬浮")
        put("high_jump", "高跳")
        put("slow_fall", "缓慢下落")
        put("anti_knockback","防击退")
        put("poseidon","海神")
        put("regen","再生")
        put("auto_jump","兔子跳")
        put("auto_sprint","自动运行")
    }

    fun getTranslationMap(language: String): Map<String, String> {
        return map[language] ?: map["en"]!!
    }

}
