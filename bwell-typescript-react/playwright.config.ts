import { PlaywrightTestConfig } from "@playwright/test";

const config: PlaywrightTestConfig = {
  testDir: "e2e", // Specify the directory where your tests are located
  timeout: 5000, // Specify a timeout for your tests
  use: {
    // Configure Playwright to record videos
    video: "on",
    // Configure Playwright to record traces
    trace: "on",
    // Configure Playwright to take screenshots
    screenshot: "on",
    launchOptions: {
      // Configure Playwright to run headless
      headless: true,
      // Configure Playwright to run in slow mo
      slowMo: 500,
    },
  },
};

export default config;
