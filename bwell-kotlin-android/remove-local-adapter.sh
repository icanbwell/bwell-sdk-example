#!/usr/bin/env bash
# Health Sync on-device adapter - LOCAL TEST OVERLAY, cleanup.
#
# Symmetrical counterpart to ./enable-local-adapter.sh: removes the
# generated overlay artifacts so the project falls back to the committed,
# vendor-neutral no-op (src/healthSyncNeutral) - exactly what a fresh clone
# builds with no local setup at all.
#
# Deliberately leaves healthsync-local.properties alone: it holds your
# JFrog credentials, not adapter wiring, so there's no reason to make you
# re-enter them the next time you run ./enable-local-adapter.sh. Delete it
# yourself if you want that cleared too.
#
# Run with -h/--help for usage.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

APPLICATION_ID="com.bwell.sampleapp"

usage() {
  cat <<EOF
Usage: ./remove-local-adapter.sh [-r|--run]

Removes the gitignored local-adapter overlay:
  - healthsync-local.settings.gradle.kts
  - healthsync-local.app.gradle.kts
  - app/src/healthSyncLocal/

healthsync-local.properties (your credentials) is left in place - see the
note at the top of this script if you want it removed too.

  -r, --run    After removing the overlay, also rebuild the debug APK and
               reinstall it (adb install -r) on the currently connected
               device/emulator, so you can confirm the vendor-neutral
               no-op is actually what's running.
EOF
}

run_after=0
for arg in "$@"; do
  case "$arg" in
    -h|--help)
      usage
      exit 0
      ;;
    -r|--run)
      run_after=1
      ;;
    *)
      echo "error: unrecognized argument '$arg'" >&2
      echo >&2
      usage >&2
      exit 1
      ;;
  esac
done

removed_any=0

if [[ -f healthsync-local.settings.gradle.kts ]]; then
  rm healthsync-local.settings.gradle.kts
  echo "removed: healthsync-local.settings.gradle.kts"
  removed_any=1
fi

if [[ -f healthsync-local.app.gradle.kts ]]; then
  rm healthsync-local.app.gradle.kts
  echo "removed: healthsync-local.app.gradle.kts"
  removed_any=1
fi

if [[ -d app/src/healthSyncLocal ]]; then
  rm -rf app/src/healthSyncLocal
  echo "removed: app/src/healthSyncLocal/"
  removed_any=1
fi

echo
if [[ "$removed_any" -eq 0 ]]; then
  echo "Nothing to remove - project was already in the clean, vendor-neutral state."
else
  echo "Done - project restored to the clean, vendor-neutral state (src/healthSyncNeutral)."
  echo "healthsync-local.properties was left in place; delete it yourself if you also want your saved credentials cleared."
fi

if [[ "$run_after" -eq 0 ]]; then
  echo
  echo "Run with --run to also rebuild + reinstall now, or manually:"
  echo "  ./gradlew --stop && ./gradlew :app:assembleDebug"
  exit 0
fi

echo
echo "Rebuilding and reinstalling (--run)..."
echo

./gradlew --stop
./gradlew :app:assembleDebug

apk="$(find app/build/outputs/apk/debug -iname '*.apk' ! -iname '*androidTest*' | head -1)"
if [[ -z "$apk" ]]; then
  echo "error: build succeeded but no debug APK was found under app/build/outputs/apk/debug" >&2
  exit 1
fi

if ! command -v adb >/dev/null 2>&1; then
  echo "error: adb not found on PATH - build succeeded, but can't install/launch automatically" >&2
  echo "APK is at: $apk"
  exit 1
fi

if [[ -z "$(adb devices | awk 'NR>1 && $2=="device" {print}')" ]]; then
  echo "error: no connected device/emulator (adb devices) - build succeeded, but can't install/launch" >&2
  echo "APK is at: $apk"
  exit 1
fi

adb install -r "$apk"
adb shell monkey -p "$APPLICATION_ID" -c android.intent.category.LAUNCHER 1 &>/dev/null

echo
echo "Done - $APPLICATION_ID reinstalled and launched, vendor-neutral (no adapter)."
