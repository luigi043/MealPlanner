import { mkdir, readdir, copyFile, writeFile } from "node:fs/promises";
import { resolve } from "node:path";
const root = resolve(import.meta.dirname, "../..");
const demo = resolve(root, "demo");
const titles = {
  "01-dashboard": "Dashboard",
  "02-recipes": "Recipe library",
  "03-recipe-detail": "Recipe details and serving controls",
  "04-edit-recipe": "Edit an existing recipe",
  "05-new-recipe": "New recipe form",
  "06-recipe-validation": "Required-field validation",
  "07-add-ingredient": "Add ingredient dialog",
  "07b-recipe-saved": "New recipe saved to Room",
  "08-meal-plan-day": "Day plan",
  "09-meal-plan-filled": "Auto-fill keeps existing meals",
  "10-meal-plan-week": "Weekly overview",
  "11-add-meal": "Choose a meal and servings",
  "12-groceries": "Generated grocery checklist",
  "13-add-grocery": "Manual grocery item dialog",
  "14-settings-goals": "Planning preferences",
  "15-settings-preferences": "Theme and reminder preferences",
  "16-settings-about": "Build and sample-data information",
  "01-home": "Today dashboard",
  "02-plan": "Day plan",
  "03-add-meal": "Meal selection dialog",
  "04-week": "Weekly overview",
  "05-groceries": "Generated groceries",
  "06-add-grocery": "Manual grocery item",
  "07-recipes": "Recipe library",
  "08-empty-search": "No matching recipes",
  "09-recipe-detail": "Recipe and scaled servings",
  "10-edit-recipe": "Edit recipe dialog",
  "11-new-recipe": "Create recipe dialog",
  "12-settings": "Settings",
  "13-dark-settings": "Dark mode",
  "14-reset-dialog": "Reset confirmation",
  "15-empty-grocery": "Empty grocery list",
  "16-invalid-ingredient": "Ingredient format validation",
  "17-delete-dialog": "Delete recipe confirmation",
  "small-phone-recipe-form": "Recipe form on a 320px phone",
};
const actions = {
  "01-dashboard": "Launch app",
  "02-recipes": "Home > Recipes",
  "03-recipe-detail": "Recipes > search Grilled > open Grilled Chicken Salad",
  "04-edit-recipe": "Recipe detail > Edit",
  "05-new-recipe": "Recipes > Add Recipe",
  "06-recipe-validation": "New Recipe > Save with required fields empty",
  "07-add-ingredient": "New Recipe > Ingredients > Add",
  "07b-recipe-saved": "Complete synthetic recipe and Save",
  "08-meal-plan-day": "Bottom navigation > Plan",
  "09-meal-plan-filled": "Plan > Auto-generate",
  "10-meal-plan-week": "Plan > Toggle view",
  "11-add-meal": "Day plan > Add meal",
  "12-groceries": "Plan > Generate, then Grocery",
  "13-add-grocery": "Grocery > Add item",
  "14-settings-goals": "Bottom navigation > Settings",
  "15-settings-preferences": "Settings > scroll to Preferences",
  "16-settings-about": "Settings > scroll to About",
};
const entries = [];
for (const type of ["native", "demo"]) {
  const folder = resolve(root, "docs/verification", type);
  const files = (await readdir(folder))
    .filter((name) => name.endsWith(".png"))
    .sort();
  await mkdir(resolve(demo, "screenshots", type), { recursive: true });
  for (const file of files) {
    await copyFile(
      resolve(folder, file),
      resolve(demo, "screenshots", type, file),
    );
    const key = file.replace(/^(phone|desktop)-/, "").replace(".png", "");
    const caption = titles[key] || key.replaceAll("-", " ");
    entries.push({
      type,
      file,
      caption,
      action:
        type === "native"
          ? actions[key] || caption
          : `Browser walkthrough: ${caption}`,
      viewport:
        type === "native"
          ? "Android 15 emulator, 1080×2340"
          : file.startsWith("desktop")
            ? "1440×1080 outer viewport, 414px app"
            : file.startsWith("small")
              ? "320×640 phone"
              : "390×844 phone",
    });
  }
}
const escape = (text) =>
  text.replace(
    /[&<>"']/g,
    (char) =>
      ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" })[
        char
      ],
  );
const sections = ["native", "demo"]
  .map(
    (type) =>
      `<section><h2>${type === "native" ? "Native Android app" : "Interactive browser demo"}</h2><p>${type === "native" ? "Real screenshots captured from the Kotlin app on an Android 15 emulator. Synthetic app data only." : "Real browser captures, including phone and desktop outer viewports. These are browser UI, not Android screenshots."}</p><div class="grid">${entries
        .filter((e) => e.type === type)
        .map(
          (e) =>
            `<figure><a href="./screenshots/${type}/${e.file}" target="_blank" rel="noreferrer"><img loading="lazy" src="./screenshots/${type}/${e.file}" alt="${escape(e.caption)}"></a><figcaption><strong>${escape(e.caption)}</strong><span>${escape(e.viewport)}</span><small>${e.file}</small></figcaption></figure>`,
        )
        .join("")}</div></section>`,
  )
  .join("");
await writeFile(
  resolve(demo, "gallery.html"),
  `<!doctype html><html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>MealPlanner | Screen gallery</title><link rel="icon" href="./leaf.svg"><style>*{box-sizing:border-box}body{margin:0;padding:35px clamp(20px,5vw,80px);background:#f2f3eb;color:#233b2c;font:15px/1.65 'Segoe UI',sans-serif}header{max-width:780px;margin-bottom:45px}h1,h2{font-family:Georgia,serif;font-weight:400}h1{font-size:42px;line-height:1.1}h2{font-size:30px}a{color:#245840;text-underline-offset:5px}header nav{display:flex;gap:25px;flex-wrap:wrap}section{margin-bottom:60px}.grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(210px,1fr));gap:22px;margin-top:25px}figure{margin:0;background:#fffef9;border:1px solid #dfe5d7;border-radius:15px;overflow:hidden}figure>a{display:block;background:#e4e8db;height:330px;padding:13px}img{width:100%;height:100%;object-fit:contain}figcaption{padding:15px;display:flex;flex-direction:column;gap:5px}strong{font-size:13px}span{font-size:11px;color:#64735a}small{font-size:10px;overflow-wrap:anywhere;color:#64735a}p{max-width:780px}@media(max-width:500px){.grid{grid-template-columns:1fr 1fr;gap:12px}figure>a{height:270px;padding:7px}figcaption{padding:10px}h1{font-size:34px}}</style></head><body><header><nav><a href="./">← Try the demo</a><a href="./downloads/mealplanner-debug.apk" download>Download debug APK</a></nav><h1>MealPlanner, screen by screen.</h1><p>Native Android verification and a phone-sized browser demo. Open any image at full resolution. Screenshots were captured on November 5, 2026, using synthetic sample data.</p></header>${sections}</body></html>`,
);
const index =
  `# Screen index\n\nCaptured November 5, 2026. Native and browser evidence are explicitly separated. Images are direct captures, not mockups.\n\nAll seven native navigation destinations are represented: dashboard, meal plan, recipes, recipe detail, add recipe, edit recipe, grocery and settings (add/edit share one route). Additional states include validation, ingredient/meal/grocery dialogs, week view and preferences.\n\n` +
  ["native", "demo"]
    .map(
      (type) =>
        `## ${type === "native" ? "Native Android, API 35 emulator" : "Browser demo"}\n\n| Screenshot | Screen/state | Reproduction action | Viewport |\n|---|---|---|---|\n` +
        entries
          .filter((e) => e.type === type)
          .map(
            (e) =>
              `| [${e.file}](${type}/${e.file}) | ${e.caption} | ${e.action} | ${e.viewport} |`,
          )
          .join("\n"),
    )
    .join("\n\n") +
  "\n\nSee RESULTS.md for test coverage, hardware/account scope and remaining limitations.\n";
await writeFile(resolve(root, "docs/verification/SCREEN-INDEX.md"), index);
await writeFile(
  resolve(root, "docs/verification/screens.json"),
  JSON.stringify(entries, null, 2) + "\n",
);
console.log(
  `Prepared gallery and index for ${entries.length} real screenshots.`,
);
