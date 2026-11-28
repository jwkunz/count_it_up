# Count It Up!

![Count It Up! logo](web/Count_It_Up.png)

Count It Up! is a simple dark-themed app for counting repeated events and tracking them over time. It runs entirely on your device, works offline, and is available as a web app and as an Android app that wraps it.

## Get it

| Where | What |
|-------|------|
| **[jwkunz.github.io/count_it_up](https://jwkunz.github.io/count_it_up/)** | The web app, always the latest `main` (deployed by GitHub Pages) |
| **[Releases](https://github.com/jwkunz/count_it_up/releases)** | `count-it-up-vX.Y.Z.html`, a single self-contained file you can save and open locally, and `count-it-up-vX.Y.Z.apk`, the Android app |

## Features

- Increment/decrement live counter
- Commit counter value with timestamp
- Reset counter without clearing history
- Edit or delete log entries
- Plot logged values over time with cubic B-spline fit
- Export log as CSV (`timestamp,value`)
- Import log from CSV
- Persist data locally using IndexedDB (with localStorage fallback; legacy cookie data is migrated on first load)

## Usage

1. Open the web app (or the downloaded HTML file, or the Android app).
2. Use **Increment** / **Decrement** to set the current value.
3. Click **Commit** to store the value and timestamp.
4. Select log rows to **Edit Entry** or **Delete**.
5. Use **Export CSV** / **Import CSV** for data portability. On Android these use the system file picker.

## Repository layout

```
VERSION                  the one place the project version lives
web/                     the web app
  Count_It_Up.html         the UI source (edit this)
  Count_It_Up.png          logo
  scripts/                 single_file_package.sh
Android/                 Gradle project: a WebView app that bundles the web build
scripts/version.mjs      prints, sets and checks the version
.github/workflows/       ci.yml (builds and releases) and pages.yml (site deploy)
```

The web app is edited in exactly one place, `web/`. Both the GitHub Pages site and the Android app are built from it, so a push to `main` updates the site, and the next APK build picks up the same code.

## How it fits together

1. `web/scripts/single_file_package.sh` inlines the logo into the HTML, producing `web/count_it_up_vX_Y_Z.html` (git-ignored).
2. That file is:
   - copied to the site by `pages.yml`,
   - bundled by Gradle into the Android app as `assets/index.html`, which `MainActivity` loads in a WebView (with a small bridge for CSV export),
   - attached to each GitHub release.

## Building locally

**Web.** Needs only bash, perl and coreutils.

```bash
cd web
./scripts/single_file_package.sh    # builds web/count_it_up_vX_Y_Z.html; open it in a browser
```

**Android.** Needs JDK 21 and the Android SDK.

```bash
cd Android
./gradlew :app:assembleDebug        # builds the web page first, then the APK
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Pass `-PskipWebBuild` to reuse an existing `web/count_it_up_v*.html`. CI signs the release APK with the key in the repository's `RELEASE_*` secrets. A local `assembleRelease` without them produces an unsigned APK.

## Continuous integration

- **`ci.yml`** runs on pushes to `main`, tags and pull requests: checks the version, builds the single HTML file (and verifies it has no external references), and builds the release APK.
- **On a `v*` tag** it also publishes a GitHub release containing the HTML file and the APK.
- **`pages.yml`** builds the single HTML file and deploys it to GitHub Pages on every push to `main`.

## Versioning and releasing

```bash
node scripts/version.mjs                # print the version
node scripts/version.mjs set 1.2.0      # update VERSION
node scripts/version.mjs check          # fail if anything disagrees
```

Then commit and tag:

```bash
git tag v1.2.0 && git push origin v1.2.0
```

CI refuses to release if the tag doesn't match `VERSION`. The Android `versionName` and `versionCode` (`major*10000 + minor*100 + patch`) are derived from `VERSION`.

## License

MIT. See [LICENSE](LICENSE).
