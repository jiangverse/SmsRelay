package io.github.jiangverse.smsrelay.forwarding

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object RelayScheduler {
    fun enqueue(context: Context, id: String, test: Boolean = false) {
        val request = OneTimeWorkRequestBuilder<ForwardSmsWorker>()
            .setInputData(Data.Builder().putString("id", id).putBoolean("test", test).build())
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS).build()
        WorkManager.getInstance(context).enqueueUniqueWork("sms-$id", ExistingWorkPolicy.KEEP, request)
    }
}
