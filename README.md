[**English**](README.md) | [简体中文](README.zh-CN.md)

# DiPlay Android 7

A personal-use-focused fork of [DiPlay](https://github.com/shihabal3amri/DiPlay), maintained primarily for an Android 7.1 head unit.

This fork keeps upstream DiPlay 0.2.15 and focuses on five differences that are visible in everyday wired CarPlay use: reconnecting without unplugging the cable, reliable unplug detection, improved navigation audio, switching between multiple iPhones, and optional music ducking during navigation prompts.

> [!IMPORTANT]
> Fixes and defaults prioritize the maintainer's own Android 7.1 head unit. Other devices are welcome to try it, but broad compatibility and support are not guaranteed.

[Download r20](https://github.com/pinatsu/DiPlay-Android7/releases/tag/v0.2.15-android7-r20) · [Report a problem](https://github.com/pinatsu/DiPlay-Android7/issues) · [Upstream DiPlay](https://github.com/shihabal3amri/DiPlay)

## Key differences from upstream

### Wired connection reliability

#### 1. Reconnect without unplugging the cable

Starting a new wired CarPlay session performs a USB reset and re-enumeration when the head-unit kernel supports it. This improves recovery after a software disconnect, settings change or failed connection without requiring the cable to be physically removed and inserted again.

If USB reset is unavailable, DiPlay falls back to the normal connection path. A physical reconnect may still be required on unsupported or vendor-modified kernels.

#### 2. More reliable unplug detection

Some Android head units do not reliably report that an iPhone has been unplugged, leaving the old CarPlay picture on screen. This fork adds a lightweight fallback check so a missing iPhone can be detected and the stale session can close even when Android omits the USB-detached event.

#### 3. Switching between multiple iPhones

Wired pairing records are stored separately for each iPhone. After each phone completes its initial trust and pairing process, switching phones no longer overwrites another phone's saved record.

This does not bypass the iPhone's initial trust prompt or normal USB device detection.

### Navigation audio experience

#### 4. Improved wired navigation audio

Wired navigation audio uses the 48 kHz format verified on the maintainer's devices. This improves compatibility with head units where navigation prompts were noticeably quieter or negotiated an unsuitable audio format.

It does not change Android's system volume and cannot guarantee identical perceived loudness on every head unit.

#### 5. Optional music ducking during navigation

When navigation audio starts, CarPlay music smoothly drops to approximately 30%. Music returns to full volume after the prompt finishes. Navigation ducking is enabled by default and can be disabled in **Settings → Audio routing**.

Only CarPlay's media track is adjusted; the head unit's global volume is not changed.

## Other visible differences

- Independent package: `io.github.pinatsu.diplay.android7`. It can coexist with upstream DiPlay, and its settings and pairing records remain separate.
- Personal branding: the default CarPlay vehicle label is `TOYOTA`, with the maintainer's custom Toyota vehicle button.
- Wired CarPlay is the primary target. Wireless functionality is inherited from upstream but remains experimental on the maintainer's Android 7.1 head unit.

## Relationship with upstream

Release r20 is based on the complete upstream [DiPlay 0.2.15](https://github.com/shihabal3amri/DiPlay/releases/tag/v0.2.15) source. Upstream already supports Android 7.1 / API 25; this fork does not claim to add API 25 support itself.

General interface, display, wireless, dashboard, diagnostics and media changes are kept from upstream. The fork maintains only the focused behavior above and integrates future upstream releases semantically instead of carrying a separate copy of every upstream feature.

- [Fork r20 release notes](docs/RELEASE-NOTES-ANDROID7-R20.md)
- [Upstream 0.2.15 release notes](docs/RELEASE-NOTES-0.2.15.md)
- [Validation record](docs/VALIDATION.md)
