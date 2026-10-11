#!/usr/bin/env bash
set -euo pipefail

task_root=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)
cd "$task_root"
task_report=build/reports/meldrite/native-extension-audit.json
if [ "${1:-}" = "--report" ] && [ "$#" -ge 3 ]; then
    task_report=$2
    shift 2
elif [ "$#" -eq 1 ]; then
    task_plugins=$(realpath "$1")
    set -- "$task_plugins"/*.jar
else
    echo "Usage: bash tools/audit-native-extensions.sh plugin-directory | --report report.json plugin.jar [plugin.jar...]" >&2
    exit 2
fi
task_roots=$(find modules adapters/paper-1.20.1 versions/v1_20_1 -type d -path '*/build/classes/java/main' -printf '%p:' | sed 's/:$//')
task_asm=$(find ~/.gradle/caches/modules-2/files-2.1/org.ow2.asm -path '*/9.9.1/*' -name '*.jar' -printf '%p:' | tr -d '\n')
if [ -z "$task_asm" ]; then
    echo "Cached ASM 9.9.1 is required to scan the supplied Java 25 plugin classes" >&2
    exit 2
fi
task_cp="$task_asm$task_roots:versions/v1_20_1/mappings/resources:$(find .gradle/meldrite/artifacts -name '*.jar' -not -name '*sources*' -printf '%p:' | tr -d '\n')"
task_native=$(find .gradle/loom-cache/minecraftMaven -name '*e572a196b7*.jar' -not -name '*sources*' | head -1)
task_fabric_api=$(find ~/.gradle/caches/modules-2/files-2.1/net.fabricmc.fabric-api/fabric-api/0.92.12+1.20.1 \
    -name '*.jar' -not -name '*sources*' | head -1)
if [ -z "$task_fabric_api" ]; then
    task_fabric_api=$(find versions/v1_20_1/bootstrap/build/loom-cache/remapped_working \
        -name '*fabric-api-base*.jar' -not -name '*sources*' | head -1)
fi
if [ -z "$task_fabric_api" ]; then
    echo "Fabric API 0.92.12+1.20.1 or its remapped base module is required to index mixin targets" >&2
    exit 2
fi
task_tiny='.gradle/loom-cache/projects/v1201-bootstrap/source_mappings/e572a196b7aba5564ce413beb37426bab8d28887.tiny'
task_output=$(mktemp -d /tmp/meldrite-native-abi.XXXXXX)
trap 'rm -rf "$task_output"' EXIT
javac --release 17 -proc:none -cp "$task_cp" -d "$task_output" tools/NativeExtensionAudit.java
java -cp "$task_output:$task_cp" NativeExtensionAudit \
    "$task_report" "$task_native" "$task_tiny" "$task_roots" \
    versions/v1_20_1/bootstrap/src/main/resources/meldrite.mixins.json \
    versions/v1_20_1/bootstrap/src/main/resources/meldrite.accesswidener \
    .gradle/meldrite/artifacts/io.papermc.paper/paper-api/1.20.1-R0.1-SNAPSHOT/paper-api-1.20.1-R0.1-20230921.165944-178.jar \
    "$task_fabric_api" \
    "$@"
