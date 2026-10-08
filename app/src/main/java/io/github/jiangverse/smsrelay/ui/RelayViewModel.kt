package io.github.jiangverse.smsrelay.ui

import androidx.compose.ui.res.stringResource
import io.github.jiangverse.smsrelay.R
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.jiangverse.smsrelay.RelayApplication
import io.github.jiangverse.smsrelay.domain.RelaySettings
import io.github.jiangverse.smsrelay.domain.SmsEnvelope
import io.github.jiangverse.smsrelay.forwarding.RelayScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class RelayViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as RelayApplication
    val settings = app.store.settings
    val records = app.repository.records
    val hookSeen = app.store.hookSeen
    fun save(value: RelaySettings) { app.store.update(value.copy(sendKey = value.sendKey.trim(), title = value.title.trim())) }
    fun test(value: RelaySettings) {
        save(value)
        viewModelScope.launch(Dispatchers.IO) {
            val id = "test-${UUID.randomUUID()}"
            app.repository.accept(SmsEnvelope("SmsRelay", app.getString(R.string.test_body), System.currentTimeMillis(), -1), id)
            RelayScheduler.enqueue(app, id, test = true)
        }
    }
    fun retry(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            app.repository.update(id, app.getString(R.string.pending))
            RelayScheduler.enqueue(app, id, test = id.startsWith("test-"))
        }
    }
}
