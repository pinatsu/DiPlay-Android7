# DiPlay Android 7 r15

Version `0.2.13-android7-r15` (versionCode `3215`) is a focused connection-recovery update for the Android 7 fork.

## Changes

- Reset and re-enumerate an already attached iPhone when an existing wireless CarPlay session switches to wired CarPlay.
- Retain the existing reset path for a wired software disconnect followed by a wired reconnect.
- Keep the initial physical wired connection on the direct path without an unnecessary second USB reset.

The wireless-to-wired failure and the recovery after a reset were reproduced on the maintainer's Android tablet. The release still requires regression testing on the target Android 7.1 head unit, especially wireless-to-wired switching, ordinary first wired connection, physical unplug/replug, audio, and multi-iPhone switching.

## Known limitation

Direct session replacement and wired-to-wired software reconnects use the new reset path. If a wireless session is first stopped with the separate **Disconnect** action and wired CarPlay is started afterwards, the process-local reset marker can be cleared before wired startup. That sequence may still remain on the preparation screen until another wired reconnect or physical replug. A follow-up revision will preserve the marker across this two-step workflow.
