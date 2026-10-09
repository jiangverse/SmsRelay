# LSPosed 从 assets/xposed_init 按完整类名加载入口，禁止删除或改名。
-keep class io.github.jiangverse.smsrelay.hook.SmsHookEntry { public *; }
# 回调由框架调用，保留其实现和依赖，避免优化影响电话进程中的加载。
-keep class io.github.jiangverse.smsrelay.hook.** { *; }
# Hook 在电话进程执行，保留实际使用的 Kotlin 运行时入口，避免 R8 合并到 app/Compose 类。
# 只保留 Hook 运行所需的少量运行时类，避免完整保留 kotlin 包增加包体积。
-keep class kotlin.jvm.internal.Intrinsics { *; }
-keep class kotlin.Result { *; }
-keep class kotlin.ResultKt { *; }
-dontwarn de.robv.android.xposed.**
