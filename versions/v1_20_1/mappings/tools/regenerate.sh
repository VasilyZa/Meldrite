#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 5 ]]; then
    echo "Usage: $0 official-spigot.tiny intermediary.jar server_mappings.txt native-intermediary.jar official-server.jar" >&2
    exit 2
fi
repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../../.." && pwd)"
mapping_io="${MELDRITE_MAPPING_IO_JAR:-$repo_root/.gradle/meldrite/artifacts/net.fabricmc/mapping-io/0.6.1/mapping-io-0.6.1.jar}"
asm_jar="${MELDRITE_MAPPING_ASM_JAR:-$repo_root/.gradle/meldrite/artifacts/org.ow2.asm/asm/9.7.1/asm-9.7.1.jar}"
tools_classes="$repo_root/build/legacy-mapping-tools"
mapping_java="${MELDRITE_MAPPING_JAVA_HOME:-/usr/lib/jvm/java-17-openjdk}"
if [[ ! -x "$mapping_java/bin/java" || ! -x "$mapping_java/bin/javac" ]]; then
    echo "Set MELDRITE_MAPPING_JAVA_HOME to the Java 17 JDK used for mapping generation" >&2
    exit 2
fi
if ! "$mapping_java/bin/java" -version 2>&1 | head -1 | rg 'version "17\.' >/dev/null; then
    echo "Mapping generation requires Java 17 so gzip output matches the released artifact" >&2
    exit 2
fi
mkdir -p "$tools_classes"
"$mapping_java/bin/javac" --release 17 -cp "$mapping_io:$asm_jar" -d "$tools_classes" \
    "$repo_root/versions/v1_20_1/mappings/tools/GenerateLegacyMappings.java"
"$mapping_java/bin/java" -cp "$tools_classes:$mapping_io:$asm_jar" GenerateLegacyMappings "$@" \
    "$repo_root/versions/v1_20_1/mappings/resources/meldrite/mappings/1.20.1"
