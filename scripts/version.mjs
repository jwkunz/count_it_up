#!/usr/bin/env node
// The project version lives in one file, VERSION, at the repository root.
// The Android build reads it directly (versionName, and versionCode = major*10000 + minor*100 + patch)
// and the web packager uses it in the output file name.
//
//   node scripts/version.mjs                 print the version
//   node scripts/version.mjs set 1.2.0       change the version
//   node scripts/version.mjs check [tag]     fail if anything disagrees (and if `tag`, e.g. v1.2.0, is not this version)
import { readFileSync, writeFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { join } from 'node:path';

const root = fileURLToPath(new URL('..', import.meta.url));
const read = (p) => readFileSync(join(root, p), 'utf8');
const SEMVER = /^\d+\.\d+\.\d+$/;
const currentVersion = () => read('VERSION').trim();

const [cmd, arg] = process.argv.slice(2);
if (!cmd) {
  console.log(currentVersion());
} else if (cmd === 'set') {
  if (!SEMVER.test(arg || '')) { console.error('Usage: node scripts/version.mjs set MAJOR.MINOR.PATCH'); process.exit(2); }
  writeFileSync(join(root, 'VERSION'), `${arg}\n`);
  console.log(`Version is now ${arg}.`);
} else if (cmd === 'check') {
  const v = currentVersion();
  const problems = SEMVER.test(v) ? [] : [`VERSION is "${v}", expected MAJOR.MINOR.PATCH`];
  if (!/rootProject\.file\("\.\.\/VERSION"\)/.test(read('Android/app/build.gradle.kts'))) problems.push('Android/app/build.gradle.kts does not read VERSION');
  if (arg && arg !== `v${v}`) problems.push(`the tag ${arg} does not match VERSION ${v} (expected v${v})`);
  if (problems.length) { console.error(`Version problems:\n  - ${problems.join('\n  - ')}`); process.exit(1); }
  console.log(`All version files agree: ${v}${arg ? ` (tag ${arg})` : ''}`);
} else {
  console.error('Usage: node scripts/version.mjs [set X.Y.Z | check [tag]]'); process.exit(2);
}
