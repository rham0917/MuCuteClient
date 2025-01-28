package com.mucheng.mucute.client.activity

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mucheng.mucute.client.navigation.Navigation
import com.mucheng.mucute.client.ui.theme.MuCuteClientTheme


class MainActivity : ComponentActivity() {

    @SuppressLint("BatteryLife")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MuCuteClientTheme {
                Navigation()
            }
        }
    }

}