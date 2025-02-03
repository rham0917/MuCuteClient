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
        put("full_bright", "Full Bright")
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
        put("no_hurt_cam","No Hurt Camera")
        put("random_move","Anti AFK")
        put("auto_walk","Auto Walk")
        put("desync","DeSync")
        put("whereisit","Entity Tracer")


    }

    private fun zh() = buildMap {
        put("fly", "飞行")
        put("no_clip", "穿墙")
        put("zoom", "缩放")
        put("air_jump",  "空中跳跃")
        put("speed", "速度")
        put("full_bright", "夜视")
        put("haste", "急迫")
        put("jetpack", "喷气背包")
        put("levitation", "漂浮")
        put("high_jump", "高跳")
        put("slow_fall", "缓慢下落")
        put("anti_knockback","防击退")
        put("poseidon","海神")
        put("regen","再生")
        put("auto_jump","自动跳跃")
        put("sprint","疾跑")
        put("no_hurt_cam","无伤害相机")
        put("random_move","随机移动")
        put("auto_walk","自动行走")
        put("desync","异步发包")
        put("whereisit","实体追踪器")
    }

    fun getTranslationMap(language: String): Map<String, String> {
        return map[language] ?: map["en"]!!
    }

}
