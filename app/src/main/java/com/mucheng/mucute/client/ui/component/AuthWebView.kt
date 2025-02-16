package com.mucheng.mucute.client.ui.component

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.mucheng.mucute.client.game.AccountManager
import com.mucheng.mucute.relay.util.authorize
import net.raphimc.minecraftauth.MinecraftAuth
import net.raphimc.minecraftauth.step.bedrock.session.StepFullBedrockSession.FullBedrockSession
import net.raphimc.minecraftauth.step.msa.StepMsaDeviceCode
import kotlin.concurrent.thread

@SuppressLint("SetJavaScriptEnabled")
class AuthWebView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : WebView(context, attrs) {

    var callback: ((Throwable?) -> Unit)? = null

    init {
        CookieManager.getInstance()
            .removeAllCookies(null)

        settings.javaScriptEnabled = true
        webViewClient = AuthWebViewClient()
    }

    fun addAccount() {
        thread {
            runCatching {
                val fullBedrockSession = authorize(
                    cache = false,
                    msaDeviceCodeCallback = StepMsaDeviceCode.MsaDeviceCodeCallback {
                        post {
                            loadUrl(it.directVerificationUri)
                        }
                    }
                )
                val containedAccount =
                    AccountManager.accounts.find { it.mcChain.displayName == fullBedrockSession.mcChain.displayName }
                if (containedAccount != null) {
                    AccountManager.removeAccount(containedAccount)
                }
                AccountManager.addAccount(fullBedrockSession)

                if (containedAccount == AccountManager.selectedAccount) {
                    AccountManager.selectAccount(fullBedrockSession)
                }
                callback?.invoke(null)
            }.exceptionOrNull()?.let {
                callback?.invoke(it)
            }
        }
    }

    inner class AuthWebViewClient : WebViewClient() {

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            return false
        }

    }

}
