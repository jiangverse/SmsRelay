package io.github.jiangverse.smsrelay.domain
enum class ChannelType(val label: String) { SERVER_CHAN("Server Chan") }
enum class ForwardType(val label: String) { ALL("All SMS"), OTP("OTP"), PICKUP("Pickup code") }
enum class TitleMode(val label: String) { DEFAULT("Default title"), CUSTOM("Custom title") }
data class RelaySettings(val enabled: Boolean = false, val channel: ChannelType = ChannelType.SERVER_CHAN, val sendKey: String = "", val titleMode: TitleMode = TitleMode.DEFAULT, val title: String = "New SMS", val titlePrefix: String = "[", val titleSuffix: String = "]", val forwardType: ForwardType = ForwardType.ALL, val customKeywords: List<String> = emptyList())
sealed interface SendResult { data object Success : SendResult; data class Failure(val message: String, val retryable: Boolean) : SendResult }
interface ForwardChannel { suspend fun send(settings: RelaySettings, sms: SmsEnvelope): SendResult }
