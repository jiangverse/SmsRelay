package io.github.jiangverse.smsrelay.hook.capture

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Telephony
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import io.github.jiangverse.smsrelay.hook.bridge.AppBridge
import io.github.jiangverse.smsrelay.hook.bridge.RelayContract

object InboundSmsHook {
    fun install(loader: ClassLoader) {
        val type = XposedHelpers.findClass("com.android.internal.telephony.InboundSmsHandler", loader)
        val methods = XposedBridge.hookAllMethods(type, "dispatchIntent", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (param.hasThrowable()) return
                try {
                    val intent = param.args.filterIsInstance<Intent>().firstOrNull() ?: return
                    // 仅监听 SMS_DELIVER，避免与后续 SMS_RECEIVED 重复处理。
                    if (intent.action != Telephony.Sms.Intents.SMS_DELIVER_ACTION) return
                    val context = XposedHelpers.getObjectField(param.thisObject, "mContext") as Context
                    val parts = Telephony.Sms.Intents.getMessagesFromIntent(intent)
                    if (parts.isNullOrEmpty()) return
                    val payload = Bundle().apply {
                        putString(RelayContract.SENDER, parts.first().originatingAddress.orEmpty())
                        putString(RelayContract.BODY, parts.joinToString("") { it.messageBody.orEmpty() })
                        putLong(RelayContract.TIMESTAMP, parts.first().timestampMillis)
                        putInt(RelayContract.SUBSCRIPTION, intent.getIntExtra("subscription", -1))
                    }
                    AppBridge.send(context, RelayContract.SMS, payload)
                } catch (error: Throwable) {
                    XposedBridge.log("SmsRelay: capture failed (${error.javaClass.simpleName})")
                }
            }
        })
        check(methods.isNotEmpty()) { "dispatchIntent not found" }
        XposedBridge.log("SmsRelay: installed ${methods.size} dispatchIntent hook(s)")
        XposedBridge.hookAllConstructors(type, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                try {
                    val context = XposedHelpers.getObjectField(param.thisObject, "mContext") as Context
                    AppBridge.send(context, RelayContract.STATUS, Bundle().apply {
                        putString(RelayContract.DETAIL, "InboundSmsHandler / dispatchIntent")
                    })
                } catch (_: Throwable) {
                    XposedBridge.log("SmsRelay: status handshake unavailable")
                }
            }
        })
    }
}
