import fs from 'node:fs';
import path from 'node:path';
import { spawnSync, execFileSync } from 'node:child_process';

const root = path.resolve(import.meta.dirname, '..');
process.chdir(root);
const manifest = JSON.parse(fs.readFileSync('distributions/mc-1.20.1/build/distribution.json', 'utf8'));
const native = path.join(process.env.HOME, '.gradle/caches/fabric-loom/minecraftMaven/net/minecraft/minecraft-merged/1.20.1-net.fabricmc.yarn.1_20_1.1.20.1+build.10-v2/minecraft-merged-1.20.1-net.fabricmc.yarn.1_20_1.1.20.1+build.10-v2.jar');
const cached = execFileSync('rg', ['--files', path.join(process.env.HOME, '.gradle/caches/modules-2/files-2.1/net.fabricmc/sponge-mixin')], { encoding: 'utf8' }).trim().split('\n').filter(file => file.endsWith('.jar') && !file.includes('-sources'));
const modules = execFileSync('rg', ['--files', '-g', 'build.gradle.kts', 'modules', 'adapters', 'versions'], { encoding: 'utf8' }).trim().split('\n').map(file => file.replace('/build.gradle.kts', ''));
const classpath = [native, ...modules.flatMap(module => [module + '/build/classes/java/main', module + '/build/resources/main']), ...cached, ...manifest.runtime.classpath.map(artifact => artifact.path)].join(path.delimiter);
const output = 'build/entity-subtype-probe'; fs.mkdirSync(output, { recursive: true });
const sources = execFileSync('rg', ['--files', 'versions/v1_20_1/bootstrap/src/main/java/org/bukkit/craftbukkit/v1_20_R1/entity', 'versions/v1_20_1/bootstrap/src/main/java/org/bukkit/craftbukkit/v1_20_R1/attribute', 'versions/v1_20_1/bootstrap/src/main/java/dev/meldrite/v1201/hook', 'versions/v1_20_1/bootstrap/src/main/java/dev/meldrite/v1201/compat'], { encoding: 'utf8' }).trim().split('\n').filter(file => /(CraftEntity|CraftLivingEntity|CraftHumanEntity|CraftPlayer|CraftArmorStand|CraftAttributeInstance|ArmorStandNativeAccess|NativeCraftFacades|ArmorStandExtensionsMixin|LivingEntityStateAccessor|MobEntityStateAccessor|EntityFlagAccessor)\.java$/.test(file));
sources.push('versions/v1_20_1/bootstrap/src/main/java/org/bukkit/craftbukkit/v1_20_R1/inventory/CraftEntityEquipment.java', 'versions/v1_20_1/bootstrap/src/test/java/dev/meldrite/v1201/compat/NativeEntitySubtypeProbe.java');
sources.push('versions/v1_20_1/bootstrap/src/test/java/dev/meldrite/v1201/compat/NativeGSitEntityProbe.java');
sources.push('versions/v1_20_1/bootstrap/src/main/java/dev/meldrite/v1201/hook/PlayerDamageTiltAccessor.java');
sources.push('versions/v1_20_1/bootstrap/src/main/java/dev/meldrite/v1201/compat/NativeEntityExtensions.java', 'versions/v1_20_1/bridge/src/main/java/dev/meldrite/v1201/bridge/NativeEntityPort.java', 'adapters/paper-1.20.1/src/main/java/dev/meldrite/paper/facade/FacadeContext.java', 'versions/v1_20_1/bootstrap/src/test/java/dev/meldrite/v1201/compat/NativeDetachedEntityProbe.java');
sources.push(...['entity/CraftMob', 'entity/CraftCreature', 'entity/CraftGolem', 'entity/CraftShulker', 'CraftLootTable'].map(file => 'versions/v1_20_1/bootstrap/src/main/java/org/bukkit/craftbukkit/v1_20_R1/' + file + '.java'));
sources.push(...['compat/MobNativeAccess', 'compat/NativeMobExtensions', 'compat/NativePathfinder', 'compat/NativeLootTables', 'hook/MobExtensionsMixin', 'hook/ShulkerStateAccessor'].map(file => 'versions/v1_20_1/bootstrap/src/main/java/dev/meldrite/v1201/' + file + '.java'));
sources.push('versions/v1_20_1/bootstrap/src/test/java/dev/meldrite/v1201/compat/NativeMobSubtypeProbe.java');
sources.push('versions/v1_20_1/bootstrap/src/test/java/dev/meldrite/v1201/compat/NativeLivingQueryProbe.java');
sources.push('versions/v1_20_1/bootstrap/src/test/java/dev/meldrite/v1201/compat/NativePlayerInventoryContentsProbe.java');
sources.push('versions/v1_20_1/bootstrap/src/main/java/dev/meldrite/v1201/hook/PlayerInventoryContentsMixin.java');
for (const [command, args] of [['java', ['-cp', classpath, 'tools/GenerateEntityApi.java', '.']], ['java', ['tools/GenerateCraftPlayer.java', '.']], ['javac', ['-proc:none', '--release', '17', '-cp', classpath, '-sourcepath', modules.map(module => module + '/src/main/java').join(path.delimiter), '-d', output, ...sources]]]) {
  const result = spawnSync(command, args, { stdio: 'inherit' }); if (result.status !== 0) process.exit(result.status ?? 1);
}
console.log('Entity subtype sources compiled; NativeEntitySubtypeProbe.verify(ServerWorld) requires a READY server with the installed mixins.');
