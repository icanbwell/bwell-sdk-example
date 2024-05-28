import { Browser, Page } from "playwright";

declare global {
  const page: Page;
  const browser: Browser;
  const browserName: string;
}

const config = {
    TEST_APP_URL: "http://localhost:5173",
}

export default config;
