# MealPlanner portfolio demo

This is a static, interactive adaptation of the Kotlin Android app. The app viewport stays phone-sized on desktop (414px normally, 430px on wide displays); on mobile it fills the available device viewport. Navigation stays visible while the app content scrolls. Dialogs are positioned inside the phone's bounds.

Run `npm ci` and `npm start`. Open the local URL printed by the server. `PORT` can override the default port 4173.

- `npm test`: date handling, composed filters, numeric validation, meal-plan idempotence, grocery scaling and nutrition calculations.
- `npm run test:browser`: recipe lifecycle, validation and safe text rendering; complete planning, grocery and settings walkthroughs at phone and desktop sizes; a small-phone layout check.
- `node scripts/prepare-portfolio.mjs`: copy genuine screenshot evidence from `../docs/verification` and regenerate the public gallery and repository screen index.

Set `BROWSER_PATH` for an installed Chrome/Edge browser, or install Playwright Chromium. Set `DEMO_URL` to exercise a running local server or the public deployment. All browser tests use fresh contexts and synthetic data.

There is no backend or runtime package dependency. localStorage keeps recipes, plans and settings in this browser. The demo uses illustrative nutrition values from the native sample recipes; it is not a nutrition service. Android reminders, Room persistence and Android sharing are represented only in the native app. Browser grocery export downloads a text file.

Deployment uses the repository's `demo` root as static output, with no install or build command. `gallery.html` and `screenshots` are generated artifacts committed with their source evidence. The APK is a debug-signed test build, not a production release. See the root README and verification results for its checksum and limitations.
