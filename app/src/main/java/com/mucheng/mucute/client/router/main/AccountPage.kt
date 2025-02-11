package com.mucheng.mucute.client.router.main

import android.annotation.SuppressLint
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.DialogProperties
import com.mucheng.mucute.client.R
import com.mucheng.mucute.client.game.AccountManager
import com.mucheng.mucute.client.model.Account
import com.mucheng.mucute.client.util.AuthWebView
import com.mucheng.mucute.client.util.LocalSnackbarHostState
import com.mucheng.mucute.client.util.SnackbarHostStateScope
import com.mucheng.mucute.client.util.getActivityWindow
import com.mucheng.mucute.client.util.getDialogWindow
import com.mucheng.mucute.client.util.windowFullScreen
import com.mucheng.mucute.relay.util.XboxDeviceInfo
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountPageContent() {
    SnackbarHostStateScope {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        var showAddAccountDropDownMenu by remember { mutableStateOf(false) }
        var selectedAccountAction: Account? by remember { mutableStateOf(null) }
        var deviceInfo: XboxDeviceInfo? by remember { mutableStateOf(null) }
        val snackbarHostState = LocalSnackbarHostState.current

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(stringResource(R.string.account))
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                showAddAccountDropDownMenu = true
                            }
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = null)
                        }
                        AddAccountDropDownMenu(
                            expanded = showAddAccountDropDownMenu,
                            onClick = {
                                deviceInfo = it
                                showAddAccountDropDownMenu = false
                            }
                        ) {
                            showAddAccountDropDownMenu = false
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        titleContentColor = contentColorFor(MaterialTheme.colorScheme.surfaceContainer)
                    )
                )
            },
            bottomBar = {
                SnackbarHost(
                    snackbarHostState,
                    modifier = Modifier
                        .animateContentSize()
                )
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Box(
                Modifier
                    .padding(it)
                    .fillMaxSize()
            ) {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(AccountManager.accounts, key = { value -> value.remark }) { account ->
                        ListItem(
                            modifier = Modifier
                                .animateItem()
                                .clickable {
                                    if (AccountManager.currentAccount != account) {
                                        AccountManager.selectAccount(account)
                                    } else {
                                        AccountManager.selectAccount(null)
                                    }
                                },
                            colors = ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            ),
                            headlineContent = {
                                Text(account.remark)
                            },
                            supportingContent = {
                                Row(Modifier.fillMaxWidth()) {
                                    Text(account.platform.deviceType)
                                    if (account == AccountManager.currentAccount) {
                                        Text(
                                            stringResource(R.string.has_been_selected),
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                    }
                                }
                            },
                            trailingContent = {
                                IconButton(
                                    onClick = {
                                        selectedAccountAction = account
                                    }
                                ) {
                                    Icon(Icons.Rounded.MoreVert, contentDescription = null)
                                }

                                DropdownMenu(
                                    expanded = selectedAccountAction == account,
                                    onDismissRequest = { selectedAccountAction = null }
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                if (account == AccountManager.currentAccount) stringResource(
                                                    R.string.unselect
                                                ) else stringResource(
                                                    R.string.select
                                                )
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                if (account == AccountManager.currentAccount) Icons.Outlined.CheckBox else Icons.Outlined.CheckBoxOutlineBlank,
                                                contentDescription = null
                                            )
                                        },
                                        onClick = {
                                            if (AccountManager.currentAccount != account) {
                                                AccountManager.selectAccount(account)
                                            } else {
                                                AccountManager.selectAccount(null)
                                            }
                                            selectedAccountAction = null
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Text(stringResource(R.string.delete))
                                        },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Outlined.DeleteOutline,
                                                contentDescription = null
                                            )
                                        },
                                        onClick = {
                                            AccountManager.accounts.remove(account)
                                            if (account == AccountManager.currentAccount) {
                                                AccountManager.selectAccount(null)
                                            }
                                            AccountManager.save()
                                            selectedAccountAction = null
                                        }
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }

        deviceInfo?.let {
            AccountDialog(it) { success ->
                deviceInfo = null
                coroutineScope.launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar(
                        context.getString(if (success) R.string.fetch_account_successfully else R.string.fetch_account_failed)
                    )
                }
            }
        }

    }
}

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountDialog(
    deviceInfo: XboxDeviceInfo,
    callback: (success: Boolean) -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = {},
        properties = DialogProperties(
            usePlatformDefaultWidth = true,
            decorFitsSystemWindows = false
        )
    ) {
        val activityWindow = getActivityWindow()
        val dialogWindow = getDialogWindow()

        SideEffect {
            windowFullScreen(activityWindow, dialogWindow)
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(stringResource(R.string.add_account))
                    }
                )
            }
        ) {
            Column(
                Modifier
                    .padding(it)
                    .fillMaxSize()
            ) {
                AndroidView(
                    factory = { context ->
                        AuthWebView(context).also { authWebView ->
                            authWebView.deviceInfo = deviceInfo
                            authWebView.callback = callback
                        }.also { authWebView ->
                            authWebView.addAccount()
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun AddAccountDropDownMenu(
    expanded: Boolean,
    onClick: (XboxDeviceInfo) -> Unit,
    onDismissRequest: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest
    ) {
        XboxDeviceInfo.devices.values.forEach { deviceInfo ->
            DropdownMenuItem(
                text = {
                    Text(stringResource(R.string.login_in, deviceInfo.deviceType))
                },
                onClick = {
                    onClick(deviceInfo)
                }
            )
        }
    }
}