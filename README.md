# MealPlanner

A native Android app for keeping recipes, planning meals and preparing a grocery list. Built in Android Studio with Kotlin, Jetpack Compose, Room, Hilt, DataStore and WorkManager.

The companion portfolio demo runs in a phone-sized viewport on desktop and fills the screen naturally on mobile. It is a browser adaptation, with synthetic sample data and local browser storage. It does not run the Android APK or sync with it.

## Try it

- [Interactive portfolio demo](https://mealplanner-portfolio-psi.vercel.app/)
- [Screen gallery](https://mealplanner-portfolio-psi.vercel.app/gallery.html)
- [Android debug APK](https://mealplanner-portfolio-psi.vercel.app/downloads/mealplanner-debug.apk)
- [Screen index and reproduction actions](docs/verification/SCREEN-INDEX.md)
- [Verification results and limitations](docs/verification/RESULTS.md)

## What works

- Create, edit, favorite, search and delete local recipes.
- Combine category, favorite and search filters.
- Scale ingredients by recipe yield and display nutrition totals by planned servings.
- Plan breakfast, lunch, dinner and snacks, move between weeks and switch day/week views.
- Fill empty weekly meal slots without duplicating existing meals.
- Generate a grocery checklist while retaining manually added shopping items.
- Export the grocery list, change appearance and save personal reference targets.

Sample recipe nutrition is illustrative and has not been independently verified. Targets are user preferences. Auto-fill uses recipe categories, not dietary, allergy or medical rules.

## Build the Android app

Requirements: JDK 17, Android SDK Platform 34 and an Android device or emulator running API 26 or newer. The project uses Gradle 8.13 and Android Gradle Plugin 8.7.0. Do not assume the newest Java bundled with Android Studio is compatible: Java 25 fails during Gradle script analysis in this project.

1. Open the repository root in Android Studio.
2. Set **Settings > Build, Execution, Deployment > Build Tools > Gradle > Gradle JDK** to an installed JDK 17.
3. Let Android Studio configure your local SDK path. `local.properties` is machine-specific and intentionally untracked.
4. Sync Gradle, select the `app` configuration and run on a test emulator.

Command-line checks, after setting `JAVA_HOME` to your JDK 17:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug --no-daemon --max-workers=2
```

Debug output: `app/build/outputs/apk/debug/app-debug.apk`. Package name: `com.mealplanner.debug`. This is a debug-signed test build, not a Play Store release.

## Run the portfolio demo

Node.js 22 or newer is sufficient. The app has no runtime packages, account, backend or external asset requests.

```powershell
cd demo
npm ci
npm start
```

Open `http://127.0.0.1:4173`. Set `PORT` to use another local port.

```powershell
npm test
npx playwright install chromium
npm run test:browser
```

Alternatively set `BROWSER_PATH` to an existing compatible Chrome or Edge executable. Set `DEMO_URL` to test an already-running or publicly deployed instance. Browser tests use fresh contexts, synthetic data and local downloads. They cover 390×844, 1440×1080 and 320×640 viewports.

## Native walkthrough

`app/src/androidTest/java/com/mealplanner/MealPlannerWalkthroughTest.kt` exercises the real Compose app and captures numbered emulator screenshots. Use a disposable emulator with synthetic app data. Never run the data-reset step on a personal device.

```powershell
.\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest
adb -s emulator-5554 install -r app/build/outputs/apk/debug/app-debug.apk
adb -s emulator-5554 install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb -s emulator-5554 shell pm clear com.mealplanner.debug
adb -s emulator-5554 shell am instrument -w -r com.mealplanner.debug.test/androidx.test.runner.AndroidJUnitRunner
adb -s emulator-5554 pull /sdcard/Android/data/com.mealplanner.debug/files/verification/. docs/verification/native/
```

The serial is an example. Select your emulator explicitly. Direct instrumentation leaves screenshot files available after the test; Gradle's connected-test cleanup may remove the app and its external files.

## Scope and limits

The native app stores recipes and plans on the device. There is no account, cloud sync, external recipe service or hardware integration. Android reminders use periodic WorkManager work, approximately every eight hours, with OS scheduling and notification-permission limits. The browser demo does not send notifications.

The demo keeps data in localStorage. Clearing site data, resetting the demo or using another browser removes or separates those changes. A storage-blocked browser keeps changes only during the current visit. Native plans store recipe nutrition snapshots; editing a recipe does not retroactively recalculate existing native meal-plan snapshots. The browser demo recalculates from the current recipe.

Android lint still reports dependency-age and compatibility warnings. The project has not been certified for production, medical use, all Android versions or physical-device behavior.
