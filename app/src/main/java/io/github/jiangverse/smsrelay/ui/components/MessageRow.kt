package io.github.jiangverse.smsrelay.ui.components

import androidx.compose.ui.res.stringResource
import io.github.jiangverse.smsrelay.R
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.jiangverse.smsrelay.data.RelayRecord
import java.text.DateFormat
import java.util.Date

@Composable
fun MessageRow(record: RelayRecord, onRetry: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(record.sms.sender.ifBlank { stringResource(R.string.unknown_sender) }, Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
            Text(record.state, style = MaterialTheme.typography.labelMedium, color = if (record.state == stringResource(R.string.failed)) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
        }
        Text(record.sms.body, maxLines = 3, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium)
        Text(stringResource(R.string.record_time, DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(record.sms.timestamp)), record.sms.subscription),
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (record.detail.isNotBlank()) Text(record.detail, style = MaterialTheme.typography.bodySmall)
        if (record.state == stringResource(R.string.failed)) TextButton(onRetry) { Icon(Icons.Outlined.Refresh, null); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.retry)) }
        HorizontalDivider(Modifier.padding(top = 8.dp))
    }
}
