import { PlaywrightTestConfig } from "@playwright/test";

const config: PlaywrightTestConfig = {
  // Configure Playwright the directory where tests are located
  testDir: "e2e",
  // Configure a timeout for the tests
  timeout: 5000,
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
  webServer: {
    // Configure Playwright to start a web server locall before running the tests
    command: "npm run dev",
    // Configure Playwright to use a specific port rather than the default 3000
    port: 5173,
    // Configure Playwright to wait for the server to be ready before running the tests
    timeout: 120 * 1000
  }
};

export default config;
