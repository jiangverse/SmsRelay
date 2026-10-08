<p style="text-align: center;"><img src="docs/icon.svg" width="112" alt="SmsRelay 图标" /></p>
<h1 style="text-align: center;">SmsRelay·短信转发</h1>

基于Android Compose和LSPosed的短信转发工具，目前支持Server酱Turbo。欢迎给[GitHub 项目](https://github.com/jiangverse/SmsRelay)点个Star，或提交问题和建议。

## 功能与配置

- **转发类型**：全部短信、验证码、取件码。验证码和取件码按关键词匹配，不保证识别所有短信格式。
- **转发方式**：Server酱Turbo，填写以SCT开头的SendKey。
- **标题方式**：默认标题使用可配置前缀、短信标题和后缀；自定义标题直接使用输入内容。
- 支持测试推送、转发记录、消息去重及网络失败重试。

## 使用步骤

1. 安装 APK，打开应用。
2. 在 LSPosed中启用模块，作用域选择电话服务（`com.android.phone`），然后重启设备。
3. 填写 SendKey，选择转发类型与标题方式，打开自动转发并保存。
4. 点击“发送测试推送”，在转发记录中确认发送结果；测试推送不验证短信 Hook。
5. 发送真实短信验证捕获与转发。Hook握手只表明跨进程通信成功。

厂商ROM的电话服务实现可能不同，兼容性应以实际测试为准。排查时请查看LSPosed中的SmsRelay日志，提交日志前删除短信内容和密钥。

## 隐私说明

本项目没有开发者数据收集服务器，不包含统计、广告或追踪功能，不向开发者上传短信、密钥或隐私信息。配置和转发记录存储于应用私有目录，系统备份已禁用。

**启用转发后，匹配的短信内容和发送号码会通过HTTPS发送到用户配置的Server酱服务。** 该服务会收到SendKey和推送数据，并按其自身政策处理。测试推送同样会访问Server酱。请谨慎选择转发范围，尤其是验证码等敏感信息。GitHub链接只在点击后由浏览器打开。

## 项目结构

`hook`：Xposed 入口、短信捕获、跨进程协议与异步传递，不包含 UI、密钥或网络转发。

`app`：`bridge` 接收入站短信；`domain` 定义配置；`data` 保存配置与记录；`forwarding` 使用 OkHttp 和 WorkManager 发送及重试；`ui` 提供Compose界面。

新增其他渠道时，在app中实现`ForwardChannel`并增加渠道配置和任务路由，无需将网络逻辑加入hook。



## 许可证

项目使用 [Apache License 2.0](LICENSE)，允许在遵循许可证条件的前提下使用、修改和分发，不提供任何担保。
