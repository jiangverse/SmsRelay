package io.github.jiangverse.smsrelay.bridge

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Binder
import io.github.jiangverse.smsrelay.RelayApplication
import io.github.jiangverse.smsrelay.domain.SmsEnvelope
import io.github.jiangverse.smsrelay.domain.ForwardType
import io.github.jiangverse.smsrelay.forwarding.RelayScheduler
import io.github.jiangverse.smsrelay.hook.bridge.RelayContract

class SmsIngressProvider : ContentProvider() {
    override fun onCreate() = true
    override fun call(method: String, arg: String?, extras: Bundle?): Bundle {
        // 校验电话服务共享 UID，不信任可伪造的消息字段。
        if (Binder.getCallingUid() != 1001) throw SecurityException("Telephony callers only")
        val identity = Binder.clearCallingIdentity()
        return try { ingest(method, extras) } finally { Binder.restoreCallingIdentity(identity) }
    }
    private fun ingest(method: String, extras: Bundle?): Bundle {
        if (extras?.getInt("version") != RelayContract.VERSION) return Bundle().apply { putBoolean("accepted", false) }
        val app = requireNotNull(context).applicationContext as RelayApplication
        val result = Bundle().apply { putBoolean("accepted", true) }
        if (method == RelayContract.STATUS) {
            app.store.markHookSeen()
            return result
        }
        if (method != RelayContract.SMS) return Bundle().apply { putBoolean("accepted", false) }
        val sms = SmsEnvelope(extras.getString(RelayContract.SENDER).orEmpty(), extras.getString(RelayContract.BODY).orEmpty(),
            extras.getLong(RelayContract.TIMESTAMP), extras.getInt(RelayContract.SUBSCRIPTION, -1))
        if (sms.body.isBlank() || sms.body.length > 64000 || sms.sender.length > 256 || sms.timestamp <= 0) {
            return Bundle().apply { putBoolean("accepted", false) }
        }
        app.store.markHookSeen()
        val settings = app.store.settings.value
        if (!settings.enabled || !matches(settings.forwardType, sms.body, settings.customKeywords)) return result
        app.repository.accept(sms)
        RelayScheduler.enqueue(requireNotNull(context), sms.fingerprint())
        return result
    }

    private fun matches(filter: ForwardType, body: String, customKeywords: List<String>): Boolean {
        val text = body.lowercase()
        return when (filter) {
            ForwardType.ALL -> true
            ForwardType.OTP -> customKeywords.ifEmpty { requireNotNull(context).resources.getStringArray(io.github.jiangverse.smsrelay.R.array.otp_keywords).toList() }
                .any { text.contains(it.lowercase()) }
            ForwardType.PICKUP -> customKeywords.ifEmpty { requireNotNull(context).resources.getStringArray(io.github.jiangverse.smsrelay.R.array.pickup_keywords).toList() }
                .any { text.contains(it.lowercase()) }
        }
    }
    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? = null
    override fun getType(uri: Uri): String? = null
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
}
