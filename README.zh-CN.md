[English](README.md) | [**简体中文**](README.zh-CN.md)

# DiPlay Android 7

这是一个基于 [DiPlay](https://github.com/shihabal3amri/DiPlay) 的个人定制分支，主要服务于维护者自己的 Android 7.1 车机。

本分支保留 DiPlay 上游 0.2.15 的通用功能，重点改善五项日常可感知的有线 CarPlay 使用体验：不拔线重新连接、更可靠的拔线检测、改善导航音频、多 iPhone 切换，以及导航播报时自动压低音乐。

> [!IMPORTANT]
> 修复和默认设置首先考虑维护者自己的 Android 7.1 车机。欢迎其他设备尝试，但不保证广泛兼容，也不承诺通用支持。

[下载 r19](https://github.com/pinatsu/DiPlay-Android7/releases/tag/v0.2.15-android7-r19) · [报告问题](https://github.com/pinatsu/DiPlay-Android7/issues) · [DiPlay 上游](https://github.com/shihabal3amri/DiPlay)

## 与上游版本的主要区别

### 有线连接可靠性

#### 1. 不拔线重新连接

开始新的有线 CarPlay 会话时，如果车机内核支持，应用会执行 USB reset 和重新枚举。这样在软件断开、修改设置或连接失败后，可以尽量直接建立新会话，而不必重新拔插数据线。

如果内核不支持 USB reset，DiPlay 会回退到普通连接流程；在不支持或经过厂商修改的内核上，极端情况下仍可能需要手动物理拔插。

#### 2. 更可靠的拔线检测

部分 Android 车机不能稳定上报 iPhone 已经拔出，导致拔线后仍停留在旧 CarPlay 画面。本分支增加了轻量的备用检测；即使 Android 遗漏 USB 拔出事件，也能识别 iPhone 已经消失并结束残留会话。

#### 3. 多 iPhone 切换

每部 iPhone 的有线配对记录会分别保存。每部手机完成首次信任和配对后，切换手机不会再覆盖其他手机已经保存的记录。

该优化不会跳过 iPhone 首次连接时的信任提示，也不能替代正常的 USB 设备识别流程。

### 导航音频体验

#### 4. 改善有线导航音频

有线导航音频使用在维护者设备上验证过的 48 kHz 格式，改善部分车机上导航播报明显偏小或协商到不合适音频格式的问题。

该功能不会修改 Android 系统音量；不同车机的音频路由不同，因此不能保证所有设备上的听感响度完全一致。

#### 5. 导航播报时压低音乐

导航开始播报时，CarPlay 音乐会平滑降低到约 30%；播报结束后自动恢复。该功能默认开启，可以在“**设置 → 音频路由**”中关闭。

它只调整 CarPlay 的媒体音轨，不会修改车机的系统总音量。

## 其他可见差异

- 独立包名：`io.github.pinatsu.diplay.android7`。可以与上游 DiPlay 共存，两者的设置和配对记录互不影响。
- 个人品牌设置：CarPlay 默认车辆名称为 `TOYOTA`，车辆按钮使用维护者的定制丰田图标。
- 有线 CarPlay 是主要目标。无线功能继承自上游，但在维护者的 Android 7.1 车机上仍属于实验功能。

## 与上游的关系

r19 基于完整的 [DiPlay 上游 0.2.15](https://github.com/shihabal3amri/DiPlay/releases/tag/v0.2.15) 源码。上游已经支持 Android 7.1 / API 25，因此本分支不会把 API 25 支持本身描述成自己的新增功能。

界面、显示、无线、仪表、诊断和媒体等通用能力均跟随上游。本分支只维护上面列出的针对性差异；以后同步上游时也会按功能语义整合，而不是复制并长期分叉每一项上游功能。

- [本分支 r19 发布说明](docs/RELEASE-NOTES-ANDROID7-R19.md)
- [上游 0.2.15 发布说明](docs/RELEASE-NOTES-0.2.15.md)
- [验证记录](docs/VALIDATION.md)
