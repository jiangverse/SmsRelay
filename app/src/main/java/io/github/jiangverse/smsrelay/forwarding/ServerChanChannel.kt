package io.github.jiangverse.smsrelay.forwarding

import io.github.jiangverse.smsrelay.R
import io.github.jiangverse.smsrelay.domain.ForwardChannel
import io.github.jiangverse.smsrelay.domain.RelaySettings
import io.github.jiangverse.smsrelay.domain.SendResult
import io.github.jiangverse.smsrelay.domain.SmsEnvelope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class ServerChanChannel(private val context: android.content.Context) : ForwardChannel {
    override suspend fun send(settings: RelaySettings, sms: SmsEnvelope): SendResult = withContext(Dispatchers.IO) {
        if (settings.sendKey.isBlank()) return@withContext SendResult.Failure(context.getString(R.string.missing_key), false)
        if (!settings.sendKey.matches(Regex("SCT[A-Za-z0-9]+"))) {
            return@withContext SendResult.Failure(context.getString(R.string.invalid_key), false)
        }
        val request = Request.Builder()
            .url("https://sctapi.ftqq.com/${settings.sendKey}.send")
            .post(FormBody.Builder().add("title", if (settings.titleMode == io.github.jiangverse.smsrelay.domain.TitleMode.DEFAULT) "${settings.titlePrefix}${context.getString(R.string.sms_title)}${settings.titleSuffix}" else settings.title.take(64))
                .add("desp", context.getString(R.string.sms_content, sms.sender, sms.body)).build())
            .build()
        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext SendResult.Failure(context.getString(R.string.http_error, response.code), response.code >= 500 || response.code == 429)
                }
                val json = JSONObject(response.body?.string().orEmpty())
                if (json.optInt("code", -1) == 0) SendResult.Success
                else SendResult.Failure(context.getString(R.string.business_error, json.optInt("code", -1)), false)
            }
        } catch (_: IOException) {
            // 异常可能包含 URL 和密钥，禁止保存或显示原始异常信息。
            SendResult.Failure(context.getString(R.string.network_error), true)
        } catch (_: org.json.JSONException) {
            SendResult.Failure(context.getString(R.string.response_error), false)
        }
    }
    private companion object {
        val client = OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS).callTimeout(25, TimeUnit.SECONDS)
            .followRedirects(false).followSslRedirects(false).build()
    }
}
