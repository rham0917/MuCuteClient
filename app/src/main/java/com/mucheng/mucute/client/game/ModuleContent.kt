package com.mucheng.mucute.client.game

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFilter
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.zIndex
import com.mucheng.mucute.client.R
import com.mucheng.mucute.client.overlay.OverlayManager
import com.mucheng.mucute.client.util.translatedSelf

@Composable
fun ModuleContent(moduleCategory: ModuleCategory) {
    val modules = ModuleManager
        .modules
        .fastFilter { it.category === moduleCategory }

    LazyColumn(
        Modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(modules.size) {
            val module = modules[it]
            ModuleCard(module)
        }
    }
}

@Composable
private fun ModuleCard(module: Module) {
    val values = module.values

    OutlinedCard(
        onClick = {
            module.isExpanded = !module.isExpanded
        },
        modifier = Modifier
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    ) {
        Column(
            Modifier
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(10.dp)
            ) {
                Text(
                    module.name.translatedSelf,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                )
                Spacer(Modifier.weight(1f))
                Switch(
                    checked = module.isEnabled,
                    onCheckedChange = {
                        module.isEnabled = it
                    },
                    modifier = Modifier
                        .width(52.dp)
                        .height(32.dp)
                )
            }
            if (module.isExpanded) {
                values.fastForEach {
                    when (it) {
                        is BoolValue -> BoolValueContent(module, it)
                    }
                }
                ShortcutContent(module)
            }
        }
    }
}

@Composable
private fun BoolValueContent(module: Module, value: BoolValue) {

}

@Composable
private fun ShortcutContent(module: Module) {
    Row(
        Modifier
            .padding(start = 10.dp, end = 10.dp, bottom = 10.dp)
            .toggleable(
                value = module.isShortcutDisplayed,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onValueChange = {
                    module.isShortcutDisplayed = it
                    if (it) {
                        OverlayManager.showOverlayWindow(module.overlayShortcutButton)
                    } else {
                        OverlayManager.dismissOverlayWindow(module.overlayShortcutButton)
                    }
                }
            )
    ) {
        Text(
            stringResource(R.string.shortcut),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.weight(1f))
        Checkbox(
            checked = module.isShortcutDisplayed,
            onCheckedChange = null,
            modifier = Modifier
                .padding(0.dp)
        )
    }
}