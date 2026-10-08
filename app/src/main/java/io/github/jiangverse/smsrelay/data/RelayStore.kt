package io.github.jiangverse.smsrelay.data
import android.content.Context
import androidx.core.content.edit
import io.github.jiangverse.smsrelay.domain.*
import kotlinx.coroutines.flow.*
import org.json.JSONObject
class RelayStore(private val context: Context) {
 private val prefs=context.getSharedPreferences("relay_settings",0); private val _settings=MutableStateFlow(read()); val settings:StateFlow<RelaySettings> = _settings.asStateFlow(); val hookSeen=MutableStateFlow(prefs.getLong("hook_seen",0))
 fun markHookSeen(){val n=System.currentTimeMillis();prefs.edit{putLong("hook_seen",n)};hookSeen.value=n}
 fun update(v:RelaySettings){prefs.edit{putString("settings",JSONObject().apply{put("enabled",v.enabled);put("channel",v.channel.name);put("sendKey",v.sendKey);put("titleMode",v.titleMode.name);put("title",v.title);put("titlePrefix",v.titlePrefix);put("titleSuffix",v.titleSuffix);put("forwardType",v.forwardType.name);put("customKeywords",v.customKeywords.joinToString("\n"))}.toString())};_settings.value=v}
 private fun read()=runCatching{val j=JSONObject(prefs.getString("settings","{}")!!);RelaySettings(j.optBoolean("enabled"),ChannelType.valueOf(j.optString("channel",ChannelType.SERVER_CHAN.name)),j.optString("sendKey"),TitleMode.valueOf(j.optString("titleMode",TitleMode.DEFAULT.name)),j.optString("title",context.getString(io.github.jiangverse.smsrelay.R.string.default_custom_title)),j.optString("titlePrefix",context.getString(io.github.jiangverse.smsrelay.R.string.default_prefix)),j.optString("titleSuffix",context.getString(io.github.jiangverse.smsrelay.R.string.default_suffix)),ForwardType.valueOf(j.optString("forwardType",ForwardType.ALL.name)),j.optString("customKeywords").split("\n").filter{it.isNotBlank()})}.getOrDefault(RelaySettings())
}
