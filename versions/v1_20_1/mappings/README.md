# Minecraft 1.20.1 Legacy Mappings

`resources/meldrite/mappings/1.20.1/legacy-names.bin.gz` is packaged in the Paper adapter.
It maps Spigot and Mojang classes and members to Fabric intermediary symbols. The
bootstrap binds the intermediary table to the active Fabric namespace, including
the development named namespace. No mapping library is required at runtime.

Spigot runtime plugins use Spigot class names with **raw official member names**.
The BuildData-derived Tiny destination member names are development names. The
generator supports both, plus Mojang readable members, with raw runtime symbols
winning the few ambiguous names. Spigot class names similarly take precedence
when a Mojang class name identifies a different type. Every conflict is recorded
in `rejected-members.tsv`.

The source inputs are the clean Paper upstream `official-spigot.tiny`, Fabric's
official intermediary artifact, Mojang's server mappings, the Loom intermediary
Minecraft jar, and the official extracted server jar. `provenance.properties`
records their sources, hashes, the BuildData commit, output hash, and counts.
The generator checks every emitted field and method against **both** real jars,
using descriptors, inherited declarations, and native member access flags. The
MLM3 resource retains these flags so plugin methods only inherit a remapped name
when they override a real public or protected instance method; similarly named
native private and static methods cannot rename plugin interface implementations.
Client-only classes are excluded
from the emitted native hierarchy. Mapping availability is not an access widener;
the bootstrap separately supplies access changes required by the Spigot ABI.

Regenerate with Java 17 and the already cached mapping-io 0.6.1 and ASM
9.7.1 artifacts:

```sh
bash versions/v1_20_1/mappings/tools/regenerate.sh \
  /path/to/official-spigot.tiny /path/to/intermediary-1.20.1-v2.jar \
  /path/to/server_mappings.txt /path/to/minecraft-intermediary.jar \
  /path/to/minecraft-extracted_server.jar
```

Override `MELDRITE_MAPPING_IO_JAR` and `MELDRITE_MAPPING_ASM_JAR` to use another
artifact location, and `MELDRITE_MAPPING_JAVA_HOME` to select a Java 17 JDK when
`/usr/lib/jvm/java-17-openjdk` is unavailable. Output ordering and gzip metadata
are deterministic with this JDK generation; newer JDKs can compress identical
table bytes differently, so the generator launcher enforces Java 17.

`NativeMappingExecutionTest` compiles Java source against a genuine Spigot 1.20.1
runtime jar and executes the transformed bytecode against the native intermediary
jar. It checks fields, overloaded methods, native and plugin inheritance, lazy
class loading, overrides, local declarations, independent plugin interface
methods matching native private/static names, and reflective member lookups.
The Gradle properties `legacySpigotJar`, `legacyNativeJar`, and `legacyLibraryCache`
override the default local BuildTools/Loom artifact locations. The separate
`NamedNativeMappingProbe` runs the same test against the development named jar
using a Yarn mapping-io tree as the runtime namespace binding.

`reflection-names.tiny.gz` retains a separate, unambiguous Mojang-to-intermediary
view of the same server-verified declarations. `LegacyMappings.writePaperReflectionMappings`
exports it to the active Fabric namespace. `NativeReflectionMappings` publishes this
as Paper's standard `META-INF/mappings/reobf.tiny` resource before plugin discovery.
The destination column is named `spigot` to follow that resource protocol; its
values are the actual runtime symbols, including Yarn names during development.
This lets consumers map enumerated reflection members without changing `Field.getName()`
or `Method.getName()`. The resource has its own checksum in `provenance.properties`.

## Source Notices

Fabric intermediary is distributed under CC0-1.0. The Spigot mapping chain is
derived from official BuildData commit
`faff587dcbe915bc565bf01f2d54c6af86039414` and Paper's clean upstream mapping output.
No Taiyitist, Forge, SRG, or plugin-specific member table is used.

Mojang's mapping input carries this notice:

> (c) 2020 Microsoft Corporation. These mappings are provided "as-is" and you bear
> the risk of using them. You may copy and use the mappings for development
> purposes, but you may not redistribute the mappings complete and unmodified.
> Microsoft makes no warranties, express or implied, with respect to the mappings
> provided here. Use and modification of this document or the source code (in any
> form) of Minecraft: Java Edition is governed by the Minecraft End User License
> Agreement available at https://account.mojang.com/documents/minecraft_eula.

The packaged resource is a modified composition of namespaces and verified server
ABI aliases; the complete unmodified Mojang mapping input is not redistributed.
