package io.github.jiangverse.smsrelay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.jiangverse.smsrelay.ui.RelayScreen
import io.github.jiangverse.smsrelay.ui.RelayViewModel
import io.github.jiangverse.smsrelay.ui.theme.SmsRelayTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmsRelayTheme {
                val model: RelayViewModel = viewModel()
                RelayScreen(model)
            }
        }
    }
}
