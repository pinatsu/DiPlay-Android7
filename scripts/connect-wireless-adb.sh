#!/usr/bin/env bash
# Discover the current TLS connection port of an already-paired Android device.
# Does not change USB roles, enable legacy TCP debugging, or modify pairing.
set -u

ADB_BIN="${ADB:-adb}"
TARGET_IP="${1:-}"
if [ -z "$TARGET_IP" ]; then
    printf 'Usage: bash %s DEVICE_IP\n' "$0" >&2
    exit 2
fi
command -v "$ADB_BIN" >/dev/null 2>&1 || {
    printf 'adb not found; set ADB to its executable path.\n' >&2
    exit 2
}
"$ADB_BIN" start-server || exit 1

# Bounded discovery: give mDNS time to populate after starting the server.
for attempt in 1 2 3 4 5 6; do
    endpoints="$("$ADB_BIN" mdns services | awk -v ip="$TARGET_IP" '
        $2 ~ /^_adb-tls-connect\._tcp\.?$/ && index($3, ip ":") == 1 { print $3 }
    ')"
    while IFS= read -r endpoint; do
        [ -n "$endpoint" ] || continue
        "$ADB_BIN" connect "$endpoint"
        if [ "$("$ADB_BIN" -s "$endpoint" get-state 2>/dev/null)" = device ]; then
            printf 'Connected: %s\n' "$endpoint"
            exit 0
        fi
    done <<< "$endpoints"
    [ "$attempt" -eq 6 ] || sleep 2
done

printf 'No usable wireless ADB service found for %s.\n' "$TARGET_IP" >&2
printf 'Enable Wireless debugging, use the same LAN, and check device pairing / multicast isolation.\n' >&2
exit 1

