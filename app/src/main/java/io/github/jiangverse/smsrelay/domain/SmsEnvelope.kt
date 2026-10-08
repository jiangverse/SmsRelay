package io.github.jiangverse.smsrelay.domain

import java.security.MessageDigest

data class SmsEnvelope(val sender: String, val body: String, val timestamp: Long, val subscription: Int) {
    /** 指纹包含字段长度和 SIM 订阅标识，避免拼接歧义。 */
    fun fingerprint(): String {
        val source = "${sender.length}:$sender${body.length}:$body:$timestamp:$subscription"
        return MessageDigest.getInstance("SHA-256").digest(source.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
    }
}
