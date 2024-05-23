import InitializePage from "./InitializePage";
import { test, expect } from "@playwright/test";

const VALID_KEY = "";

test("Navigating to the initialize page works", async ({ page }) => {
    //arrange
    const initializePage = new InitializePage(page);

    //act
    await initializePage.navigate();

    //assert: page title should be "b.well Typescript SDK Example Web App"
    expect(await page.title()).toBe("b.well Typescript SDK Example Web App");
});
