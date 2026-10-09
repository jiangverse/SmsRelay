package io.github.jiangverse.smsrelay.hook

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import android.os.Bundle
import io.github.jiangverse.smsrelay.hook.bridge.AppBridge
import io.github.jiangverse.smsrelay.hook.bridge.RelayContract
import io.github.jiangverse.smsrelay.hook.capture.InboundSmsHook
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers

/** 传统 Xposed API 入口，适用于保留传统模块支持的 LSPosed。 */
class SmsHookEntry : IXposedHookLoadPackage {
    override fun handleLoadPackage(param: XC_LoadPackage.LoadPackageParam) {
        if (param.packageName != "com.android.phone") return
        try {
            val hookClassLoader = param.classLoader
            XposedHelpers.findAndHookMethod("android.app.Application", hookClassLoader, "attach", android.content.Context::class.java, object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val context = param.args[0] as android.content.Context
                    runCatching { InboundSmsHook.install(hookClassLoader) }
                        .onFailure { error ->
                            XposedBridge.log("SmsRelay: SMS hook installation failed")
                            XposedBridge.log(error)
                        }
                    AppBridge.send(context, RelayContract.STATUS, Bundle().apply { putString(RelayContract.DETAIL, "Hook loaded") })
                }
            })
            // 应用初始化后安装短信监听。
            // 上报状态，便于排查作用域和通信问题。
        } catch (error: Throwable) {
            // 禁止将模块异常传播到电话服务进程。
            XposedBridge.log("SmsRelay: hook installation failed (${error.javaClass.simpleName})")
            XposedBridge.log(error)
        }
    }
}
