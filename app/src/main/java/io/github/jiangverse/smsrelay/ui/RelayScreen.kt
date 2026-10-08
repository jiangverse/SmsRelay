package io.github.jiangverse.smsrelay.ui

import androidx.compose.ui.res.stringResource
import io.github.jiangverse.smsrelay.R
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudQueue
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.jiangverse.smsrelay.ui.components.ChannelSettings
import io.github.jiangverse.smsrelay.ui.components.MessageRow
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelayScreen(model: RelayViewModel) {
    val settings by model.settings.collectAsStateWithLifecycle()
    val records by model.records.collectAsStateWithLifecycle()
    val hookSeen by model.hookSeen.collectAsStateWithLifecycle()
    var tab by remember { mutableIntStateOf(0) }
    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.screen_title), style = MaterialTheme.typography.titleLarge) }) },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(tab == 0, { tab = 0 }, { Icon(Icons.Outlined.Settings, null) }, label = { Text(stringResource(R.string.settings_tab)) })
                NavigationBarItem(tab == 1, { tab = 1 }, { Icon(Icons.Outlined.History, null) }, label = { Text(stringResource(R.string.history_tab)) })
            }
        }) { padding ->
        val status: @Composable () -> Unit = {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Outlined.CloudQueue, null, tint = MaterialTheme.colorScheme.primary)
                    Column {
                        Text(if (settings.enabled) stringResource(R.string.enabled) else stringResource(R.string.disabled), style = MaterialTheme.typography.titleMedium)
                        Text(if (hookSeen == 0L) stringResource(R.string.no_handshake) else stringResource(R.string.last_hook, DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(hookSeen))),
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
        }
        if (tab == 0) {
            // 设置表单包含隐私说明和项目入口，共用同一个滚动容器。
            Column(
                modifier = Modifier.fillMaxSize().padding(padding)
                    .verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                status()
                ChannelSettings(settings, model::save, model::test)
            }
        } else {
            // 历史列表按需加载。
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { status() }
                item { Text(stringResource(R.string.recent), style = MaterialTheme.typography.titleMedium) }
                if (records.isEmpty()) item { Text(stringResource(R.string.empty_records), color = MaterialTheme.colorScheme.onSurfaceVariant) }
                items(records, key = { it.id }) { MessageRow(it, onRetry = { model.retry(it.id) }) }
            }
        }
    }
}
