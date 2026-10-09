# DiPlay Android 7 r20

Version `0.2.15-android7-r20` (versionCode `3420`) is a focused reliability update for the wired USB reset path.

## Changes

- Stops the five-second USB reset discovery timeout as soon as the iPhone is rediscovered, leaving the existing permission flow to handle delayed USB authorization.
- Uses the reset attempt's existing generation token so an early USB attach broadcast cannot be followed by a stale timeout.
- Converts native USB reset linkage failures into the existing reset-failure result, allowing the normal wired connection fallback to continue.

All other Android 7 fork behavior remains unchanged from r19.

## Validation

- Shared, common and home unit-test suites.
- Mobile, home and map-host lint checks.
- Mobile, home and map-host debug builds for API 25+.
- Release APK packaging and signing, including explicit local runtime authentication assets.
