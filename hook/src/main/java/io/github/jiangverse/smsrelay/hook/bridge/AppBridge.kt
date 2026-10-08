package io.github.jiangverse.smsrelay.hook.bridge

import android.content.Context
import android.os.Bundle
import de.robv.android.xposed.XposedBridge
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/** 异步通信，避免 Binder 和数据库操作阻塞电话状态机。 */
object AppBridge {
    private val executor = ThreadPoolExecutor(1, 1, 30, TimeUnit.SECONDS,
        ArrayBlockingQueue(128), { task -> Thread(task, "SmsRelay-bridge").apply { isDaemon = true } },
        ThreadPoolExecutor.AbortPolicy())

    fun send(context: Context, method: String, payload: Bundle) {
        try {
            executor.execute {
                try {
                    payload.putInt("version", RelayContract.VERSION)
                    val reply = context.contentResolver.call(RelayContract.URI, method, null, payload)
                    if (reply?.getBoolean("accepted") != true) {
                        XposedBridge.log("SmsRelay: app did not accept $method")
                    }
                } catch (error: Throwable) {
                    XposedBridge.log("SmsRelay: app bridge unavailable (${error.javaClass.simpleName})")
                }
            }
        } catch (_: Throwable) {
            XposedBridge.log("SmsRelay: bridge queue full")
        }
    }
}
