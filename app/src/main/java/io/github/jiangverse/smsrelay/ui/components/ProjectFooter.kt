package io.github.jiangverse.smsrelay.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.jiangverse.smsrelay.R

/** 项目入口只在用户点击后打开浏览器，不主动访问 GitHub。 */
@Composable
fun ProjectFooter() {
    val handler = LocalUriHandler.current
    val url = stringResource(R.string.github_url)
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        TextButton(onClick = { handler.openUri(url) }) {
            Icon(painterResource(R.drawable.ic_github), contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.github))
        }
        Text(stringResource(R.string.star_hint), style = MaterialTheme.typography.bodySmall)
        Text(stringResource(R.string.license_label), style = MaterialTheme.typography.labelSmall)
    }
}
