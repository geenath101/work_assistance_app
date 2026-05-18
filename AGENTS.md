# AGENTS.md

## Project State
Field worker attendance app — sign in / sign out enforced by GPS geofence.
All app source lives in `app/src/main/java/com/example/workassistance/`.

**Current state (as of last update):**
- Login screen implemented (username/password + Google Sign-In with mock logic)
- Main shell with top bar (user name + company logo) and bottom navigation (3 tabs)
- Home tab shows upcoming site visits loaded from API; tap navigates to sign-in screen
- Request Consumables tab — placeholder UI (feature pending)
- Profile tab — shows user details + sign out
- Theme upgraded to Material 3 (`Theme.Material3.DayNight.NoActionBar`)
- API field `proximity_radius_m` correctly mapped in `Site.kt`

## Toolchain (non-obvious versions)
- Gradle: **9.4.1** (via wrapper — always use `./gradlew`, not a system `gradle`)
- Android Gradle Plugin: **9.2.1**
- Kotlin: **2.2.10** (bundled by AGP 9.x — do NOT add `kotlin.android` plugin separately, it will conflict)
- KSP: **2.2.10-2.0.2** (must match Kotlin version exactly)
- Room: **2.8.4** (required for Kotlin 2.x / KSP2 compatibility — do not downgrade below 2.7)
- Java toolchain: **21** (resolved automatically via Foojay; no local JDK config needed)
- Java source/target compatibility: **11**
- `compileSdk` uses AGP 9.x syntax: `release(36) { minorApiLevel = 1 }` — do not change to plain integer form

## Build Commands
```bash
./gradlew assembleDebug       # build debug APK
./gradlew assembleRelease     # build release APK (minify disabled)
./gradlew build               # full build (compiles + tests)
./gradlew clean
```

## Tests
```bash
./gradlew :app:testDebugUnitTest          # JVM unit tests (no device needed)
./gradlew :app:connectedDebugAndroidTest  # instrumented tests (requires device/emulator)
```

## Dependencies
Managed via **version catalog** at `gradle/libs.versions.toml`. Add new deps there only — never inline in `build.gradle.kts`.

Key libraries in use:
- `core-ktx`, `appcompat`, `material` (Material 3 `1.10.0`)
- Coroutines `1.7.3`
- Lifecycle ViewModel + LiveData `2.6.2`
- Room `2.8.4` (KSP for codegen — `ksp(libs.androidx.room.compiler)`)
- Retrofit `2.9.0` + OkHttp `4.12.0` + Gson `2.10.1`
- Play Services Location `21.0.1`
- **Google Maps SDK `18.2.0`** (`play-services-maps`)
- **Fragment KTX `1.6.2`** (`androidx-fragment-ktx`)
- **Google Sign-In `20.7.0`** (`play-services-auth`) — wired but using mock until Web Client ID configured

## Architecture
```
data/
  local/          AppDatabase (Room), AttendanceDao, AttendanceRecord entity
  model/          User (in-memory auth user model)
  remote/api/     ApiService (Retrofit), RetrofitClient (singleton)
  remote/model/   Site, AttendanceRequest, AttendanceResponse
repository/       SiteRepository, AttendanceRepository, AuthRepository (mock/singleton)
ui/auth/          LoginActivity                    ← LAUNCHER activity
ui/main/          MainActivity (shell: top bar + bottom nav), MainViewModel,
                  MainViewModelFactory, SiteAdapter
ui/home/          HomeFragment                     ← default tab; shows upcoming sites
ui/consumables/   RequestConsumablesFragment        ← placeholder
ui/profile/       ProfileFragment                  ← shows user info + sign out
ui/site/          SiteDetailActivity, SiteViewModel, SiteViewModelFactory
util/             GeofenceHelper, Resource<T>
```

- Pattern: MVVM, manual DI (no Hilt), ViewBinding, LiveData
- No annotation processors other than KSP (no kapt)
- ProGuard/R8: **disabled** even in release builds
- Navigation: `LoginActivity` → `MainActivity` (shell) → fragments via `FragmentContainerView`
- `MainActivity` is NOT the launcher — `LoginActivity` is

## Authentication (mock)
- `AuthRepository` is a Kotlin `object` (in-memory singleton)
- `signInWithPassword(username, password)` — accepts any non-empty credentials; simulates 1 s delay
- `signInWithGoogle(...)` — mocked; falls back gracefully if Google Sign-In SDK not configured
- **TODO**: replace mock bodies with real backend API / Firebase Auth calls
- **TODO**: persist session token with DataStore or EncryptedSharedPreferences
- **TODO**: add real Web Client ID to `LoginActivity.launchGoogleSignIn()` for production Google Sign-In

## Google Maps integration
- Dependency: `play-services-maps:18.2.0`
- API key injected via `manifestPlaceholders["MAPS_API_KEY"]` in `build.gradle.kts`
- Key is read from `local.properties` (`MAPS_API_KEY=...`) — **never hardcode or commit the key**
- Manifest uses `${MAPS_API_KEY}` placeholder on the `com.google.android.geo.API_KEY` meta-data tag
- To get a key: Google Cloud Console → APIs & Services → Credentials → enable "Maps SDK for Android"
- `SiteDetailActivity` implements `OnMapReadyCallback`; map shows site pin (blue), geofence circle (translucent blue), and live user marker (green = inside / red = outside)
- Map is full-screen with a bottom sheet overlay for sign-in/out controls

## API integration
- Base URL constant: `RetrofitClient.BASE_URL` — update this when the real backend URL is known
- `GET sites` → `List<Site>` (fields: `id`, `name`, `address`, `latitude`, `longitude`, `proximity_radius_m`)
  - Note: field is `proximity_radius_m` in API, mapped to `radiusMeters` via `@SerializedName`
- `POST attendance` body: `site_id`, `site_name`, `event_type` (`SIGN_IN`/`SIGN_OUT`), `latitude`, `longitude`, `timestamp` (epoch ms)
- Attendance events are saved to Room immediately; server sync is attempted after. Records with `synced=false` are retried via `AttendanceRepository.syncPending()`.

## Key Config
- `applicationId`: `com.example.workassistance`
- `minSdk`: 24 / `targetSdk`: 36
- Permissions: `INTERNET`, `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`

## Repo Rules
- Single module: `:app`
- Per-module repository declarations are blocked (`FAIL_ON_PROJECT_REPOS`) — add repos only in `settings.gradle.kts`
- `gradle.properties` sets `-Xmx2048m`; parallel builds are commented out
- `android.disallowKotlinSourceSets=false` is required in `gradle.properties` to let KSP register its sources without conflicting with AGP 9.x's built-in Kotlin
- `local.properties` is not committed — safe to store secrets there

## Pending / TODOs
- [ ] Replace `AuthRepository` mock with real backend auth (Firebase Auth or custom API)
- [ ] Persist auth session across app restarts (DataStore / EncryptedSharedPreferences)
- [ ] Add real Google Sign-In Web Client ID
- [ ] Implement `RequestConsumablesFragment` — form to submit tool/material requests
- [ ] Wire `AttendanceRepository.syncPending()` to WorkManager for background retry
- [ ] Add company logo image resource (currently uses app launcher icon as placeholder)
- [ ] Add user avatar support in `ProfileFragment`


## Toolchain (non-obvious versions)
- Gradle: **9.4.1** (via wrapper — always use `./gradlew`, not a system `gradle`)
- Android Gradle Plugin: **9.2.1**
- Kotlin: **2.2.10** (bundled by AGP 9.x — do NOT add `kotlin.android` plugin separately, it will conflict)
- KSP: **2.2.10-2.0.2** (must match Kotlin version exactly)
- Room: **2.8.4** (required for Kotlin 2.x / KSP2 compatibility — do not downgrade below 2.7)
- Java toolchain: **21** (resolved automatically via Foojay; no local JDK config needed)
- Java source/target compatibility: **11**
- `compileSdk` uses AGP 9.x syntax: `release(36) { minorApiLevel = 1 }` — do not change to plain integer form

## Build Commands
```bash
./gradlew assembleDebug       # build debug APK
./gradlew assembleRelease     # build release APK (minify disabled)
./gradlew build               # full build (compiles + tests)
./gradlew clean
```

## Tests
```bash
./gradlew :app:testDebugUnitTest          # JVM unit tests (no device needed)
./gradlew :app:connectedDebugAndroidTest  # instrumented tests (requires device/emulator)
```

## Dependencies
Managed via **version catalog** at `gradle/libs.versions.toml`. Add new deps there only — never inline in `build.gradle.kts`.

Key libraries in use:
- `core-ktx`, `appcompat`, `material`
- Coroutines `1.7.3`
- Lifecycle ViewModel + LiveData `2.6.2`
- Room `2.6.0` (KSP for codegen — `ksp(libs.androidx.room.compiler)`)
- Retrofit `2.9.0` + OkHttp `4.12.0` + Gson `2.10.1`
- Play Services Location `21.0.1`
- **Google Maps SDK `18.2.0`** (`play-services-maps`)

## Architecture
```
data/
  local/          AppDatabase (Room), AttendanceDao, AttendanceRecord entity
  remote/api/     ApiService (Retrofit), RetrofitClient (singleton)
  remote/model/   Site, AttendanceRequest, AttendanceResponse
repository/       SiteRepository, AttendanceRepository
ui/main/          MainActivity, MainViewModel, MainViewModelFactory, SiteAdapter
ui/site/          SiteDetailActivity, SiteViewModel, SiteViewModelFactory
util/             GeofenceHelper, Resource<T>
```

- Pattern: MVVM, manual DI (no Hilt), ViewBinding, LiveData
- No annotation processors other than KSP (no kapt)
- ProGuard/R8: **disabled** even in release builds

## Google Maps integration
- Dependency: `play-services-maps:18.2.0`
- API key injected via `manifestPlaceholders["MAPS_API_KEY"]` in `build.gradle.kts`
- Key is read from `local.properties` (`MAPS_API_KEY=...`) — **never hardcode or commit the key**
- Manifest uses `${MAPS_API_KEY}` placeholder on the `com.google.android.geo.API_KEY` meta-data tag
- To get a key: Google Cloud Console → APIs & Services → Credentials → enable "Maps SDK for Android"
- `SiteDetailActivity` implements `OnMapReadyCallback`; map shows site pin (blue), geofence circle (translucent blue), and live user marker (green = inside / red = outside)

## API integration
- Base URL constant: `RetrofitClient.BASE_URL` — update this when the real backend URL is known
- `GET sites` → `List<Site>` (fields: `id`, `name`, `address`, `latitude`, `longitude`, `radius_meters`)
- `POST attendance` body: `site_id`, `site_name`, `event_type` (`SIGN_IN`/`SIGN_OUT`), `latitude`, `longitude`, `timestamp` (epoch ms)
- Attendance events are saved to Room immediately; server sync is attempted after. Records with `synced=false` are retried via `AttendanceRepository.syncPending()`.

## Key Config
- `applicationId`: `com.example.workassistance`
- `minSdk`: 24 / `targetSdk`: 36
- Permissions: `INTERNET`, `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`

## Repo Rules
- Single module: `:app`
- Per-module repository declarations are blocked (`FAIL_ON_PROJECT_REPOS`) — add repos only in `settings.gradle.kts`
- `gradle.properties` sets `-Xmx2048m`; parallel builds are commented out
- `android.disallowKotlinSourceSets=false` is required in `gradle.properties` to let KSP register its sources without conflicting with AGP 9.x's built-in Kotlin
- `local.properties` is not committed — safe to store secrets there
