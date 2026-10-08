# DiPlay Android 7.1 实机测试指南

本指南适用于本分支 Android 7.1 / API 25 构建。请以实际下载的 APK 和发布说明为准：

- APK：以下示例使用 `DiPlay-Android7.apk`，安装时替换为实际文件名；
- release 包名：`io.github.pinatsu.diplay.android7`；debug/测试包名：`io.github.pinatsu.diplay.android7.hudtest`；
- 当前分支版本：`0.2.13-android7-r14`（versionCode `3214`），debug 版本另带 `-hud-test` 后缀；
- 支持的无线方式：车机自身热点（手动填写热点信息）

本分支已进行 API 25 模拟器测试、维护者车机的有线测试以及较新 Android 平板测试。这些结果不保证其他固件可用，也不能证明目标车机的完整无线流程。最新构建仍需回归测试 USB 插拔、多手机切换、音频、触摸和断线恢复；所有测试应在停车状态下进行。

## 1. 测试前准备

全程驻车测试，车辆挂 P 挡并保持通风。准备：

- Android 7.1 车机，电量或车辆供电稳定；
- 一台 iPhone、可靠的数据线；
- 一台装有 Android Platform Tools 的电脑；
- APK 和本仓库的 `scripts/collect-android7-diagnostics.sh`；
- 车机开启“开发者选项 → USB 调试”；
- 记录车机型号/固件版本、iPhone 型号/iOS 版本和测试时间。

不要在测试期间恢复出厂设置、刷机或授予 root。首次测试关闭 DiPlay 的开机自启，避免车机重启后反复拉起应用。

## 2. 连接 ADB 并确认目标

电脑终端执行：

```bash
adb devices -l
adb shell getprop ro.build.version.release
adb shell getprop ro.build.version.sdk
```

车机上接受 RSA 调试授权。预期系统版本为 `7.1` 或 `7.1.2`，API 为 `25`。如果出现多个设备，后续命令前设置车机序列号：

```bash
export ANDROID_SERIAL=这里填adb显示的车机序列号
```

## 3. 安装测试包

在包含 APK 的目录执行：

```bash
adb install -r "DiPlay-Android7.apk"
```

如果提示签名不一致，不要直接卸载：卸载会清除旧版设置和数据。先记录报错并决定是否需要备份后再卸载。安装成功后执行：

```bash
adb shell am start -W -n io.github.pinatsu.diplay.android7.hudtest/com.shilapi.xcertplay.DiPlayActivity
```

上面的启动命令适用于 debug/测试包。release 包请将组件前面的包名改为 `io.github.pinatsu.diplay.android7`，活动类名保持不变。

预期：应用在 10 秒内打开，无闪退、黑屏或系统反复弹出“已停止运行”。

## 4. 先做无手机冒烟测试

在尚未连接 iPhone 时依次检查：

1. 首页、设置、连接设置均能打开和返回；
2. 连接设置中只使用“车机自身热点”，不使用 Wi-Fi Direct；
3. 设置页能看到“保存诊断报告”；
4. 切换分辨率、30 fps、H.264 后保存，应用不崩溃；
5. 先保留最稳妥配置：60% 或 80% 分辨率、30 fps、H.264、500 ms 音频缓冲；
6. 点“连接手机”时若尚无手机，应显示可理解的等待/失败状态，而不是闪退。

若这一步发生闪退，停止后续 USB/无线测试，直接执行第 8 节的取证流程。

## 5. 有线测试（优先）

先测试有线，因为它能把 Android 7.1 框架兼容问题和热点问题分开。

1. 关闭 iPhone 的个人热点；
2. 打开 DiPlay，选择 USB/有线连接；
3. 连接 iPhone，接受手机端出现的 CarPlay/信任提示；
4. 最多等待 30 秒，记录每个提示出现的时间；
5. 检查画面首帧、触摸、音乐、导航播报和麦克风；
6. 播放音乐 5 分钟，观察爆音、卡顿、延迟和音量通道；
7. 断开数据线，等待 10 秒后重连，重复 3 次；
8. 熄屏/亮屏一次，并切回车机主页再返回 DiPlay。

通过标准：不闪退、不出现持续黑屏；3 次重连至少能稳定复现同一结果；触摸方向和坐标正确；声音不会占错系统通道。任何异常都记录精确时间和刚执行的动作。

## 6. 车机自身热点无线测试

“车机能开热点”说明该路径值得测试，但不能单凭开热点就判断 CarPlay 一定可用。还要确认频段、信道、BSSID、iPhone 入网以及 CarPlay 服务发现。

1. 在车机系统设置中开启热点；
2. 优先选择 5 GHz。若 Android 7.1 固件只能提供 2.4 GHz，仍可做连通性测试，但性能可能不足；
3. 记下热点 SSID、密码、频段/信道和 BSSID（若系统显示）；
4. 在 iPhone 上手动加入该热点，确认不会马上掉线；没有互联网本身不是失败；
5. 在 DiPlay 的“连接设置 → 车机自身热点”中填写完全相同的信息；
6. 保持 iPhone 的 Wi-Fi、蓝牙和蜂窝数据开启，再发起无线连接；
7. 首次连接等待 60 秒，记录停在哪个阶段；
8. 成功后播放音乐和导航 10 分钟，再做 3 次断开/重连；
9. 车机重启后复测一次，但暂时不要启用 DiPlay 开机自启。

若 iPhone 已加入热点但 DiPlay 找不到手机，重点采集 Wi-Fi、mDNS/Bonjour 和连接状态日志；若能连上但卡顿，记录热点频段/信道，并分别用 60% 分辨率、30 fps、H.264、1000 ms 音频缓冲复测。

## 7. 每个问题只做一次清晰复现

建议给问题编号，例如 `A7-USB-01`，并记录：

- 问题编号和精确到秒的发生时间；
- 当时是 USB 还是无线；
- 从应用冷启动开始的每一步；
- 预期结果和实际结果；
- 是否可以稳定复现，重复几次；
- 屏幕照片或短视频；
- 车机/iPhone/系统版本；
- DiPlay 应用内诊断报告和 ADB 采集目录。

不要把多个不同问题混在同一次日志中，否则很难定位因果。

## 8. 收集崩溃、ANR 和连接诊断

### 应用内报告

复现问题后先不要重新连接或清除应用数据。进入：

`DiPlay → 设置 → 诊断 → 保存诊断报告`

Android 7.1 会调用系统文件选择器，需要手动选择保存位置。如果车机没有 DocumentsUI/文件选择器，使用下面的 ADB 脚本；应用内报告失败本身也要记录。

### 一键 ADB 采集

电脑连接车机后，在仓库根目录执行：

```bash
chmod +x scripts/collect-android7-diagnostics.sh
./scripts/collect-android7-diagnostics.sh
```

脚本会：

1. 记录车机和已安装包信息；
2. 启动持续 `logcat` 并打开 DiPlay；
3. 等待你完成一次问题复现；
4. 在按下回车后保存 crash buffer、Activity、内存、图形、USB、网络、Wi-Fi、DropBox、ANR/tombstone 可见性信息；
5. 询问是否生成完整 Android bugreport，直接回车默认生成。

结果位于 `diagnostics/diplay-日期时间/`。原生崩溃 tombstone 和 ANR traces 在非 root 车机上通常不能直接读取，但 bugreport/DropBox 往往会包含系统允许导出的部分。即使应用已闪退，也不要立刻重启车机；先让脚本完成。

如果安装的是正式包而非当前测试包，执行：

```bash
DIPLAY_PACKAGE=io.github.pinatsu.diplay.android7 ./scripts/collect-android7-diagnostics.sh
```

## 9. 隐私与提交内容

应用内诊断报告会经过项目的脱敏器处理，优先分享它。完整 `logcat` 和 Android bugreport 可能包含设备标识、Wi-Fi 信息、定位、账户、通知内容或其他应用信息；分享前必须人工检查，不能公开上传原始 bugreport。

每个问题至少提供：

- 应用内的 `DiPlay-*.txt`；
- 采集目录中的 `summary.txt`、`launch.txt`、`crash-buffer.txt`；
- 出问题时间前后约 60 秒的 `logcat-full.txt`；
- 若是 ANR、native 崩溃、系统重启或 USB 枚举失败，再私下提供审核过的 bugreport；
- 问题步骤、照片/视频和硬件版本信息。

## 10. 立即停止测试的情况

出现以下任一情况就断开 iPhone、退出 DiPlay并停止测试：

- 车机反复重启、系统界面持续无响应；
- 音频发出持续高音量噪声；
- 车辆原生倒车影像、仪表或关键控制受到影响；
- 车机明显过热；
- 应用进入连续崩溃/自动重启循环。

恢复顺序：拔掉 iPhone → 强制停止 DiPlay → 关闭车机热点 → 正常重启车机。不要在行驶中排障。
