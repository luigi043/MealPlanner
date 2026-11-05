import { test, expect } from "@playwright/test";
import { mkdir } from "node:fs/promises";
const shots = "../docs/verification/demo";
async function shot(page, name) {
  await mkdir(shots, { recursive: true });
  await page.screenshot({ path: `${shots}/${name}.png` });
}
async function navigate(page, name) {
  await page
    .getByRole("navigation")
    .getByRole("button", { name, exact: true })
    .click();
}
async function noOverflow(page) {
  expect(
    await page.evaluate(
      () => document.documentElement.scrollWidth <= innerWidth,
    ),
  ).toBe(true);
  expect(
    await page
      .locator("main")
      .evaluate((el) => el.scrollWidth <= el.clientWidth + 1),
  ).toBe(true);
}
for (const [label, viewport] of [
  ["phone", { width: 390, height: 844 }],
  ["desktop", { width: 1440, height: 1080 }],
]) {
  test(`${label}: every screen, actual meal planning, grocery persistence and export`, async ({
    page,
  }) => {
    await page.setViewportSize(viewport);
    const errors = [];
    page.on("pageerror", (error) => errors.push(error.message));
    await page.goto("/");
    await expect(
      page.getByRole("heading", { name: "Good food, planned." }),
    ).toBeVisible();
    expect(
      (await page.locator("#phone").boundingBox()).width,
    ).toBeLessThanOrEqual(430);
    await shot(page, `${label}-01-home`);
    await noOverflow(page);
    await navigate(page, "Plan");
    await shot(page, `${label}-02-plan`);
    await noOverflow(page);
    await page.getByRole("button", { name: "Add dinner", exact: true }).click();
    await shot(page, `${label}-03-add-meal`);
    const modal = await page.locator("dialog").boundingBox(),
      phone = await page.locator("#phone").boundingBox();
    expect(modal.x).toBeGreaterThanOrEqual(phone.x);
    expect(modal.x + modal.width).toBeLessThanOrEqual(phone.x + phone.width);
    await page.getByLabel("Recipe", { exact: true }).selectOption("3");
    await page.getByLabel("Servings", { exact: true }).fill("2");
    await page
      .getByRole("button", { name: "Add to plan", exact: true })
      .click();
    await expect(
      page.getByRole("button", { name: "Remove Salmon with Quinoa" }),
    ).toBeAttached();
    await page.getByRole("button", { name: "Fill empty slots" }).click();
    await expect(page.getByRole("status")).toContainText(
      "18 empty meal slots filled",
    );
    await page.getByRole("button", { name: "Fill empty slots" }).click();
    await expect(page.getByRole("status")).toContainText("0 empty meal slots");
    await page.getByRole("button", { name: "Show week view" }).click();
    await shot(page, `${label}-04-week`);
    await noOverflow(page);
    await page.getByRole("button", { name: "Groceries", exact: true }).click();
    await shot(page, `${label}-05-groceries`);
    await noOverflow(page);
    await page.getByRole("button", { name: "Add grocery item" }).click();
    await page.getByLabel("Item name").fill("Portfolio test item");
    await page.getByLabel("Quantity").fill("2");
    await shot(page, `${label}-06-add-grocery`);
    await page.getByRole("button", { name: "Add item", exact: true }).click();
    await page.getByRole("checkbox", { name: /Portfolio test item/ }).check();
    await page.getByRole("button", { name: "From this week" }).click();
    await expect(
      page.getByRole("checkbox", { name: /Portfolio test item/ }),
    ).toBeChecked();
    const downloadPromise = page.waitForEvent("download");
    await page.getByRole("button", { name: "Export list" }).click();
    expect((await downloadPromise).suggestedFilename()).toBe(
      "mealplanner-groceries.txt",
    );
    await navigate(page, "Recipes");
    await shot(page, `${label}-07-recipes`);
    await noOverflow(page);
    await page.getByLabel("Search recipes").fill("Salmon");
    await expect(page.locator(".recipe-card")).toHaveCount(1);
    await page.getByRole("button", { name: "Breakfast", exact: true }).click();
    await expect(
      page.getByRole("heading", { name: "No matching recipes" }),
    ).toBeVisible();
    await shot(page, `${label}-08-empty-search`);
    await page.getByRole("button", { name: "Clear filters" }).click();
    await page
      .locator(".recipe-open")
      .filter({ hasText: "Grilled Chicken Salad" })
      .click();
    await shot(page, `${label}-09-recipe-detail`);
    await noOverflow(page);
    await expect(page.locator(".detail-macros")).toContainText("640");
    await page.getByRole("button", { name: "Increase servings" }).click();
    await expect(page.locator(".detail-macros")).toContainText("960");
    await page
      .getByRole("button", { name: "Edit recipe", exact: true })
      .click();
    await shot(page, `${label}-10-edit-recipe`);
    await page.getByRole("button", { name: "Cancel", exact: true }).click();
    await page.getByRole("button", { name: "Back to recipes" }).click();
    await page.getByRole("button", { name: "New recipe", exact: true }).click();
    await shot(page, `${label}-11-new-recipe`);
    await page
      .getByLabel("Recipe name", { exact: true })
      .fill("Portfolio rice");
    await page
      .getByLabel("Ingredients for the whole recipe")
      .fill("200 g Rice");
    await page
      .getByLabel("Instructions", { exact: true })
      .fill("Cook according to package instructions.");
    await page
      .getByRole("button", { name: "Save recipe", exact: true })
      .click();
    await expect(
      page.getByRole("heading", { name: "Portfolio rice" }),
    ).toBeVisible();
    await page.getByRole("button", { name: "Back to recipes" }).click();
    await navigate(page, "Settings");
    await shot(page, `${label}-12-settings`);
    await noOverflow(page);
    await page.getByRole("switch", { name: /Dark mode/ }).check();
    await page.getByLabel("Daily calorie target").fill("2300");
    await page.getByRole("button", { name: "Save target" }).click();
    await shot(page, `${label}-13-dark-settings`);
    await page.reload();
    await navigate(page, "Settings");
    await expect(page.getByRole("switch", { name: /Dark mode/ })).toBeChecked();
    await expect(page.getByLabel("Daily calorie target")).toHaveValue("2300");
    await page.getByRole("button", { name: "Reset demo data" }).click();
    await shot(page, `${label}-14-reset-dialog`);
    await page.getByRole("button", { name: "Reset demo", exact: true }).click();
    await expect(
      page.getByRole("heading", { name: "Good food, planned." }),
    ).toBeVisible();
    expect(errors).toEqual([]);
  });
}
test("small phone keeps navigation and dialogs reachable", async ({ page }) => {
  await page.setViewportSize({ width: 320, height: 640 });
  await page.goto("/");
  for (const screen of ["Home", "Plan", "Recipes", "Grocery", "Settings"]) {
    await navigate(page, screen);
    await noOverflow(page);
  }
  await navigate(page, "Recipes");
  await page.getByRole("button", { name: "New recipe", exact: true }).click();
  await expect(
    page.getByRole("button", { name: "Save recipe", exact: true }),
  ).toBeVisible();
  await shot(page, "small-phone-recipe-form");
});
