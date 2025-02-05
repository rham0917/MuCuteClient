package com.mucheng.mucute.client.model

import android.content.SharedPreferences
import com.google.gson.JsonParser
import com.mucheng.mucute.client.application.AppContext
import net.raphimc.minecraftauth.MinecraftAuth
import net.raphimc.minecraftauth.step.bedrock.session.StepFullBedrockSession

data class GameSettingsModel(
    val captureModeModel: CaptureModeModel,
    val selectedGame: String,
    val selectedAccount: StepFullBedrockSession.FullBedrockSession?
) {

    companion object {

        fun from(sharedPreferences: SharedPreferences): GameSettingsModel {
            val captureModeModel = CaptureModeModel.from(sharedPreferences)
            val selectedGame = fetchSelectedGame(sharedPreferences)
            val selectedAccount = fetchSelectedAccount(sharedPreferences)
            return GameSettingsModel(captureModeModel, selectedGame, selectedAccount)
        }

        private fun fetchSelectedGame(sharedPreferences: SharedPreferences): String {
            return sharedPreferences.getString("selected_game", "")!!
        }

        private fun fetchSelectedAccount(sharedPreferences: SharedPreferences): StepFullBedrockSession.FullBedrockSession? {
            val account = sharedPreferences.getString("selected_account", null)
            if (account != null) {
                val accountDir = AppContext.instance.filesDir.resolve("accounts")
                if (!accountDir.exists() || accountDir.isFile) return null

                val authFile = accountDir.resolve("${account}.json")
                if (!authFile.exists()) return null

                return MinecraftAuth.BEDROCK_DEVICE_CODE_LOGIN.fromJson(
                    JsonParser.parseString(
                        authFile.readText()
                    ).asJsonObject
                )
            } else {
                return null
            }
        }

    }

}