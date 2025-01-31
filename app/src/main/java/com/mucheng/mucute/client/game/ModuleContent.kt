package com.mucheng.mucute.client.game

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults.drawStopIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderDefaults.TickSize
import androidx.compose.material3.SliderDefaults.drawStopIndicator
import androidx.compose.material3.SliderDefaults.trackPath
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFilter
import androidx.compose.ui.util.fastForEach
import com.mucheng.mucute.client.R
import com.mucheng.mucute.client.overlay.OverlayManager
import com.mucheng.mucute.client.util.translatedSelf
import java.text.DecimalFormat

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
    val background by animateColorAsState(
        targetValue = if (module.isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background
    )

    Card(
        onClick = {
            module.isExpanded = !module.isExpanded
        },
        modifier = Modifier
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.outlinedCardColors(
            containerColor = background
        )
    ) {
        Column(
            Modifier
                .fillMaxWidth()
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
                    modifier = Modifier,
                    color = if (module.isExpanded) contentColorFor(MaterialTheme.colorScheme.primary) else contentColorFor(
                        MaterialTheme.colorScheme.background
                    )
                )
                Spacer(Modifier.weight(1f))
                Switch(
                    checked = module.isEnabled,
                    onCheckedChange = {
                        module.isEnabled = it
                    },
                    colors = SwitchDefaults.colors(
                        checkedBorderColor = if (module.isExpanded) MaterialTheme.colorScheme.surface else Color.Transparent,
                        uncheckedTrackColor = Color.Transparent,
                        uncheckedBorderColor = if (module.isExpanded) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.outline,
                        uncheckedThumbColor = if (module.isExpanded) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.outline,
                    ),
                    modifier = Modifier
                        .width(52.dp)
                        .height(32.dp)
                )
            }
            if (module.isExpanded) {
                values.fastForEach {
                    when (it) {
                        is BoolValue -> BoolValueContent(module, it)
                        is FloatValue -> FloatValueContent(module, it)
                        is IntValue -> IntValueContent(module, it)
                    }
                }
                ShortcutContent(module)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FloatValueContent(module: Module, value: FloatValue) {
    Column(
        Modifier
            .padding(start = 10.dp, end = 10.dp, bottom = 10.dp)
    ) {
        Row {
            Text(
                value.name.translatedSelf,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.surface
            )
            Spacer(Modifier.weight(1f))
            Text(
                value.value.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.surface
            )
        }
        val colors = SliderDefaults.colors(
            thumbColor = MaterialTheme.colorScheme.surface,
            activeTrackColor = MaterialTheme.colorScheme.surface,
            activeTickColor = MaterialTheme.colorScheme.surface,
            inactiveTickColor = MaterialTheme.colorScheme.surfaceVariant,
            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Slider(
            value = animateFloatAsState(
                targetValue = value.value,
                label = ""
            ).value,
            valueRange = value.range,
            colors = colors,
            track = { sliderState ->
                SliderDefaults.Track(
                    colors = colors,
                    enabled = true,
                    sliderState = sliderState,
                    drawStopIndicator = null
                )
            },
            onValueChange = {
                val newValue = "%.2f".format(it).toFloat()
                if (value.value != newValue) {
                    value.value = newValue
                }
            }
        )
    }
}

@Composable
private fun IntValueContent(module: Module, value: IntValue) {

}

@Composable
private fun BoolValueContent(module: Module, value: BoolValue) {
    Row(
        Modifier
            .padding(start = 10.dp, end = 10.dp, bottom = 10.dp)
            .toggleable(
                value = value.value,
                interactionSource = null,
                indication = null,
                onValueChange = {
                    value.value = it
                }
            )
    ) {
        Text(
            value.name.translatedSelf,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.surface
        )
        Spacer(Modifier.weight(1f))
        Checkbox(
            checked = value.value,
            onCheckedChange = null,
            modifier = Modifier
                .padding(0.dp),
            colors = CheckboxDefaults.colors(
                uncheckedColor = MaterialTheme.colorScheme.surface,
                checkedColor = MaterialTheme.colorScheme.surface,
                checkmarkColor = MaterialTheme.colorScheme.primary
            )
        )
    }
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
            color = MaterialTheme.colorScheme.surface
        )
        Spacer(Modifier.weight(1f))
        Checkbox(
            checked = module.isShortcutDisplayed,
            onCheckedChange = null,
            modifier = Modifier
                .padding(0.dp),
            colors = CheckboxDefaults.colors(
                uncheckedColor = MaterialTheme.colorScheme.surface,
                checkedColor = MaterialTheme.colorScheme.surface,
                checkmarkColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}