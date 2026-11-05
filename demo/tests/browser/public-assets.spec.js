import { test, expect } from "@playwright/test";
import { createHash } from "node:crypto";

test("public gallery and downloadable APK match the verification evidence", async ({
  page,
  request,
}) => {
  await page.goto("/gallery.html");
  await expect(
    page.getByRole("heading", { name: "MealPlanner, screen by screen." }),
  ).toBeVisible();
  await expect(page.locator("figure")).toHaveCount(49);
  const images = await page
    .locator("figure img")
    .evaluateAll((elements) => elements.map((el) => el.getAttribute("src")));
  for (const path of images) {
    const response = await request.head(path);
    expect(response.status(), path).toBe(200);
    expect(response.headers()["content-type"], path).toContain("image/png");
  }
  await page.setViewportSize({ width: 390, height: 844 });
  expect(
    await page.evaluate(
      () => document.documentElement.scrollWidth <= innerWidth,
    ),
  ).toBe(true);
  const infoResponse = await request.get("/downloads/build-info.json");
  expect(infoResponse.status()).toBe(200);
  const info = await infoResponse.json();
  const apkResponse = await request.get("/downloads/mealplanner-debug.apk");
  expect(apkResponse.status()).toBe(200);
  const apk = await apkResponse.body();
  expect(apk.length).toBe(info.bytes);
  expect(createHash("sha256").update(apk).digest("hex")).toBe(info.sha256);
  expect(info.applicationId).toBe("com.mealplanner.debug");
});
