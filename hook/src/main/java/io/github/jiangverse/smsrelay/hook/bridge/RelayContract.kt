package io.github.jiangverse.smsrelay.hook.bridge

import android.net.Uri

/** 带版本号的跨进程协议，转发配置和凭据不进入电话进程。 */
object RelayContract {
    const val APP_ID = "io.github.jiangverse.smsrelay"
    val URI: Uri = Uri.parse("content://$APP_ID.ingress")
    const val VERSION = 1
    const val SMS = "sms"
    const val STATUS = "status"
    const val SENDER = "sender"
    const val BODY = "body"
    const val TIMESTAMP = "timestamp"
    const val SUBSCRIPTION = "subscription"
    const val DETAIL = "detail"
}
