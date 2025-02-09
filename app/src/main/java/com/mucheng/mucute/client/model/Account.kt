package com.mucheng.mucute.client.model

import com.google.gson.annotations.SerializedName
import com.mucheng.mucute.client.game.AccountManager
import com.mucheng.mucute.relay.util.XboxDeviceInfo

class Account(
    @SerializedName("remark") var remark: String,
    @SerializedName("device") val platform: XboxDeviceInfo,
    @SerializedName("refresh_token") var refreshToken: String
) {

    /**
     * @return accessToken
     */
    fun refresh(): String {
        val isCurrent = AccountManager.currentAccount == this
        val (accessToken, refreshToken) = platform.refreshToken(refreshToken)
        this.refreshToken = refreshToken
        if (isCurrent) {
            // refreshes the token field
            AccountManager.selectAccount(this)
        }
        AccountManager.save()
        return accessToken
    }
}