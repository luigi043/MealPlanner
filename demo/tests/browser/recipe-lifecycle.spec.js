import { test, expect } from "@playwright/test";
import { mkdir } from "node:fs/promises";

test("recipe validation, safe text, edits and deletion keep linked plans consistent", async ({
  page,
}) => {
  await page.setViewportSize({ width: 390, height: 844 });
  const errors = [];
  page.on("pageerror", (error) => errors.push(error.message));
  await page.goto("/");
  const nav = (name) =>
    page
      .getByRole("navigation")
      .getByRole("button", { name, exact: true })
      .click();
  const shot = async (name) => {
    await mkdir("../docs/verification/demo", { recursive: true });
    await page.screenshot({ path: `../docs/verification/demo/${name}.png` });
  };
  await nav("Grocery");
  await expect(
    page.getByRole("heading", { name: "A clean shopping list" }),
  ).toBeVisible();
  await shot("phone-15-empty-grocery");
  await nav("Recipes");
  await page.getByRole("button", { name: "New recipe", exact: true }).click();
  const name = "<img src=x onerror=alert(1)> Rice";
  await page.getByLabel("Recipe name", { exact: true }).fill(name);
  await page
    .getByLabel("Instructions", { exact: true })
    .fill("Use the package directions.");
  await page
    .getByLabel("Ingredients for the whole recipe", { exact: true })
    .fill("Rice");
  await page.getByRole("button", { name: "Save recipe", exact: true }).click();
  await expect(page.getByRole("alert")).toContainText("Each ingredient needs");
  await shot("phone-16-invalid-ingredient");
  await page
    .getByLabel("Ingredients for the whole recipe", { exact: true })
    .fill("200 g Rice");
  await page.getByRole("button", { name: "Save recipe", exact: true }).click();
  await expect(page.getByRole("heading", { name, exact: true })).toBeVisible();
  await expect(page.locator("img[src=x]")).toHaveCount(0);
  await page.getByRole("button", { name: "Edit recipe", exact: true }).click();
  await page
    .getByLabel("Recipe name", { exact: true })
    .fill("Edited portfolio rice");
  await page.getByLabel("Calories (kcal)", { exact: true }).fill("125");
  await page.getByRole("button", { name: "Save changes", exact: true }).click();
  await expect(
    page.getByRole("heading", { name: "Edited portfolio rice" }),
  ).toBeVisible();
  await expect(page.locator(".detail-macros")).toContainText("250");
  await page
    .getByRole("button", { name: "Add to meal plan", exact: true })
    .click();
  await page.getByRole("button", { name: "Add to plan", exact: true }).click();
  await expect(
    page.getByRole("button", {
      name: "Remove Edited portfolio rice",
      exact: true,
    }),
  ).toBeAttached();
  await nav("Recipes");
  await page
    .locator(".recipe-open")
    .filter({ hasText: "Edited portfolio rice" })
    .click();
  await page
    .getByRole("button", { name: "Delete recipe", exact: true })
    .click();
  await shot("phone-17-delete-dialog");
  await page
    .getByRole("dialog")
    .getByRole("button", { name: "Delete recipe", exact: true })
    .click();
  await expect(
    page.locator(".recipe-open").filter({ hasText: "Edited portfolio rice" }),
  ).toHaveCount(0);
  await nav("Plan");
  await expect(
    page.getByRole("button", { name: "Remove Edited portfolio rice" }),
  ).toHaveCount(0);
  expect(errors).toEqual([]);
});
