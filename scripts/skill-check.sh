#!/usr/bin/env sh
# Checks the Agent Skill in skills/onlinepayments-sdk-client-android/:
# 1. compiles the Kotlin and Java example files against the SDK sources of this repository
#    (the unpublished Gradle module :skill-examples; any compiler warning fails the check);
# 2. with --lint: also runs Android lint on the examples (about 1.5 minutes more);
# 3. fails if a code block in the skill's Markdown or a versioned skill link is out of date.
#
# Run from the repository root: sh scripts/skill-check.sh [--lint]
# From PowerShell (Git for Windows): bash scripts/skill-check.sh [--lint]
# Needs JDK 17 or newer (JAVA_HOME or "java" on the PATH), Node.js, and the Android SDK (ANDROID_HOME,
# ANDROID_SDK_ROOT or local.properties; on Windows the default location is tried).
set -e

cd "$(dirname "$0")/.."

if [ -z "$ANDROID_HOME" ] && [ -z "$ANDROID_SDK_ROOT" ] && [ ! -f local.properties ]; then
  for candidate in "$LOCALAPPDATA/Android/Sdk" "$HOME/Library/Android/sdk" "$HOME/Android/Sdk"; do
    if [ -d "$candidate" ]; then
      ANDROID_HOME="$candidate"
      export ANDROID_HOME
      break
    fi
  done
fi

TASKS=":skill-examples:compileDebugKotlin :skill-examples:compileDebugJavaWithJavac"
if [ "$1" = "--lint" ]; then
  TASKS="$TASKS :skill-examples:lintDebug"
fi

# shellcheck disable=SC2086
./gradlew $TASKS --console=plain
node scripts/skill-sync.mjs check
