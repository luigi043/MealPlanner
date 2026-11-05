# Verification results

Date: November 5, 2026. All app data used during verification was synthetic. Only the assigned MealPlanner repository and its emulator app package were changed.

## Android baseline and fixes

The checkout started clean at `3e592ae`. It was already a native Kotlin/Compose Android Studio project, not a web app. The existing Gradle wrapper uses Gradle 8.13 with AGP 8.7.0, Kotlin/Compose plugin 2.0.0, Java target 17 and Android SDK 34 (minimum 26).

The first command using the newest Android Studio Java failed with `Unsupported class file major version 69`. Selecting the already-installed JDK 17 resolved that environment failure. No competing SDK or emulator was installed.

Evidence-backed fixes:

- Search, category and favorite filters now compose rather than replacing one another.
- Form validation rejects negative and non-finite numbers, zero ingredient quantities and invalid serving counts. Save failures clear the busy state and show a retry message. Recipe edits retain original creation time and image metadata.
- Grocery generation scales whole-recipe ingredient quantities by the requested servings, atomically replaces generated items and keeps manual purchases.
- Auto-fill skips occupied date/meal slots, so repeated generation adds no duplicates. It does not implement dietary/allergy filtering or optimize personal nutrition goals.
- Recipe details now multiply per-serving nutrition by selected servings instead of dividing it by recipe yield. Ingredients retain their separate batch-yield scaling.
- Week view displays seven actual days, adjacent-week controls work, and status messages follow operation completion.
- Dashboard reference targets follow saved preferences. Refresh cancels its previous observer, and the weekly streak stops at today rather than the end of the future week.
- Reminders request notification permission when enabled. The helper skips notifications when OS permission is absent. Unused Internet and exact-alarm permissions were removed.
- Settings identify debug/release builds accurately and explain sample nutrition. Machine-specific `local.properties` is no longer tracked. Recipe timing labels are shorter and editing shows its loading state.

## Automated checks

| Check | Result | Evidence |
|---|---|---|
| Debug Android build, JDK 17 | Passed | `:app:assembleDebug` |
| Kotlin JVM regression tests | 5 passed | `PlanningRegressionTest.kt` |
| Android lint | 0 errors, 56 warnings | `:app:lintDebug`; warnings include older dependency/target versions and existing compatibility/icon issues |
| Native Android walkthrough | 1 passed, 87.176 seconds | `native-walkthrough.txt`; real API 35 emulator and Room-backed forms |
| Browser logic tests | 6 passed | `demo/tests/core.test.mjs` |
| Public browser tests | 5 passed | Recipe lifecycle, phone walkthrough, desktop walkthrough, small-phone layout, gallery/APK integrity |

The JVM tests exercise finite numeric validation, composed filters, grocery scaling and replacement semantics, auto-fill preservation/idempotence, and per-serving nutrition. They use synthetic DAO proxies rather than a remote database. The native walkthrough supplements them with actual Room-backed interaction.

The successful native walkthrough visited every navigation destination, opened an existing recipe and its editor, checked required-field errors, added an ingredient and saved a synthetic recipe, filled a meal plan, switched day/week views, opened meal selection, generated groceries, added a manual shopping item, and changed the theme in Settings. Seventeen native screenshots and thirty-two browser screenshots record these screens, dialogs and key empty/error states. Earlier native test attempts exposed selector ambiguity and asynchronous load timing in the test harness; those selectors were corrected before the successful run.

The public browser tests ran against `https://mealplanner-portfolio-psi.vercel.app`, using fresh Edge contexts. They created, edited and deleted a recipe; verified user text was escaped; added meals; filled a week twice; generated and exported groceries; kept a manual checked item after regeneration; searched by category; scaled servings; changed theme and targets; reloaded persisted state; and reset the demo. No JavaScript page errors were observed.

Final public verification ran against deployment source commit `0b4ecc4afca0a2ae5d3e5fdb1ed73d264b429698`: five tests passed in 44.6 seconds. Every one of the 49 gallery images returned HTTP 200 as PNG, the gallery fit a phone viewport, and the downloaded APK matched the committed manifest's byte length and SHA-256. The following documentation commit records these results and refreshes four captures; it does not change application logic.

Browser viewports: 390×844, 1440×1080 and 320×640. Tests assert the app remains at most 430px wide, dialogs stay within the phone bounds, main navigation remains reachable and app/page content does not overflow horizontally. The desktop app viewport is 414px wide.

## Screenshot provenance

`native/` contains real Android emulator captures. `demo/` contains real browser screenshots of the deployed web adaptation. These are explicitly labeled in the generated gallery and screen index. Browser images must not be presented as Android screenshots.

The source for the gallery/index is `demo/scripts/prepare-portfolio.mjs`. Run it after collecting evidence to refresh the static public gallery and `SCREEN-INDEX.md`.

## APK

`demo/downloads/mealplanner-debug.apk` is the installable debug build. Its application ID is `com.mealplanner.debug`. The version, byte length and SHA-256 digest are recorded in `demo/downloads/build-info.json`. It is not a signed store release.

APK SHA-256: `3077a5954dc2d44099e97e3bb42ae642c705a58cd89a07fa53bb131a28a72229` (18,900,265 bytes).

## External dependencies and remaining limits

- No login/account, backend, paid service, camera, BLE, RFID or other hardware integration is present in this app. Those integrations were not invented or claimed.
- The Android emulator is API 35 / Android 15. Physical devices, the connected Samsung phone, API 26 behavior and other Android versions were not tested.
- Long-running WorkManager reminders, reboot recovery and the OS notification permission-denial path have not been validated over an actual eight-hour interval. Permission handling and notification gating were inspected; the browser does not simulate successful Android delivery.
- Sample recipe nutrition is illustrative, not verified food-composition data. Targets do not constitute dietary or medical advice. Verify ingredients, portions, preparation and allergy suitability independently.
- Native meal plans retain recipe nutrition snapshots. Editing a recipe does not rewrite existing plan snapshots. The browser adaptation recalculates from current recipe data.
- Browser persistence is localStorage, separate from native Room/DataStore. Export downloads a text file; it is not Android's system share sheet.
- Existing dependency-age, target-version and icon/compatibility lint warnings remain. Gradle reports deprecated features that require review before a future Gradle 9 migration. No blanket dependency upgrade was attempted.
- The repository's previous history and required third-party attribution were preserved. New source, documentation, assets and branch names use neutral project branding.
