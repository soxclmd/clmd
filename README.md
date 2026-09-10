# SOXCLMD — Official Android Application

**SOCCSKSARGEN Curriculum and Learning Management Division**
Department of Education · Regional Office XII

*Connecting Curriculum, Learning, and Innovation.*

A native Android app (Kotlin · Jetpack Compose · Material 3) that shares the
same GitHub-powered content source as the [SOXCLMD website](https://soxclmd.github.io/clmd/) —
**one update, maintained in one place, appears everywhere.**

---

## Architecture — GitHub as the single source of truth

```
                 SOXCLMD GITHUB REPOSITORY  (single source of truth)
                            │
                     GitHub Actions
             ┌──────────────┴───────────────┐
             ▼                              ▼
     SOXCLMD Website              Shared content layer
   (GitHub Pages site)         (static JSON under /assets/data)
                                            │
                                            ▼
                                   SOXCLMD Android app
                             (manifest + hash-based sync)
                                            │
                                            ▼
                                  Teachers / mobile users
```

The app is **not a WebView**. It fetches the same JSON feeds the site uses
and renders them in a native Material 3 UI with offline caching.

### Content feeds consumed (all public, HTTPS)

| Feed | URL path | Powers |
|---|---|---|
| `manifest` | `assets/data/manifest.json` | Change detection, app-version gate |
| `news` | `assets/data/news.json` | News module |
| `events` | `assets/data/events.json` | Activities + Calendar |
| `memoranda` | `assets/data/advisories.json` | Memoranda repository |
| `learningAreas` | `assets/data/learning-areas.json` | Learning Areas + MAPEH Hub |
| `resources` | `assets/data/downloads.json` | Resource Center |
| `site` | `assets/data/site.json` | About, Contact, Announcements |
| `searchIndex` | `assets/data/search-index.json` | Global Search |

### How synchronization works

1. App opens / returns from background / reconnects / every 6 h (WorkManager) / manual refresh.
2. Downloads the small **manifest** (`content_version` + SHA-256 per feed).
3. If the version changed, downloads **only the feeds whose hash changed**.
4. Cache is updated atomically; UI always renders from local cache (offline-first).
5. New-vs-seen diff powers the in-app "What's new" feed.
6. `minimum_app_version` / `maintenance_mode` in the manifest gate the app.

**Content updates never require a new APK.** A new APK/AAB is only needed for
app code changes (built by GitHub Actions — see below).

---

## Project layout

```
soxclmd-app/
├── .github/workflows/
│   ├── android-build.yml      # CI: APK + AAB artifacts, tag → GitHub Release
│   └── content-manifest.yml   # CI: validate JSON, refresh manifest.json
├── app/src/main/java/ph/gov/deped/region12/soxclmd/
│   ├── AppApplication.kt      # DI root + WorkManager scheduling
│   ├── MainActivity.kt        # single activity, sync on resume
│   ├── AppNav.kt              # bottom navigation + NavHost
│   ├── data/                  # models, HTTP client, cache, repository, sync worker
│   ├── ui/theme/              # official purple/gold Material 3 theme
│   ├── ui/components/         # shared composables (banners, chips, empty states)
│   ├── ui/screens/            # Home, News, Activities, Calendar, Resources,
│   │                          # Learning Areas, MAPEH, Search, About, Settings
│   └── util/                  # DownloadManager helper, date utilities
└── tools/build-content.py     # manifest generator + content validator
```

## Building

### Locally (Android Studio)
1. Open the `soxclmd-app` folder in Android Studio (Hedgehog or newer).
2. Run ▶ on the `app` configuration. Done.

### Command line
```bash
./gradlew :app:assembleDebug   # debug APK
./gradlew :app:bundleRelease   # Play Store AAB (add signing config first)
```

### CI (recommended)
Push to `main` — [android-build.yml](.github/workflows/android-build.yml)
builds an **APK + AAB** and uploads them as workflow artifacts
(repo → Actions → latest run → Artifacts). Tag a release (`v1.0.0`) to
publish a GitHub Release with the APK for direct download.

> The first CI run is the project's compile gate. If it reports an error,
> paste the log and it can be fixed in one commit.

## Maintaining content (no Android knowledge required)

| To publish… | Do this |
|---|---|
| New memorandum | Add the PDF (e.g. `assets/pdf/clmd2026/CLMD-2026-XXX.pdf` or a Drive link) and append a record to `assets/data/advisories.json` |
| News story | Add images to `assets/images/news/`, append an entry to `assets/data/news.json` |
| Event / schedule | Append an entry to `assets/data/events.json` |
| Resource | Append an entry to `assets/data/downloads.json` |
| Announcement / leaders / contact | Edit `assets/data/site.json` |
| Learning area profile | Edit `assets/data/learning-areas.json` |

Then commit. The **Content Manifest** workflow validates the JSON, refreshes
`assets/data/manifest.json`, and commits it — the website and every installed
app pick up the change automatically (no APK, no Play Store).

Field reference for each JSON lives in `app/src/main/java/.../data/model/Models.kt`.

## Google Play preparation (when ready)

- Package: `ph.gov.deped.region12.soxclmd` (already set)
- Build: `./gradlew :app:bundleRelease` with a signing config (see below)
- Play App Signing: upload the AAB; Google manages the app signing key
- Before first upload: privacy policy URL, store listing, 4 screenshots,
  and (for an organization account) DepEd RO XII's D-U-N-S number.

### Signing config (add when wiring Play)
```kotlin
// app/build.gradle.kts -> android { signingConfigs { ... } }
signingConfigs {
    create("release") {
        storeFile = file(System.getenv("SOXCLMD_KEYSTORE") ?: "release.jks")
        storePassword = System.getenv("SOXCLMD_KEYSTORE_PASSWORD")
        keyAlias = System.getenv("SOXCLMD_KEY_ALIAS")
        keyPassword = System.getenv("SOXCLMD_KEY_PASSWORD")
    }
}
```
Never commit the keystore or passwords. In CI, use GitHub repository secrets.

## Push notifications (phase 2)

The in-app "What's new" feed is live. For real push notifications:
1. Create a Firebase project, add the Android app with this package id.
2. Drop `google-services.json` into `app/` and apply the `com.google.gms.google-services` plugin.
3. Register an `FirebaseMessagingService` and post a notification on "new content" messages.
4. Content-side: a small GitHub Action can call FCM when `assets/data/manifest.json` changes.

## Security & privacy

- HTTPS only; no personal data collected; no authentication.
- Permissions: `INTERNET` and `ACCESS_NETWORK_STATE` only.
- Downloads go through the system DownloadManager (no storage permission).
- No hard-coded secrets anywhere in the repository.

## Versioning

- App: semantic `MAJOR.MINOR.PATCH` in `app/build.gradle.kts` (`versionCode` + `versionName`).
- Content: date-stamped by `tools/build-content.py` (e.g. `2026.09.10.1420`).
- `minimum_app_version` in the manifest forces an app update when needed.
