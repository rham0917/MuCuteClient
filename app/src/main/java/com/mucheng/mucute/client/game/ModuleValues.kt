package com.mucheng.mucute.client.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlin.reflect.KProperty

interface Configurable {

    val values: MutableList<Value<*>>

    fun getValue(name: String) = values.find { it.name == name }

    fun boolValue(name: String, value: Boolean) = BoolValue(name, value).also { values.add(it) }

}

@Suppress("MemberVisibilityCanBePrivate")
sealed class Value<T>(val name: String, val defaultValue: T) {

    var value: T by mutableStateOf(defaultValue)

    open fun reset() {
        value = defaultValue
    }

    operator fun getValue(from: Any, property: KProperty<*>): T {
        return value
    }

    operator fun setValue(from: Any, property: KProperty<*>, newValue: T) {
        value = newValue
    }

    abstract fun toJson(): JsonElement

    abstract fun fromJson(element: JsonElement)

}

class BoolValue(name: String, defaultValue: Boolean) : Value<Boolean>(name, defaultValue) {

    override fun toJson() = JsonPrimitive(value)

    override fun fromJson(element: JsonElement) {
        if (element is JsonPrimitive) {
            value = element.boolean
        }
    }

}