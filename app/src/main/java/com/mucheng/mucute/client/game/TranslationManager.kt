package com.mucheng.mucute.client.game

object TranslationManager {

    private val map = HashMap<String, Map<String, String>>()

    init {
        map["en"] = en()
        map["zh"] = zh()
    }

    private fun en() = buildMap {
        put("fly", "Fly")
        put("speed", "Speed")
        put("no_clip", "No Clip")
        put("zoom", "Zoom")
        put("air_jump",  "AirJump")
        put("speed_hack", "Speed Boost")
        put("full_bright", "Fullbright")
        put("haste", "Haste")

    }

    private fun zh() = buildMap {
        put("fly", "飞行")
        put("speed", "速度")
        put("no_clip", "穿墙")
        put("zoom", "缩放")
        put("air_jump",  "空中跳跃")
        put("speed_hack", "提升速度")
        put("full_bright", "夜视仪")
        put("haste", "快攻")
    }

    fun getTranslationMap(language: String): Map<String, String> {
        return map[language] ?: map["en"]!!
    }

}