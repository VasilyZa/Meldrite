import path from 'node:path';
import { spawnSync } from 'node:child_process';

const root = path.resolve(import.meta.dirname, '..');
const artifactRoot = path.join(root, '.gradle/meldrite/artifacts/com.fasterxml.jackson.core');
const classpath = ['jackson-core', 'jackson-annotations', 'jackson-databind']
  .map(name => path.join(artifactRoot, name, '2.18.2', `${name}-2.18.2.jar`)).join(path.delimiter);
const java = process.env.JAVA_HOME ? path.join(process.env.JAVA_HOME, 'bin/java') : 'java';
const report = process.argv[3] ?? path.join(root, 'build/reports/meldrite/craft-source-audit.json');
const leaf = process.argv[2] ?? '/home/xiaobai/Projects/Projects/Leaf/Leaf-Server/src/main/java/org/bukkit/craftbukkit';
const result = spawnSync(java, ['-cp', classpath, path.join(root, 'tools/CraftSourceAudit.java'), report, leaf,
  path.join(root, 'versions/v1_20_1/bootstrap/src/main/java/org/bukkit/craftbukkit'),
  path.join(root, 'adapters/paper-1.20.1/src/main/java/org/bukkit/craftbukkit')], { stdio: 'inherit' });
process.exit(result.status ?? 1);
