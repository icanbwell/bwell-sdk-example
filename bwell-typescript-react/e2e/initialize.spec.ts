import InitializePage from "./InitializePage";
import { test, expect } from "@playwright/test";
import { config } from 'dotenv';

//initialize dotenv so we can access the environment variables
config();

const DEFAULT_KEY = process.env.VITE_DEFAULT_KEY ?? "";
const DEFAULT_OAUTH_CREDS = process.env.VITE_DEFAULT_OAUTH_CREDS ?? "";

test("Navigating to the Initialize page works", async ({ page }) => {
    //arrange
    const initializePage = new InitializePage(page);

    //act
    await initializePage.navigate();

    //assert: page title should be "b.well Typescript SDK Example Web App"
    expect(await page.title()).toBe("b.well Typescript SDK Example Web App");
});

test("Initialize page doesn't display OAuth Creds initially", async ({ page }) => {
    //arrange
    const initializePage = new InitializePage(page);

    //act
    await initializePage.navigate();

    //assert: oauth creds textarea should not be visible
    expect(await page.isVisible(initializePage.txtOauthCredsLocator)).toBe(false);
});

test("Initialize page displays OAuth Creds after client key is entered", async ({ page }) => {
    //arrange
    const initializePage = new InitializePage(page);

    //act
    await initializePage.navigate();
    await initializePage.enterClientKey(DEFAULT_KEY);
    await initializePage.clickInitializeButton();

    //assert: oauth creds textarea should not be visible
    expect(await page.isVisible(initializePage.txtOauthCredsLocator)).toBe(true);
});