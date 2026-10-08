[English](README.md) | [**简体中文**](README.zh-CN.md)

# DiPlay Android 7

面向 Android 7.1 车机的 CarPlay 应用，基于 [DiPlay](https://github.com/shihabal3amri/DiPlay) 维护。

> [!IMPORTANT]
> 这是一个以个人使用为主要目标的分支，首先服务于维护者自己的 Android 7.1 车机。修复、默认设置和设计选择都会优先考虑这套环境。欢迎其他设备尝试，但不保证广泛兼容，也不承诺支持范围和响应时间。

[下载 APK](https://github.com/pinatsu/DiPlay-Android7/releases) · [报告问题](https://github.com/pinatsu/DiPlay-Android7/issues) · [Android 7 实机测试指南](docs/ANDROID7_REAL_DEVICE_TEST_ZH.md) · [DiPlay 上游](https://github.com/shihabal3amri/DiPlay)

## 项目状态

当前分支版本为 `0.2.13-android7-r14`，兼容基线为上游提交 `e2fd8ea`（`v0.2.13-51-ge2fd8ea`，位于 DiPlay 0.2.13 标签之后 51 个提交）。本分支将最低系统要求从 Android 9（API 28）降到 Android 7.1（API 25），在较新 Android 版本上尽量保持上游行为，在 Android 7 上通过受保护的兼容路径运行。此后的上游提交需要另外同步和验证。

| 环境 | 当前状态 |
| --- | --- |
| Android 7.1 有线 CarPlay | 已在维护者的车机上实测，是本项目的主要目标 |
| Android 7.1 无线 CarPlay | 实验阶段；目标车机的完整无线流程尚未确认成功 |
| Android 9 及以上 | 预期保留上游行为，但本分支不会测试上游支持的全部设备 |
| 其他车机固件 | 仅尽力兼容；USB、蓝牙、Wi-Fi、音频路由和硬件解码器都可能被厂商修改 |

## 本分支增加的能力

- **Android 7.1 兼容**：支持 API 25 构建，补充旧版通知和音频焦点路径，并替换或保护较新系统才提供的 Base64、日期等 API。
- **旧版 USB 和 NCM 传输**：为有线 CarPlay 增加 API 25 可用且带超时的批量传输，并修正本地 VPN/TUN 链路的 IPv6 scope。
- **Android 7 无线基础适配**：针对替换了标准蓝牙 SPP 流程的厂商固件，增加 API 25 专用的 AOSP RFCOMM 路径。
- **多 iPhone 配对记录**：每部 iPhone 独立保存 Lockdown 配对数据，切换手机时不会让全部记录一起失效。
- **导航压低音乐**：可选开关会在导航播报时平滑降低 CarPlay 音乐音量，播报结束后恢复。
- **降低诊断开销**：对高频数据包和媒体日志采样或汇总，减少老车机上的字符串分配、主线程工作和闪存写入。
- **非 BYD 车机保护**：只检查一次 BYD 私有 HUD 服务是否存在，不再持续绑定不存在的组件。
- **个人品牌设置**：CarPlay 默认车辆名称为 `TOYOTA`，车辆按钮使用定制的丰田主题图标。

如需查看精确差异，可以比较本分支位于上游 `main` 之后的提交。后续同步时最容易产生冲突的文件是 `CarPlayController`、`AndroidMediaSink` 和 `AirPlayPersistence`。

## 安装

APK 将通过本分支的 Releases 页面发布；如果页面没有版本，说明尚未发布公开 APK。原 DiPlay 上游 APK 不包含本分支的 Android 7 修改。

1. 将车辆停稳，并关闭其他手机投屏应用。
2. 从本仓库的 [Releases](https://github.com/pinatsu/DiPlay-Android7/releases) 页面下载 APK。
3. 将 APK 安装到 **Android 车机**，不是 iPhone。
4. 打开 DiPlay，只授予所使用连接方式需要的权限。
5. 有线连接时选择“使用 USB 连接”，使用支持数据传输的线和接口，并同意 USB、CarPlay 和本地 VPN 提示。

也可以通过 ADB 安装：

```sh
adb install -r DiPlay-Android7.apk
```

覆盖更新要求包名和 Android 签名证书保持一致。在不再需要旧版设置和诊断报告之前，不要卸载已经可以工作的版本。

本分支使用独立包名 `io.github.pinatsu.diplay.android7`，可以与上游 DiPlay（`com.shihab.diplay`）共存；两者的设置和手机配对记录也彼此独立。

### 老车机建议起始设置

- 30 fps
- 60% 或 80% 分辨率
- 如果解码器不稳定，关闭“高效视频/HEVC”
- 音频出现欠载或断音时，尝试 500～1000 ms 媒体缓冲
- Android 7 使用车机内置热点/手动热点模式

更高的分辨率、帧率和可选画面效果会增加解码器、GPU 和内存负担。

## 有线 CarPlay

有线 CarPlay 是本分支主要且经过实车测试的连接方式。由于 API 25 没有较新 Android 使用的带超时 `UsbRequest` 接口，本分支在 Android 7 上改用有边界的 USB 批量传输。应用还会按 iPhone 分开保存配对记录；当 Lockdown 拒绝某条配对记录时，只修复对应手机。

USB 表现仍取决于车机 USB Host 控制器、固件 USB 模式、数据线、Hub 和接口。一些 Android 设备默认进入文件传输模式；修改系统的默认 USB 配置可能影响 iPhone 是否能正确交给应用处理。

## 无线 CarPlay

Android 7 上的无线 CarPlay 仍然是实验功能。目标车机可以开启热点，但这并不能单独证明蓝牙交接、iPhone 加入 Wi-Fi、服务发现和 AirPlay 数据路径都能完成。

在 API 25 上，本分支可以绕过厂商 SPP 代理，直接向 Android 蓝牙服务请求支持 UUID 的 RFCOMM socket。Android 7 的应用界面只提供手动/车机内置热点流程；Wi-Fi Direct 和较新的自动热点 API 仍只用于实际提供这些接口的 Android 版本。

如果主要需求是“任何 Android 7 车机都能使用无线 CarPlay”，请不要仅凭这个项目作出判断。

## 已知限制

- 在已测试的平板上，本分支可以通过 USB reset 和重新枚举建立新的有线会话，无需拔线；如果车机内核拒绝 USB reset，仍可能需要物理拔插。
- Android 7 固件可能不发送 USB 拔出广播。本分支会额外轮询当前 iPhone，在设备消失时关闭残留的 CarPlay 画面；仍需在目标车机上反复验证。
- 旧固件可能对导航、音乐、通话和麦克风使用不同的音频路由。导航压低音乐开关不能保证系统路由正确或听感音量一致。
- 在较新 Android 平板上连接成功，不能证明 Android 7.1 车机也兼容。

## 音频表现

CarPlay 媒体使用媒体音频路径。Android 7 通过旧版 `STREAM_MUSIC` 接口请求音频焦点，较新 Android 继续使用上游的 `AudioFocusRequest` 实现。

导航压低音乐默认开启，可以在 DiPlay 音频设置中关闭。检测到导航 PCM 后，CarPlay 音乐会渐变到约 30%；播报结束后再平滑恢复到 100%。它调整的是应用媒体轨道音量，不会修改车机的系统总音量设置。

## 诊断

应用内可进入“**设置 → 诊断 → 保存诊断报告**”。报告不会自动上传。分享前必须人工检查，并删除设备标识、Wi-Fi 信息、位置、通知或账户等隐私内容。

连接开发电脑后还可以运行：

```sh
DIPLAY_PACKAGE=io.github.pinatsu.diplay.android7 ./scripts/collect-android7-diagnostics.sh
```

如果安装的是 debug/测试包，请改用 `DIPLAY_PACKAGE=io.github.pinatsu.diplay.android7.hudtest`。脚本可以收集 logcat、崩溃缓冲区、USB/网络状态和 Android bugreport。完整 bugreport 可能包含大量隐私信息，未经人工检查不应公开上传。完整流程见 [Android 7.1 实机测试指南](docs/ANDROID7_REAL_DEVICE_TEST_ZH.md)。

提交问题时请附上车机型号和固件、Android 版本、iPhone 和 iOS 版本、有线/无线连接方式、准确复现步骤以及大致故障时间。

## 从源码构建

构建要求暂时跟随上游：JDK 25、Android SDK 37、NDK 28.2.13676358，以及仓库附带的 Gradle Wrapper。

```sh
./gradlew :shared:testDebugUnitTest :common:testDebugUnitTest :mobile:lintDebug :mobile:assembleDebug
```

普通源码和 CI 构建刻意不包含 **CarPlay 配件认证身份**，因此生成的包不能独立建立 CarPlay 连接。可用的本地包需要从外部提供认证资源和 Android release 签名参数，具体见[构建说明](docs/BUILD.md)。

不要提交配件凭据、Android 签名 keystore、密码、诊断转储或本地构建的 APK。如果希望以后发布的 APK 可以覆盖更新，Android 签名密钥必须长期保持不变并妥善保管。

## 上游关系和维护原则

本仓库 Fork 自 [shihabal3amri/DiPlay](https://github.com/shihabal3amri/DiPlay)，而 DiPlay 基于 [shilapi/xcertplay](https://github.com/shilapi/xcertplay)。同步上游时采用“语义同步”：能原样使用的行为尽量与上游保持一致，API 25 的替代实现则通过 Android 版本判断或小型兼容类隔离。

公开仓库的目的是让代码可以被检查、参考和测试，但它仍然是个人项目，不是商品或通用技术支持服务。欢迎提交 Issue 和 Pull Request，但维护工作会优先考虑维护者自己的车机和可用于测试的时间。

## 许可证、认证与商标

接收端代码根据 [GNU GPL v3](LICENSE) 发布。主页和设置界面的部分内容基于 AGPL-3.0 的 DiAuto，部分资源还有单独的使用条款。重新分发项目或 APK 时，必须保留[第三方声明](docs/THIRD_PARTY_NOTICES.md)中列出的通知。

这不是经过 Apple 认证的产品。可以独立连接的测试版或发布版 APK 可能包含从公开第三方固件中取得的实验性配件身份，其中的私有部分可以从已发布 APK 中提取，未来 iOS 是否继续接受也无法保证。认证资源和 Android APK 签名密钥不会存放在本仓库中。

CarPlay 和 Apple 是 Apple Inc. 的商标；Toyota、TOYOTA 名称和丰田标志是 Toyota Motor Corporation 的商标；BYD 及其他提及的名称属于各自权利人。本独立项目与 Apple、Toyota、BYD 及上游维护者不存在隶属、授权、赞助或认可关系。定制的 TOYOTA 名称和图标只是个人默认设置，不代表丰田官方产品。

## 安全

只在停车状态下配置、测试和诊断。如果车机反复重启、失去响应、发出异常高音量声音、明显过热，或者影响倒车影像、仪表等原车功能，应立即停止测试。
