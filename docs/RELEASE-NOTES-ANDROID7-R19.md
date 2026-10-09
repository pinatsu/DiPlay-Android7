# DiPlay Android 7 r19

Version `0.2.15-android7-r19` (versionCode `3419`) moves the fork to the complete upstream DiPlay 0.2.15 source baseline while retaining the focused behavior used by the maintainer's Android 7.1 head unit.

## Changes

- Includes the upstream 0.2.15 Android 7.1 compatibility, interface, settings, audio recovery, wireless, diagnostics, update-check and dashboard changes.
- Keeps the independent package `io.github.pinatsu.diplay.android7`, the `TOYOTA` vehicle label and the custom Toyota vehicle button.
- Stores wired Lockdown pairing records separately for each iPhone and repairs only the record rejected by that phone.
- Starts every wired software connection with a bounded USB reset and re-enumeration attempt; kernels without reset support fall back to the existing attached device.
- Detects a physically removed iPhone when vendor Android firmware omits the USB-detached broadcast.
- Deactivates the NCM data interface during teardown and filters duplicate, overlapping and out-of-order USBMUX TCP data before TLS processing.
- Keeps wired type-101 navigation audio at the tested 48 kHz format.
- Enables smooth CarPlay music ducking during navigation prompts by default, with a setting to disable it.
- Restores the Android 7 real-device guide and local diagnostic/ADB helper scripts.

## Validation

- Complete shared, common and home unit-test suites passed.
- Mobile, home and map-host lint checks passed.
- Mobile, home and map-host debug builds passed for API 25+.
- The native USB reset library was packaged for armeabi-v7a, arm64-v8a and x86_64.
- The standalone APK was verified to contain the explicitly supplied local authentication assets.

## Test status and limitations

The USB recovery design and earlier revisions were exercised on the maintainer's Android tablet and Android 7.1 head unit. Because r19 replaces the source baseline with upstream 0.2.15, it still needs a final tablet smoke test and vehicle regression test before it should be treated as the preferred stable build.

Wireless CarPlay remains experimental on the maintainer's Android 7.1 head unit. USB host behavior, audio routing, Bluetooth, Wi-Fi and hardware decoding remain dependent on vendor firmware. If USB reset is rejected by the kernel and the attached iPhone is left in a stale mode, a physical unplug/replug may still be required.

This release does not include unmerged upstream development branches. Test only while parked and retain the previous working APK until wired connection, physical unplug/replug, software reconnect, wired/wireless switching, navigation audio and multi-iPhone switching have been checked.
