package io.github.jiangverse.smsrelay

import android.app.Application
import io.github.jiangverse.smsrelay.data.RelayStore
import io.github.jiangverse.smsrelay.data.SmsRepository
import io.github.jiangverse.smsrelay.forwarding.RelayScheduler

class RelayApplication : Application() {
    val store by lazy { RelayStore(this) }
    val repository by lazy { SmsRepository(this) }
    override fun onCreate() {
        super.onCreate()
        // 恢复短信入库后、任务入队前进程退出造成的未完成任务。
        Thread({ repository.pending().forEach { RelayScheduler.enqueue(this, it.id) } }, "relay-recovery").start()
    }
}
