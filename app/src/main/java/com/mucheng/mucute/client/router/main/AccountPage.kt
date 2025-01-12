package com.mucheng.mucute.client.router.main

import android.annotation.SuppressLint
import android.graphics.Color
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mucheng.mucute.client.R
import com.mucheng.mucute.client.util.LocalSnackbarHostState
import com.mucheng.mucute.client.util.SnackbarHostStateScope
import com.mucheng.mucute.client.util.getActivityWindow
import com.mucheng.mucute.client.util.getDialogWindow
import com.mucheng.mucute.client.util.windowFullScreen
import com.mucheng.mucute.client.viewmodel.MainScreenViewModel
import kotlinx.coroutines.launch
import net.raphimc.minecraftauth.step.msa.StepMsaDeviceCode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountPageContent() {
    SnackbarHostStateScope {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        val mainScreenViewModel: MainScreenViewModel = viewModel()
        val accounts by mainScreenViewModel.accounts.collectAsStateWithLifecycle()
        val selectedAccount by mainScreenViewModel.selectedAccount.collectAsStateWithLifecycle()
        var url by rememberSaveable { mutableStateOf<String?>(null) }
        var showAccountDropdownMenu by remember { mutableStateOf(false) }
        val snackbarHostState = LocalSnackbarHostState.current

        LifecycleEventEffect(Lifecycle.Event.ON_START) {
            mainScreenViewModel.fetchXboxTokens()
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(stringResource(R.string.account))
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
                    items(accounts.size) { index ->
                        val account = accounts[index]

                        ListItem(
                            modifier = Modifier.clickable {
                                if (account != selectedAccount) {
                                    mainScreenViewModel.selectAccount(account)
                                } else {
                                    mainScreenViewModel.selectAccount(null)
                                }
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            ),
                            headlineContent = {
                                Text(account.mcChain.displayName)
                            },
                            supportingContent = {
                                Row(Modifier.fillMaxWidth()) {
                                    Text(stringResource(R.string.android))
                                    if (account == selectedAccount) {
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
                                        showAccountDropdownMenu = true
                                    }
                                ) {
                                    Icon(Icons.Rounded.MoreVert, contentDescription = null)
                                }
                                DropdownMenu(
                                    expanded = showAccountDropdownMenu,
                                    onDismissRequest = { showAccountDropdownMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                if (account == selectedAccount) stringResource(R.string.unselect) else stringResource(
                                                    R.string.select
                                                )
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                if (account == selectedAccount) Icons.Outlined.CheckBox else Icons.Outlined.CheckBoxOutlineBlank,
                                                contentDescription = null
                                            )
                                        },
                                        onClick = {
                                            if (account != selectedAccount) {
                                                mainScreenViewModel.selectAccount(account)
                                            } else {
                                                mainScreenViewModel.selectAccount(null)
                                            }
                                            showAccountDropdownMenu = false
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
                                            if (account == selectedAccount) {
                                                mainScreenViewModel.selectAccount(null)
                                            }
                                            mainScreenViewModel.removeXboxToken(account)
                                            showAccountDropdownMenu = false
                                        }
                                    )
                                }
                            }
                        )
                    }
                }
                FloatingActionButton(
                    onClick = {
                        mainScreenViewModel.fetchXboxToken(
                            codeCallback = StepMsaDeviceCode.MsaDeviceCodeCallback { msaDeviceCode ->
                                Log.e("XBoxToken", "Go to " + msaDeviceCode.verificationUri)
                                Log.e("XBoxToken", "Enter code " + msaDeviceCode.userCode)

                                url = msaDeviceCode.directVerificationUri
                            },
                            onAuthSession = { authSession, throwable ->
                                if (authSession == null) {
                                    coroutineScope.launch {
                                        snackbarHostState.currentSnackbarData?.dismiss()
                                        snackbarHostState.showSnackbar(
                                            message = context.getString(R.string.failed_to_fetch_account, throwable.toString())
                                        )
                                    }
                                    return@fetchXboxToken
                                }

                                mainScreenViewModel.addXboxToken(authSession)
                                url = null
                            },
                        )
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(15.dp)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null)
                }
            }
        }

        if (url != null) {
            AccountDialog(
                url = url!!,
                onDismissRequest = {
                    url = null
                }
            )
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountDialog(
    url: String,
    onDismissRequest: () -> Unit
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
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                onDismissRequest()
                            }
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
                        }
                    }
                )
            }
        ) {
            Column(
                Modifier
                    .padding(it)
                    .fillMaxSize()
            ) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                )
                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            setBackgroundColor(Color.TRANSPARENT)
                            settings.javaScriptEnabled = true
                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(
                                    view: WebView,
                                    request: WebResourceRequest
                                ): Boolean {
                                    view.loadUrl(request.url.toString())
                                    return super.shouldOverrideUrlLoading(view, request)
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize(),
                    update = { webView ->
                        webView.loadUrl(url)
                    }
                )
            }
        }
    }
}