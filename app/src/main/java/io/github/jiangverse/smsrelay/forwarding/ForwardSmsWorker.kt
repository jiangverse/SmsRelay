package io.github.jiangverse.smsrelay.forwarding
import io.github.jiangverse.smsrelay.R

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.github.jiangverse.smsrelay.RelayApplication
import io.github.jiangverse.smsrelay.domain.ChannelType
import io.github.jiangverse.smsrelay.domain.SendResult

class ForwardSmsWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as RelayApplication
        val id = inputData.getString("id") ?: return Result.failure()
        val record = app.repository.get(id) ?: return Result.failure()
        if (record.state == applicationContext.getString(R.string.delivered)) return Result.success()
        val settings = app.store.settings.value
        if (!settings.enabled && !inputData.getBoolean("test", false)) {
            app.repository.update(id, applicationContext.getString(R.string.skipped), applicationContext.getString(R.string.disabled))
            return Result.success()
        }
        val channel = when (settings.channel) { ChannelType.SERVER_CHAN -> ServerChanChannel(applicationContext) }
        return when (val result = channel.send(settings, record.sms)) {
            SendResult.Success -> { app.repository.update(id, applicationContext.getString(R.string.delivered)); Result.success() }
            is SendResult.Failure -> {
                val retry = result.retryable && runAttemptCount < 4
                app.repository.update(id, if (retry) applicationContext.getString(R.string.retrying) else applicationContext.getString(R.string.failed), result.message)
                if (retry) Result.retry() else Result.failure()
            }
        }
    }
}
