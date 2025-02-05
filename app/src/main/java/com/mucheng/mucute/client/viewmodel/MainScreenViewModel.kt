package com.mucheng.mucute.client.viewmodel

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import androidx.compose.ui.util.fastFilter
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonParser
import com.mucheng.mucute.client.application.AppContext
import com.mucheng.mucute.client.model.CaptureModeModel
import com.mucheng.mucute.client.router.main.MainScreenPages
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.lenni0451.commons.httpclient.HttpClient
import net.raphimc.minecraftauth.MinecraftAuth
import net.raphimc.minecraftauth.step.bedrock.session.StepFullBedrockSession.FullBedrockSession
import net.raphimc.minecraftauth.step.msa.StepMsaDeviceCode.MsaDeviceCodeCallback

class MainScreenViewModel : ViewModel() {

    enum class PackageInfoState {
        Loading, Done
    }

    private val gameSettingsSharedPreferences by lazy {
        AppContext.instance.getSharedPreferences("game_settings", Context.MODE_PRIVATE)
    }

    private val _selectedPage = MutableStateFlow(MainScreenPages.HomePage)

    private val _captureModeModel = MutableStateFlow(initialCaptureModeModel())

    private val _packageInfos = MutableStateFlow<List<PackageInfo>>(emptyList())

    private val _packageInfoState = MutableStateFlow(PackageInfoState.Loading)

    private val _selectedGame = MutableStateFlow(initialSelectedGame())

    private val _accounts =
        MutableStateFlow<List<FullBedrockSession>>(emptyList())

    private val _selectedAccount = MutableStateFlow(initialSelectedAccount())

    val selectedPage = _selectedPage.asStateFlow()

    val captureModeModel = _captureModeModel.asStateFlow()

    val packageInfos = _packageInfos.asStateFlow()

    val packageInfoState = _packageInfoState.asStateFlow()

    val selectedGame = _selectedGame.asStateFlow()

    val accounts = _accounts.asStateFlow()

    val selectedAccount = _selectedAccount.asStateFlow()

    fun selectPage(page: MainScreenPages) {
        _selectedPage.value = page
    }

    fun selectCaptureModeModel(captureModeModel: CaptureModeModel) {
        _captureModeModel.value = captureModeModel
        captureModeModel.to(gameSettingsSharedPreferences)
    }

    fun selectGame(packageName: String?) {
        _selectedGame.value = packageName
        gameSettingsSharedPreferences.edit {
            putString("selected_game", packageName)
        }
    }

    fun fetchPackageInfos() {
        viewModelScope.launch(Dispatchers.IO) {
            _packageInfoState.value = PackageInfoState.Loading
            try {
                val packageManager = AppContext.instance.packageManager
                val packageInfos = packageManager.getInstalledPackages(0)
                    .fastFilter {
                        it.applicationInfo!!.flags and ApplicationInfo.FLAG_SYSTEM == 0
                    }
                    .fastFilter {
                        it.applicationInfo != null
                    }
                _packageInfos.value = packageInfos
            } finally {
                _packageInfoState.value = PackageInfoState.Done
            }
        }
    }

    fun fetchXboxToken(
        codeCallback: MsaDeviceCodeCallback,
        onAuthSession: (FullBedrockSession?, Throwable?) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val client: HttpClient = MinecraftAuth.createHttpClient()
                val authSession =
                    MinecraftAuth.BEDROCK_DEVICE_CODE_LOGIN.getFromInput(client, codeCallback)
                onAuthSession(authSession, null)
            } catch (e: Throwable) {
                onAuthSession(null, e)
                if (e is CancellationException) throw e
            }
        }
    }

    fun fetchXboxTokens() {
        viewModelScope.launch(Dispatchers.IO) {
            val accountDir = AppContext.instance.filesDir.resolve("accounts")
            if (!accountDir.exists() || accountDir.isFile) return@launch

            val accounts = (accountDir.listFiles() ?: emptyArray())
                .filter { it.isFile && it.extension == "json" }
                .sortedByDescending { it.lastModified() }
                .map {
                    MinecraftAuth.BEDROCK_DEVICE_CODE_LOGIN.fromJson(JsonParser.parseString(it.readText()).asJsonObject)
                }
                .toList()

            _accounts.value = accounts
        }
    }

    fun addXboxToken(authSession: FullBedrockSession) {
        viewModelScope.launch(Dispatchers.IO) {
            _accounts.update {
                listOf(authSession) + it
            }

            val json = MinecraftAuth.BEDROCK_DEVICE_CODE_LOGIN.toJson(authSession).toString()
            val accountDir = AppContext.instance.filesDir.resolve("accounts")
            accountDir.mkdirs()

            val authFile = accountDir.resolve(authSession.mcChain.displayName + ".json")
            authFile.writer().use {
                it.write(json)
            }
        }
    }

    fun removeXboxToken(authSession: FullBedrockSession) {
        viewModelScope.launch(Dispatchers.IO) {
            val xboxTokens = _accounts.value.toMutableList()
            xboxTokens.remove(authSession)

            _accounts.value = xboxTokens

            val accountDir = AppContext.instance.filesDir.resolve("accounts")
            if (!accountDir.exists() || accountDir.isFile) return@launch

            val authFile = accountDir.resolve("${authSession.mcChain.displayName}.json")
            if (authFile.exists()) {
                authFile.deleteRecursively()
            }
        }
    }

    fun selectAccount(account: FullBedrockSession?) {
        _selectedAccount.value = account

        gameSettingsSharedPreferences.edit {
            putString("selected_account", account?.mcChain?.displayName)
        }
    }

    private fun initialCaptureModeModel(): CaptureModeModel {
        return CaptureModeModel.from(gameSettingsSharedPreferences)
    }

    private fun initialSelectedGame(): String? {
        return gameSettingsSharedPreferences.getString("selected_game", null)
    }

    private fun initialSelectedAccount(): FullBedrockSession? {
        val account = gameSettingsSharedPreferences.getString("selected_account", null)
        if (account != null) {
            val accountDir = AppContext.instance.filesDir.resolve("accounts")
            if (!accountDir.exists() || accountDir.isFile) return null

            val authFile = accountDir.resolve("${account}.json")
            if (!authFile.exists()) return null

            return MinecraftAuth.BEDROCK_DEVICE_CODE_LOGIN.fromJson(JsonParser.parseString(authFile.readText()).asJsonObject)
        } else {
            return null
        }
    }

}