# LSPosed 从 assets/xposed_init 按完整类名加载入口，禁止删除或改名。
-keep class io.github.jiangverse.smsrelay.hook.SmsHookEntry { public *; }
# 回调由框架调用，保留其实现和依赖，避免优化影响电话进程中的加载。
-keep class io.github.jiangverse.smsrelay.hook.** { *; }
-dontwarn de.robv.android.xposed.**
