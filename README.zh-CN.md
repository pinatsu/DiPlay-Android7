[English](README.md) | [**简体中文**](README.zh-CN.md)

# DiPlay Android 7 定制版

这是基于 [DiPlay](https://github.com/shihabal3amri/DiPlay) 维护的个人定制分支，主要适配维护者自己的 Android 7.1 / API 25 车机，并以有线 CarPlay 为主要使用方式。

## 主要定制

- 在车机支持 USB reset 时重新建立有线连接，尽量避免反复拔插 iPhone 数据线。
- 补充 iPhone 拔出检测，减少厂商 Android 系统遗漏 USB 拔出事件后 CarPlay 画面仍然残留的问题。
- 分别保存不同 iPhone 的有线配对记录，改善多部手机轮换使用。
- 使用适配目标车机的有线导航音频格式，并支持导航播报时自动压低 CarPlay 音乐。
- 保留维护者自己的车辆名称、车辆图标和目标车机适配。
- 使用独立包名、更新通道和应用数据。

## 应用身份

- 包名：`io.github.pinatsu.diplay.android7`
- 应用名称：`DiPlay`
- 版本格式：`<上游版本>-android7-r<N>`
- 最低系统：Android 7.1 / API 25

> [!IMPORTANT]
> 本分支优先保证维护者自己的车机可用，不承诺兼容其他车型、车机或 Android 版本。无线 CarPlay 功能跟随上游，但在目标 Android 7.1 车机上仍视为实验功能。

完整的 DiPlay 项目介绍请阅读[英文 README](README.md)。其中分隔线以下保留了上游原始 README；上游部分出现的下载地址、包名和发布版本均指向上游 DiPlay。面向维护者和 AI 的完整定制要求记录在 [Android 7 分支定制契约](docs/ANDROID7_FORK_CONTRACT.md)。
