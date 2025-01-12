package com.mucheng.mucute.client.util

import android.content.SharedPreferences
import androidx.core.content.edit

enum class WorkModes {

    CaptureMode, ProxyMode;

    companion object {

        fun from(sharedPreferences: SharedPreferences): WorkModes {
            return WorkModes.valueOf(sharedPreferences.getString("work_mode", CaptureMode.name)!!)
        }

    }

    fun to(sharedPreferences: SharedPreferences) {
        sharedPreferences.edit {
            putString("work_mode", name)
        }
    }

}