package com.mucheng.mucute.client.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.mucheng.mucute.client.navigation.Navigation
import com.mucheng.mucute.client.ui.theme.MuCuteClientTheme


class MainActivity : ComponentActivity() {

    @SuppressLint("BatteryLife")
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            MuCuteClientTheme {
                Navigation()
            }
        }
    }

}