# DiPlay Android 7 r21

Version `0.2.17-android7-r21` updates the Android 7 fork from upstream DiPlay 0.2.15 to
0.2.17 while preserving the fork's independent package and vehicle-specific behavior.

## Fork changes

- Reconnect wired CarPlay without unplugging when the head-unit kernel supports USB reset.
- Detect a removed iPhone when vendor Android omits the USB detach event.
- Keep separate wired pairing records for multiple iPhones.
- Tolerate the identified damaged NCM blocks and duplicate or overlapping USBMUX traffic.
- Use the target-compatible 48 kHz wired navigation format.
- Optionally duck CarPlay music during navigation prompts.
- Keep the maintainer's default vehicle name and vehicle-button image.
- Use the independent package `io.github.pinatsu.diplay.android7` and fork release feed.

## Upstream base

The complete upstream 0.2.17 application is included. This brings the upstream connection,
audio, display, settings, diagnostics, update, controller and BYD integration changes from
0.2.16 and 0.2.17. See the upstream [0.2.17 release notes](RELEASE-NOTES-0.2.17.md) for
that broader change list.

## Packaging

- App name: `DiPlay`
- Package: `io.github.pinatsu.diplay.android7`
- Version: `0.2.17-android7-r21`
- Version code: `3621`
- Minimum Android version: Android 7.1 / API 25

The release APK uses the same Android signing certificate as r20, so it can upgrade r20 in
place. Runtime CarPlay authentication assets are supplied only during the local release
build and are not stored in the public Git repository.

## Validation and limits

- The complete source workflow in `AGENTS.md` passed with JDK 25.
- The public-tree scan, signed release build, artifact metadata, signature, runtime assets
  and tablet launch are verified during release packaging.
- Wired CarPlay remains the primary supported path for the maintainer's Android 7.1 head
  unit. Wireless remains experimental there.
- Passing automated checks and a tablet smoke test does not establish compatibility with
  every vehicle or replace a complete vehicle regression.
