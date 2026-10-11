# DiPlay Android 7 fork contract

This is the authoritative maintenance contract for behavior intentionally carried by the
Android 7 fork. It describes outcomes, not a requirement to preserve a particular old diff.
When upstream provides an equivalent or better implementation, use it and retain the
acceptance criteria below.

The target environment is Android 7.1 / API 25. Wired CarPlay is the primary supported
path. Wireless behavior follows upstream but remains experimental on the maintainer's
target head unit.

## Maintenance rules

- Treat each `Fxx` item as an independently reviewable feature and keep its implementation,
  tests and required translations in one commit when practical.
- Do not silently remove a requirement during an upstream merge.
- Do not restore whole old files over newer upstream code. Reapply the required behavior to
  the current design and preserve upstream fixes.
- Record device evidence separately from automated validation. Tablet success is not vehicle
  acceptance.
- Authentication assets, signing keys, APKs and private diagnostics never belong in Git.

## F01 — Independent application and update channel

**Status:** Required
**Current commit:** `7197643f`

The fork uses application ID `io.github.pinatsu.diplay.android7` for every build type. It can
coexist with upstream DiPlay, and its settings and pairing data must not share upstream
storage. Update checks must use the fork's release feed and compare both the upstream
semantic version and `android7-rN` revision. The tablet-tested APK is the release artifact;
it is not rebuilt or renamed after acceptance.

Acceptance:

- API 25 installs and launches with the expected package and version.
- A matching-signature fork build upgrades in place without clearing settings.
- The updater never offers an upstream APK or an older fork revision.

## F02 — Personal vehicle branding

**Status:** Required personal customization
**Current commit:** `17877301`

The default CarPlay OEM label is `TOYOTA`, and the default vehicle button uses the
maintainer's custom Toyota image. Branding must remain separate from protocol behavior and
must not overwrite updated upstream license or attribution notices.

Acceptance:

- A fresh install presents the expected label and image.
- Existing user-selected vehicle images continue to take precedence.

## F03 — Wired USB reset and software reconnect

**Status:** Required reliability feature
**Current commit:** `8a3cbca2`

Starting a new wired session should attempt one bounded USB reset and re-enumeration when
the kernel supports it, so a software disconnect, settings change or failed connection can
recover without a physical unplug. Unsupported reset paths must fall back to the ordinary
wired flow. Old broadcasts, callbacks and timeouts must not terminate a newer session.

Acceptance:

- First wired connection succeeds without an unnecessary reset loop.
- Application disconnect/reconnect and settings reconnect work without unplugging when the
  kernel supports reset.
- Reset failure falls back cleanly; retries are bounded.
- Rapid reconnects do not let stale callbacks close the current generation.
- Physical unplug/replug still works.

Automated evidence includes `UsbDeviceResetTest`, `WiredReenumerationDecisionTest` and
`NcmFunctionDiscoveryTest`.

## F04 — Missing USB-detach fallback

**Status:** Required reliability feature
**Current commit:** `8e4a750b`

Some vendor Android builds omit `USB_DEVICE_DETACHED`. During an active wired session, a
lightweight presence check must close the stale session after the connected iPhone is
consistently absent. A transient enumeration failure, normal re-enumeration or removal of a
different USB device must not terminate the session.

Acceptance:

- Removing the active iPhone clears the stale CarPlay picture promptly.
- USB reset/re-enumeration is not mistaken for a physical unplug.
- Temporary discovery errors do not disconnect an active drive.

## F05 — Pairing records per iPhone

**Status:** Required reliability feature
**Current commit:** `d3b85fcc`

Lockdown pairing records are keyed by a digest of each iPhone's stable device identifier.
Switching A → B → A must preserve both records. Only the record explicitly rejected with
`InvalidPairRecord` or `InvalidHostID` may be cleared. Device identifiers, certificates and
private keys must not be written to diagnostics.

Acceptance:

- Each phone completes its normal first trust/pairing flow.
- Returning to a previously paired phone reuses its own record.
- Rejecting one phone's record does not delete another phone's record.
- Persistence and rejection tests pass.

## F06 — Damaged NCM and USBMUX traffic protection

**Status:** Required wired reliability feature
**Current commit:** `b2d5daff`

The wired transport must reject or recover from narrowly identified malformed NCM blocks
and must not deliver duplicate, overlapping or out-of-order USBMUX TCP bytes to TLS. Normal
frames and valid retransmission behavior must remain intact.

Acceptance:

- Sequential, duplicate, overlap, out-of-order, wraparound and FIN/SYN-ACK cases pass.
- A malformed block does not automatically destroy an otherwise recoverable session.
- Long-running wired media does not duplicate or silently drop delivered TLS bytes.

Automated evidence includes `UsbMuxTcpReceiveDispositionTest` and the NCM framing tests.

## F07 — Wired navigation audio at 48 kHz

**Status:** Required target-device audio behavior
**Current commit:** `b8a77ede`

Wired type-101 navigation audio advertises the target-device-compatible 48 kHz stereo PCM
format. Wireless sessions and unrelated audio types retain upstream negotiation. This does
not alter Android's system volume.

Acceptance:

- Wired `/info` advertises the intended type-101 format.
- Wireless and non-navigation format lists retain upstream capabilities.
- Navigation, media, calls and Siri have no new silence or format mismatch.

Automated evidence includes `AirPlayInfoPlistTest`.

## F08 — Navigation prompt media ducking

**Status:** Required, user-configurable
**Current commit:** `7c4f5670`

When CarPlay navigation audio becomes active, CarPlay media should ramp to approximately
30 percent and return smoothly when the prompt ends. The feature is enabled by default and
can be disabled under Audio routing. It changes only the CarPlay media track and must not
raise muted media or the head unit's global volume.

Acceptance:

- Start/end, rapid short prompts and repeated prompts restore the previous media gain.
- Disabled mode leaves media unchanged.
- Calls, audio-focus changes and renderer replacement do not cause an unexpected volume
  increase.
- Every supported locale contains the setting text.

Automated evidence includes `NavigationDuckingEnvelopeTest` and audio renderer tests.

## F09 — Explicit standalone authentication packaging

**Status:** Required release safety rule
**Current commit:** `2bd92b0b`

Ordinary source and CI builds contain no accessory identity. A standalone car-test or
release APK requires an explicit external `DIPLAY_AUTH_ASSETS_DIR` containing exactly the
expected runtime authentication inputs. Signing and authentication material remain outside
Git and public source archives.

Acceptance:

- Source-only debug and CI builds succeed without private inputs.
- `assembleStandaloneDebug` fails when inputs are absent or incomplete.
- A candidate built with explicit inputs contains the two expected runtime assets and no
  keystore or extra credential container.
- `scripts/check_public_tree.py` passes.

## Release gates

Before a candidate becomes a release:

1. The full command in `AGENTS.md` passes.
2. `scripts/check_public_tree.py` passes.
3. Package, version, minimum SDK, signing identity and runtime assets are verified.
4. The tablet smoke test passes without crash or obvious UI regression.
5. Wired vehicle tests cover initial connection, software reconnect, physical unplug/replug,
   A → B → A phone switching, navigation audio and navigation ducking.
6. Known limitations and actual evidence are recorded; untested behavior is not described as
   confirmed.
7. Release commit, tag, push and publication occur only after explicit user approval.

## Supporting documents

- `ANDROID7_REAL_DEVICE_TEST_ZH.md` — device test procedure.
- `CONNECTION_RELIABILITY.md` — connection evidence and remaining investigations.
- `RELEASE-NOTES-ANDROID7-R19.md`, `RELEASE-NOTES-ANDROID7-R20.md` and
  `RELEASE-NOTES-ANDROID7-R21.md` — fork release history.
- `../../reference/DiPlay-Android7-上游同步与补丁迁移指南.md` — historical migration audit;
  useful background, but this contract takes precedence for the current fork.
