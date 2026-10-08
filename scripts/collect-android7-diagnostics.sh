#!/usr/bin/env bash

# Collect host-side evidence around one DiPlay reproduction on an Android head unit.
# This script intentionally does not require root. Commands unavailable on an OEM ROM
# are recorded and skipped instead of aborting the whole capture.

set -u

PACKAGE="${DIPLAY_PACKAGE:-io.github.pinatsu.diplay.android7.hudtest}"
ACTIVITY="${DIPLAY_ACTIVITY:-com.shilapi.xcertplay.DiPlayActivity}"
ADB_BIN="${ADB:-adb}"
STAMP="$(date '+%Y%m%d-%H%M%S')"
OUTPUT_ROOT="${1:-diagnostics}"
OUTPUT_DIR="$OUTPUT_ROOT/diplay-$STAMP"

fail() {
    printf 'Error: %s\n' "$1" >&2
    exit 1
}

command -v "$ADB_BIN" >/dev/null 2>&1 || fail "adb was not found. Install Android platform-tools or set ADB=/full/path/to/adb."

DEVICE_LINES="$($ADB_BIN devices | awk 'NR > 1 && $2 == "device" { print $1 }')"
DEVICE_COUNT="$(printf '%s\n' "$DEVICE_LINES" | awk 'NF { count++ } END { print count+0 }')"
if [ "$DEVICE_COUNT" -eq 0 ]; then
    fail "No authorized Android device. Enable USB debugging, reconnect, and accept the RSA prompt."
fi
if [ "$DEVICE_COUNT" -gt 1 ] && [ -z "${ANDROID_SERIAL:-}" ]; then
    fail "More than one device is connected. Set ANDROID_SERIAL to the head-unit serial shown by: adb devices"
fi

mkdir -p "$OUTPUT_DIR" || fail "Could not create $OUTPUT_DIR"

run_capture() {
    name="$1"
    shift
    {
        printf '$'
        printf ' %q' "$@"
        printf '\n\n'
        "$@"
    } >"$OUTPUT_DIR/$name.txt" 2>&1 || true
}

{
    printf 'Capture started: %s\n' "$(date '+%Y-%m-%d %H:%M:%S %z')"
    printf 'Package: %s\n' "$PACKAGE"
    printf 'Serial: %s\n' "$($ADB_BIN get-serialno 2>/dev/null)"
    printf 'Product: %s %s\n' \
        "$($ADB_BIN shell getprop ro.product.manufacturer 2>/dev/null | tr -d '\r')" \
        "$($ADB_BIN shell getprop ro.product.model 2>/dev/null | tr -d '\r')"
    printf 'Android: %s (API %s)\n' \
        "$($ADB_BIN shell getprop ro.build.version.release 2>/dev/null | tr -d '\r')" \
        "$($ADB_BIN shell getprop ro.build.version.sdk 2>/dev/null | tr -d '\r')"
    printf 'Build: %s\n' "$($ADB_BIN shell getprop ro.build.display.id 2>/dev/null | tr -d '\r')"
} >"$OUTPUT_DIR/summary.txt"

run_capture package "$ADB_BIN" shell dumpsys package "$PACKAGE"
run_capture usb-before "$ADB_BIN" shell dumpsys usb
run_capture connectivity-before "$ADB_BIN" shell dumpsys connectivity
run_capture wifi-before "$ADB_BIN" shell dumpsys wifi

printf 'Recording logcat to %s/logcat-full.txt\n' "$OUTPUT_DIR"
printf 'The app will now open. Reproduce ONE issue, note the exact time, then return here.\n'
"$ADB_BIN" logcat -v threadtime >"$OUTPUT_DIR/logcat-full.txt" 2>&1 &
LOGCAT_PID=$!
"$ADB_BIN" shell am start -W -n "$PACKAGE/$ACTIVITY" >"$OUTPUT_DIR/launch.txt" 2>&1 || true

printf '\nPress Enter AFTER the issue has occurred to finish collection... '
read -r _

kill "$LOGCAT_PID" >/dev/null 2>&1 || true
wait "$LOGCAT_PID" >/dev/null 2>&1 || true

run_capture crash-buffer "$ADB_BIN" logcat -b crash -d -v threadtime
run_capture activity "$ADB_BIN" shell dumpsys activity activities
run_capture processes "$ADB_BIN" shell dumpsys activity processes
run_capture meminfo "$ADB_BIN" shell dumpsys meminfo "$PACKAGE"
run_capture gfxinfo "$ADB_BIN" shell dumpsys gfxinfo "$PACKAGE"
run_capture usb-after "$ADB_BIN" shell dumpsys usb
run_capture connectivity-after "$ADB_BIN" shell dumpsys connectivity
run_capture wifi-after "$ADB_BIN" shell dumpsys wifi
run_capture dropbox "$ADB_BIN" shell dumpsys dropbox --print
run_capture tombstone-list "$ADB_BIN" shell ls -l /data/tombstones
run_capture anr-list "$ADB_BIN" shell ls -l /data/anr

printf '\nCreate an Android bugreport too? It is strongly recommended after a crash/ANR. [Y/n] '
read -r BUGREPORT_REPLY
case "$BUGREPORT_REPLY" in
    n|N|no|NO)
        printf 'Bugreport skipped.\n'
        ;;
    *)
        printf 'Generating bugreport; this can take several minutes...\n'
        "$ADB_BIN" bugreport "$OUTPUT_DIR/bugreport" >"$OUTPUT_DIR/bugreport-command.txt" 2>&1 || true
        ;;
esac

printf 'Capture finished: %s\n' "$(date '+%Y-%m-%d %H:%M:%S %z')" >>"$OUTPUT_DIR/summary.txt"
printf '\nDone: %s\n' "$OUTPUT_DIR"
printf 'Before sharing, review the files: full logcat and bugreport can contain device, Wi-Fi, location, account, and notification data.\n'
