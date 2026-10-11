#!/usr/bin/env bash
set -euo pipefail
if [ "$#" -lt 2 ] && [ "${1:-}" != "--self-test" ]; then
    echo "Usage: bash tools/audit-plugin-apis.sh report.json plugin.jar [plugin.jar...]" >&2
    exit 2
fi
task_report=$1
shift
task_root=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)
cd "$task_root"
task_roots=$(find modules adapters/paper-1.20.1 versions/v1_20_1 -type d -path '*/build/classes/java/main' -printf '%p:' | sed 's/:$//')
task_asm=$(find "${HOME}/.gradle/caches/modules-2/files-2.1/org.ow2.asm" -path '*/9.9.1/*' -name '*.jar' -printf '%p:' | tr -d '\n')
task_json=$(find .gradle/meldrite/artifacts/com.fasterxml.jackson.core -name '*.jar' -printf '%p:' | tr -d '\n')
task_api=.gradle/meldrite/artifacts/io.papermc.paper/paper-api/1.20.1-R0.1-SNAPSHOT/paper-api-1.20.1-R0.1-20230921.165944-178.jar
task_adventure=.gradle/meldrite/artifacts/net.kyori/adventure-api/4.14.0/adventure-api-4.14.0.jar
if [ -z "$task_asm" ] || [ -z "$task_roots" ] || [ ! -f "$task_api" ]; then
    echo "Cached ASM 9.9.1, compiled 1.20.1 classes, and the locked Paper API are required" >&2
    exit 2
fi
task_output=$(mktemp -d /tmp/meldrite-plugin-api-audit.XXXXXX)
trap 'rm -rf "$task_output"' EXIT
task_cp="$task_asm$task_json"
javac --release 17 -proc:none -cp "$task_cp" -d "$task_output" tools/PluginApiAudit.java tools/PluginApiAuditTest.java
if [ "$task_report" = "--self-test" ]; then
    java -cp "$task_output:$task_cp" PluginApiAuditTest
    exit 0
fi
if [ -f "$task_adventure" ]; then task_api="$task_api:$task_adventure"; fi
java -cp "$task_output:$task_cp" PluginApiAudit "$task_report" \
    org/bukkit/craftbukkit/v1_20_R1 "$task_roots" "$task_api" "$@"
