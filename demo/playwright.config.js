import { defineConfig } from "@playwright/test";
export default defineConfig({
  testDir: "./tests/browser",
  workers: 1,
  timeout: 45000,
  use: {
    baseURL: process.env.DEMO_URL || "http://127.0.0.1:4173",
    headless: true,
    launchOptions: process.env.BROWSER_PATH
      ? { executablePath: process.env.BROWSER_PATH }
      : {},
    screenshot: "only-on-failure",
  },
  reporter: [["list"], ["json", { outputFile: "test-results/results.json" }]],
  webServer: process.env.DEMO_URL
    ? undefined
    : {
        command: "node server.mjs",
        url: "http://127.0.0.1:4173",
        reuseExistingServer: true,
      },
});
