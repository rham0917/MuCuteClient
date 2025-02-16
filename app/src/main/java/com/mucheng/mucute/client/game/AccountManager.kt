package com.mucheng.mucute.client.game

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.gson.JsonParser
import com.mucheng.mucute.client.application.AppContext
import com.mucheng.mucute.relay.util.AuthUtils
import com.mucheng.mucute.relay.util.refresh
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.raphimc.minecraftauth.MinecraftAuth
import net.raphimc.minecraftauth.step.bedrock.session.StepFullBedrockSession.FullBedrockSession
import java.io.File

object AccountManager {

    private val coroutineScope =
        CoroutineScope(Dispatchers.IO + CoroutineName("AccountManagerCoroutine"))

    private val _accounts: MutableList<FullBedrockSession> = mutableStateListOf()

    val accounts: List<FullBedrockSession>
        get() = _accounts

    var selectedAccount: FullBedrockSession? by mutableStateOf(null)
        private set

    init {
        val fetchedAccounts = fetchAccounts()

        _accounts.addAll(fetchedAccounts)
        selectedAccount = fetchSelectedAccount()
    }

    fun addAccount(fullBedrockSession: FullBedrockSession) {
        _accounts.add(fullBedrockSession)

        coroutineScope.launch {
            val file = File(AppContext.instance.cacheDir, "accounts")
            file.mkdirs()

            val json = MinecraftAuth.BEDROCK_DEVICE_CODE_LOGIN
                .toJson(fullBedrockSession)
            file.resolve("${fullBedrockSession.mcChain.displayName}.json")
                .writeText(AuthUtils.gson.toJson(json))
        }
    }

    fun containsAccount(fullBedrockSession: FullBedrockSession): Boolean {
        return _accounts.find { it.mcChain.displayName == fullBedrockSession.mcChain.displayName } != null
    }

    fun removeAccount(fullBedrockSession: FullBedrockSession) {
        _accounts.remove(fullBedrockSession)

        coroutineScope.launch {
            val file = File(AppContext.instance.cacheDir, "accounts")
            file.mkdirs()

            file.resolve("${fullBedrockSession.mcChain.displayName}.json")
                .delete()
        }
    }

    fun selectAccount(fullBedrockSession: FullBedrockSession?) {
        this.selectedAccount = fullBedrockSession

        coroutineScope.launch {
            val file = File(AppContext.instance.cacheDir, "accounts")
            file.mkdirs()

            runCatching {
                val selectedAccount = file.resolve("selectedAccount")
                if (fullBedrockSession != null) {
                    selectedAccount.writeText(fullBedrockSession.mcChain.displayName)
                } else {
                    selectedAccount.delete()
                }
            }
        }
    }

    private fun fetchAccounts(): List<FullBedrockSession> {
        val file = File(AppContext.instance.cacheDir, "accounts")
        file.mkdirs()

        val accounts = ArrayList<FullBedrockSession>()
        val listFiles = file.listFiles() ?: emptyArray()
        for (child in listFiles) {
            runCatching {
                if (child.isFile && child.extension == "json") {
                    val account = MinecraftAuth.BEDROCK_DEVICE_CODE_LOGIN
                        .fromJson(JsonParser.parseString(child.readText()).asJsonObject)
                    accounts.add(account)
                }
            }
        }

        return accounts
    }

    private fun fetchSelectedAccount(): FullBedrockSession? {
        val file = File(AppContext.instance.cacheDir, "accounts")
        file.mkdirs()

        val selectedAccount = file.resolve("selectedAccount")
        if (!selectedAccount.exists() || selectedAccount.isDirectory) {
            return null
        }

        val displayName = selectedAccount.readText()
        return accounts.find { it.mcChain.displayName == displayName }
    }

}