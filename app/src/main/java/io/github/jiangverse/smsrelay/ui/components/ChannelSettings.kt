package io.github.jiangverse.smsrelay.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import io.github.jiangverse.smsrelay.domain.RelaySettings
import io.github.jiangverse.smsrelay.domain.ForwardType
import io.github.jiangverse.smsrelay.domain.TitleMode
import androidx.compose.ui.res.stringResource
import io.github.jiangverse.smsrelay.R

@Composable
fun ChannelSettings(settings: RelaySettings, onSave: (RelaySettings) -> Unit, onTest: (RelaySettings) -> Unit) {
    // 密钥仅保留在内存中，不写入界面状态恢复数据。
    var key by remember(settings.sendKey) { mutableStateOf(settings.sendKey) }
    var title by remember(settings.title) { mutableStateOf(settings.title) }
    var prefix by remember(settings.titlePrefix) { mutableStateOf(settings.titlePrefix) }
    var suffix by remember(settings.titleSuffix) { mutableStateOf(settings.titleSuffix) }
    var enabled by remember(settings.enabled) { mutableStateOf(settings.enabled) }
    var visible by remember { mutableStateOf(false) }
    var forwardType by remember(settings.forwardType) { mutableStateOf(settings.forwardType) }
    var titleMode by remember(settings.titleMode) { mutableStateOf(settings.titleMode) }
    var notice by remember { mutableIntStateOf(0) }
    val valid = key.trim().matches(Regex("SCT[A-Za-z0-9]+")) && (titleMode == TitleMode.DEFAULT || (title.isNotBlank() && title.length <= 32))
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        HorizontalDivider()
        Text(stringResource(R.string.forward_type), style = MaterialTheme.typography.titleMedium)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ForwardType.entries.forEach { type ->
                FilterChip(selected = forwardType == type, onClick = { forwardType = type }, label = { Text(stringResource(when (type) { ForwardType.ALL -> R.string.all_sms; ForwardType.OTP -> R.string.otp_sms; ForwardType.PICKUP -> R.string.pickup_sms })) })
            }
        }
        Text(stringResource(R.string.forward_method), style = MaterialTheme.typography.titleMedium)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(R.string.auto_forward), style = MaterialTheme.typography.titleMedium)
            Switch(enabled, { enabled = it; notice = 0 })
        }
        Text(stringResource(R.string.push_channel), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        var expanded by remember { mutableStateOf(false) }
        Box {
            OutlinedButton({ expanded = true }, Modifier.fillMaxWidth()) { Text(stringResource(R.string.server_chan)) }
            DropdownMenu(expanded, { expanded = false }) {
                DropdownMenuItem(text = { Text(stringResource(R.string.server_chan)) }, onClick = { expanded = false })
            }
        }
        OutlinedTextField(key, { key = it; notice = 0 }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.send_key)) }, singleLine = true,
            visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = { IconButton({ visible = !visible }) { Icon(if (visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility, if (visible) stringResource(R.string.hide_key) else stringResource(R.string.show_key)) } },
            isError = key.isNotEmpty() && !key.trim().matches(Regex("SCT[A-Za-z0-9]+")))
        Text(stringResource(R.string.title_method), style = MaterialTheme.typography.titleMedium)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TitleMode.entries.forEach { mode -> FilterChip(selected = titleMode == mode, onClick = { titleMode = mode }, label = { Text(stringResource(if (mode == TitleMode.DEFAULT) R.string.default_title else R.string.custom_title_mode)) }) }
        }
        if (titleMode == TitleMode.DEFAULT) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(prefix, { prefix = it.take(8) }, Modifier.weight(1f), label = { Text(stringResource(R.string.title_prefix)) }, singleLine = true)
                OutlinedTextField(suffix, { suffix = it.take(8) }, Modifier.weight(1f), label = { Text(stringResource(R.string.title_suffix)) }, singleLine = true)
            }
        }
        if (titleMode == TitleMode.CUSTOM) OutlinedTextField(title, { title = it; notice = 0 }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.custom_title)) }, singleLine = true,
            isError = title.length > 32, supportingText = { Text(stringResource(R.string.title_count, title.length)) })
        Button({ onSave(settings.copy(enabled = enabled, sendKey = key, title = title, titlePrefix = prefix, titleSuffix = suffix, forwardType = forwardType, titleMode = titleMode)); notice = R.string.saved },
            Modifier.fillMaxWidth(), enabled = valid || !enabled) {
            Icon(Icons.Outlined.Save, null); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.save_config))
        }
        OutlinedButton({ onTest(settings.copy(enabled = enabled, sendKey = key, title = title, titlePrefix = prefix, titleSuffix = suffix, forwardType = forwardType, titleMode = titleMode)); notice = R.string.test_queued }, Modifier.fillMaxWidth(), enabled = valid) {
            Icon(Icons.Outlined.Send, null); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.test_push))
        }
        if (notice != 0) Text(stringResource(notice), color = MaterialTheme.colorScheme.primary)
        Text(stringResource(R.string.privacy), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        // 项目入口紧随隐私说明，作为设置表单的一部分一起滚动。
        ProjectFooter()
    }
}
