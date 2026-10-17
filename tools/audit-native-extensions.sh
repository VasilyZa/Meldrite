#!/usr/bin/env bash
set -euo pipefail

task_root=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)
cd "$task_root"
task_report=build/reports/meldrite/native-extension-audit.json
task_native=
task_mappings=
task_runtime_requests=
task_positionals=()

while (($#)); do
    case "$1" in
        --report|--native|--mappings|--runtime-requests)
            if (($# < 2)); then
                echo "Missing value for $1" >&2
                exit 2
            fi
            case "$1" in
                --report) task_report=$2 ;;
                --native) task_native=$2 ;;
                --mappings) task_mappings=$2 ;;
                --runtime-requests) task_runtime_requests=$2 ;;
            esac
            shift 2
            ;;
        --help|-h)
            cat <<'USAGE'
Usage: bash tools/audit-native-extensions.sh [--report report.json]
       [--native minecraft-merged.jar] [--mappings yarn.tiny]
       [--runtime-requests requests.json] plugin-directory | plugin.jar [plugin.jar...]

Without --native, the script requires exactly one matching 1.20.1 Yarn merged
jar in the Loom cache. Without --mappings, it requires exactly one Tiny mapping
paired with the selected merged-jar cache hash. Ambiguous caches require explicit
paths. This script does not invoke Gradle or download dependencies.
USAGE
            exit 0
            ;;
        *) task_positionals+=("$1"); shift ;;
    esac
done

if ((${#task_positionals[@]} == 0)); then
    echo "At least one plugin directory or JAR is required; use --help for usage" >&2
    exit 2
fi
if ((${#task_positionals[@]} == 1)) && [[ -d "${task_positionals[0]}" ]]; then
    task_plugin_directory=$(realpath "${task_positionals[0]}")
    shopt -s nullglob
    task_positionals=("$task_plugin_directory"/*.jar)
    shopt -u nullglob
fi
if ((${#task_positionals[@]} == 0)); then
    echo "No plugin JARs found" >&2
    exit 2
fi
for task_plugin in "${task_positionals[@]}"; do
    if [[ ! -f "$task_plugin" ]]; then
        echo "Plugin JAR does not exist: $task_plugin" >&2
        exit 2
    fi
done

task_native_cache=.gradle/loom-cache/minecraftMaven/net/minecraft
task_mapping_cache=.gradle/loom-cache/projects/v1201-bootstrap/source_mappings
task_native_candidates=()
if [[ -z "$task_native" ]]; then
    if [[ -d "$task_native_cache" ]]; then
        mapfile -d '' task_native_candidates < <(find "$task_native_cache" -type f \
            -path '*/1.20.1-net.fabricmc.yarn*1_20_1.1.20.1+build.10-v2/*.jar' \
            -name 'minecraft-merged-*.jar' -not -name '*sources*' -print0 | sort -z)
    fi
    if ((${#task_native_candidates[@]} != 1)); then
        echo "Expected one cached Minecraft 1.20.1 Yarn merged JAR; found ${#task_native_candidates[@]}." >&2
        printf '  %s\n' "${task_native_candidates[@]}" >&2
        echo "Specify the intended artifact with --native <path>. No cache candidate was selected." >&2
        exit 2
    fi
    task_native=${task_native_candidates[0]}
fi
if [[ ! -f "$task_native" ]]; then
    echo "Minecraft native JAR does not exist: $task_native" >&2
    exit 2
fi
task_native=$(realpath "$task_native")

task_mapping_candidates=()
if [[ -z "$task_mappings" ]]; then
    task_native_hash=$(basename "$(dirname "$(dirname "$task_native")")")
    task_native_hash=${task_native_hash#minecraft-merged-}
    if [[ "$task_native_hash" != "$(basename "$(dirname "$(dirname "$task_native")")")" && -d "$task_mapping_cache" ]]; then
        mapfile -d '' task_mapping_candidates < <(find "$task_mapping_cache" -maxdepth 1 -type f \
            -name "${task_native_hash}*.tiny" -print0 | sort -z)
    fi
    if ((${#task_mapping_candidates[@]} != 1)); then
        echo "Expected one Yarn Tiny mapping paired with native cache hash '$task_native_hash'; found ${#task_mapping_candidates[@]}." >&2
        printf '  %s\n' "${task_mapping_candidates[@]}" >&2
        echo "Specify the intended mapping with --mappings <path>. No mapping candidate was selected." >&2
        exit 2
    fi
    task_mappings=${task_mapping_candidates[0]}
fi
if [[ ! -f "$task_mappings" ]]; then
    echo "Yarn mappings file does not exist: $task_mappings" >&2
    exit 2
fi
task_mappings=$(realpath "$task_mappings")

task_roots=$(find modules adapters/paper-1.20.1 versions/v1_20_1 -type d -path '*/build/classes/java/main' -printf '%p:' | sed 's/:$//')
task_asm=$(find "$HOME/.gradle/caches/modules-2/files-2.1/org.ow2.asm" -path '*/9.9.1/*' -name '*.jar' -printf '%p:' 2>/dev/null | tr -d '\n')
if [[ -z "$task_asm" ]]; then
    echo "Cached ASM 9.9.1 is required to scan the supplied Java 25 plugin classes" >&2
    exit 2
fi
task_cached_artifacts=$(find .gradle/meldrite/artifacts -name '*.jar' -not -name '*sources*' -printf '%p:' | tr -d '\n')
task_cp="$task_asm$task_roots:versions/v1_20_1/mappings/resources:$task_cached_artifacts"

task_fabric_api=$(find "$HOME/.gradle/caches/modules-2/files-2.1/net.fabricmc.fabric-api/fabric-api/0.92.12+1.20.1" \
    -name '*.jar' -not -name '*sources*' -print -quit 2>/dev/null || true)
if [[ -z "$task_fabric_api" ]]; then
    task_fabric_api=$(find versions/v1_20_1/bootstrap/build/loom-cache/remapped_working \
        -name '*fabric-api-base*.jar' -not -name '*sources*' -print -quit 2>/dev/null || true)
fi
if [[ -z "$task_fabric_api" ]]; then
    echo "Fabric API 0.92.12+1.20.1 or its remapped base module is required to index mixin targets" >&2
    exit 2
fi
task_paper=.gradle/meldrite/artifacts/io.papermc.paper/paper-api/1.20.1-R0.1-SNAPSHOT/paper-api-1.20.1-R0.1-20230921.165944-178.jar
if [[ ! -f "$task_paper" ]]; then
    echo "Cached Paper API is required: $task_paper" >&2
    exit 2
fi

task_output=$(mktemp -d /tmp/meldrite-native-abi.XXXXXX)
trap 'rm -rf "$task_output"' EXIT
javac --release 17 -proc:none -cp "$task_cp" -d "$task_output" tools/NativeExtensionAudit.java
task_runtime_args=()
if [[ -n "$task_runtime_requests" ]]; then
    task_runtime_args=(--runtime-requests "$task_runtime_requests")
fi
java -cp "$task_output:$task_cp" NativeExtensionAudit \
    "${task_runtime_args[@]}" \
    "$task_report" "$task_native" "$task_mappings" "$task_roots" \
    versions/v1_20_1/bootstrap/src/main/resources/meldrite.mixins.json \
    versions/v1_20_1/bootstrap/src/main/resources/meldrite.accesswidener \
    "$task_paper" "$task_fabric_api" \
    "${task_positionals[@]}"
