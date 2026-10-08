[**English**](README.md) | [简体中文](README.zh-CN.md)

# DiPlay Android 7

CarPlay for Android 7.1 head units, based on [DiPlay](https://github.com/shihabal3amri/DiPlay).

> [!IMPORTANT]
> This is a personal-use-focused fork maintained primarily for my own Android 7.1 head unit. Fixes, defaults, and design decisions prioritize that environment. Other devices are welcome to try it, but broad compatibility, support, and response times are not guaranteed.

[Download APK](https://github.com/pinatsu/DiPlay-Android7/releases) · [Report an issue](https://github.com/pinatsu/DiPlay-Android7/issues) · [Android 7 test guide](docs/ANDROID7_REAL_DEVICE_TEST_ZH.md) · [Upstream DiPlay](https://github.com/shihabal3amri/DiPlay)

## Project status

The current compatibility baseline is upstream commit `e2fd8ea` (`v0.2.13-51-ge2fd8ea`, 51 commits after the DiPlay 0.2.13 tag). This branch lowers the app requirement from Android 9 (API 28) to Android 7.1 (API 25). It keeps upstream behavior on newer Android versions where possible and adds guarded compatibility paths for Android 7. Later upstream commits require separate integration and validation.

| Environment | Status |
| --- | --- |
| Android 7.1 wired CarPlay | Tested on the maintainer's head unit; this is the primary target |
| Android 7.1 wireless CarPlay | Experimental; the target head unit's complete wireless flow is not yet confirmed |
| Android 9+ | Expected to retain upstream behavior, but this fork does not test every upstream target |
| Other head-unit firmware | Best effort only; USB, Bluetooth, Wi-Fi, audio routing, and hardware decoders vary by vendor |

## What this fork adds

- **Android 7.1 compatibility:** API 25 build support, legacy notification and audio-focus paths, API-safe Base64/date handling, and guarded newer-platform features.
- **Legacy USB and NCM transport:** timeout-capable API 25 bulk-transfer paths for wired CarPlay and scoped IPv6 handling for the local VPN/TUN link.
- **Android 7 wireless groundwork:** an API-25 AOSP RFCOMM path for vendor firmware that replaces the standard Bluetooth SPP connection flow.
- **Per-iPhone pairing records:** Lockdown pairing data is stored separately for each iPhone, allowing phones to be changed without invalidating every saved record.
- **Navigation audio ducking:** an optional setting smoothly lowers CarPlay music during navigation prompts and restores it afterwards.
- **Lower diagnostic overhead:** high-frequency packet and media logs are sampled or summarized to reduce CPU allocation, main-thread work, and flash writes on slower head units.
- **Non-BYD safeguards:** missing BYD private HUD services are detected once instead of being rebound continuously.
- **Personal branding:** the default CarPlay vehicle label is `TOYOTA`, with a custom Toyota-themed vehicle button.

For the exact maintained delta, inspect the commits on top of upstream `main`. The most conflict-prone integration points are `CarPlayController`, `AndroidMediaSink`, and `AirPlayPersistence`.

## Installation

APKs will be published through this fork's Releases page. If no release is listed, a public APK has not been published yet. Upstream DiPlay APKs do not include this fork's Android 7 changes.

1. Park the vehicle and close other phone-projection applications.
2. Download an APK from this repository's [Releases](https://github.com/pinatsu/DiPlay-Android7/releases) page.
3. Install it on the **Android head unit**, not on the iPhone.
4. Open DiPlay and grant only the permissions needed by the connection method you use.
5. For wired CarPlay, select **Connect with USB**, use a data-capable cable and port, and approve the USB/CarPlay and local VPN prompts.

ADB installation is also supported:

```sh
adb install -r DiPlay-Android7.apk
```

An in-place update requires the same package name and Android signing certificate. Do not uninstall an existing working build until its settings and diagnostic reports are no longer needed.

### Suggested starting settings for older head units

- 30 fps
- 60% or 80% resolution
- Efficient video/HEVC off if the decoder is unstable
- 500–1000 ms media buffer when audio underruns are audible
- Built-in car hotspot/manual hotspot mode on Android 7

Higher resolution, frame rate, and optional display effects increase decoder, GPU, and memory pressure.

## Wired CarPlay

Wired CarPlay is the primary, physically tested path for this fork. Android 7 uses bounded USB bulk transfers because the timed `UsbRequest` APIs used by newer Android releases are unavailable on API 25. The app also keeps pairing records separate by iPhone and repairs only the rejected record when Lockdown reports an invalid pairing.

USB behavior still depends on the head unit's host controller, firmware USB mode, cable, hub, and port. Some Android devices default to file-transfer mode; changing the system's default USB configuration can affect whether the iPhone is exposed correctly to the app.

## Wireless CarPlay

Wireless CarPlay on Android 7 is experimental. The target head unit can create a hotspot, but that alone does not prove the complete Bluetooth handoff, iPhone Wi-Fi join, service discovery, and AirPlay path.

On API 25, this fork can bypass a vendor SPP proxy and request the UUID-aware RFCOMM socket from Android's Bluetooth service. Android 7 exposes only the manual/built-in-hotspot workflow in the app. Wi-Fi Direct and newer automatic hotspot APIs remain limited to the Android versions that provide them.

Do not choose this fork solely on the expectation that wireless CarPlay will work on every Android 7 head unit.

## Known limitations

- After an unexpected wired disconnect, the app may return to the waiting screen without reconnecting until the USB cable is unplugged and reinserted. The cause remains under investigation.
- Android 7 firmware can omit the USB-detached broadcast. This fork polls for the active iPhone as a fallback so the stale CarPlay screen can be closed; the behavior still needs repeated validation on the target head unit.
- Older firmware may route navigation, media, calls, and microphone audio differently. The navigation ducking setting does not guarantee correct system routing or equal perceived loudness.
- A successful connection on a newer Android tablet does not establish compatibility with the Android 7.1 head unit.

## Audio behavior

CarPlay media uses the media audio path. Android 7 receives a legacy `STREAM_MUSIC` audio-focus request while newer Android versions retain the upstream `AudioFocusRequest` implementation.

Navigation ducking is enabled by default and can be changed in DiPlay's audio settings. When navigation PCM arrives, CarPlay music fades to approximately 30%, remains lowered through the prompt, and then fades back to full volume. This changes the app's media track volume, not the head unit's global volume setting.

## Diagnostics

For app-level reports, open **Settings → Diagnostics → Save diagnostic report**. Nothing is uploaded automatically. Review every report before sharing it and remove device identifiers, Wi-Fi details, locations, notifications, or account information.

For a connected development machine:

```sh
DIPLAY_PACKAGE=com.shihab.diplay ./scripts/collect-android7-diagnostics.sh
```

For a debug/test APK, use `DIPLAY_PACKAGE=com.shihab.diplay.hudtest` instead. The script can collect logcat, crash buffers, USB/network state, and an Android bug report. Full bug reports can contain sensitive information and should not be attached publicly without manual review. See the [Android 7.1 real-device test guide](docs/ANDROID7_REAL_DEVICE_TEST_ZH.md) for the complete workflow.

When reporting a problem, include the head-unit model and firmware, Android version, iPhone and iOS version, wired/wireless mode, exact reproduction steps, and the approximate failure time.

## Building from source

Requirements currently follow upstream: JDK 25, Android SDK 37, NDK 28.2.13676358, and the included Gradle wrapper.

```sh
./gradlew :shared:testDebugUnitTest :common:testDebugUnitTest :mobile:lintDebug :mobile:assembleDebug
```

Ordinary source and CI builds intentionally contain **no CarPlay accessory authentication identity** and therefore are not standalone CarPlay packages. See [Building DiPlay](docs/BUILD.md) for the external authentication assets and Android release-signing inputs required for a usable local package.

Never commit accessory credentials, an Android signing keystore, passwords, diagnostic dumps, or locally built APKs. The Android signing key must remain stable and private if future APKs are expected to update previous releases.

## Upstream and maintenance policy

This repository is a fork of [shihabal3amri/DiPlay](https://github.com/shihabal3amri/DiPlay), which is based on [shilapi/xcertplay](https://github.com/shilapi/xcertplay). Upstream changes are integrated semantically: behavior that applies unchanged is kept identical, while API-25 alternatives are isolated behind Android-version checks or small compatibility helpers.

This repository is public so the work can be inspected, reused, and tested. However, it remains a personal project rather than a product or general-purpose support service. Issues and pull requests are welcome, but work will be prioritized according to the maintainer's own head unit and available testing time.

## License, authentication, and trademarks

The receiver code is distributed under [GNU GPL v3](LICENSE). Parts of the home/settings UI adapt DiAuto under AGPL-3.0, and some assets have separate terms. Preserve all notices described in [third-party notices](docs/THIRD_PARTY_NOTICES.md) when redistributing the project or an APK.

This is **not an Apple-certified product**. Standalone test/release APKs may contain an experimental accessory identity recovered from public third-party firmware. Its private component is extractable from any distributed APK, and continued acceptance by future iOS releases is not guaranteed. Authentication assets and Android signing keys are not stored in this repository.

CarPlay and Apple are trademarks of Apple Inc. Toyota, the Toyota name, and the Toyota emblem are trademarks of Toyota Motor Corporation. BYD and other referenced names belong to their respective owners. This independent project is not affiliated with, authorized by, sponsored by, or endorsed by Apple, Toyota, BYD, or the upstream maintainers. The custom Toyota-themed label and icon are a personal default and do not indicate an official Toyota product.

## Safety

Configure, test, and diagnose the app only while parked. Stop testing if the head unit reboots, becomes unresponsive, emits unexpected high-volume audio, overheats, or interferes with native vehicle functions such as the reverse camera or instrument display.
