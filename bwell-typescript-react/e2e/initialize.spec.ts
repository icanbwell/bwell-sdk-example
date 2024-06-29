import InitializePage from "./InitializePage";
import { test, expect } from "@playwright/test";
import { config } from 'dotenv';

//initialize dotenv, access environment variables
config();

const DEFAULT_KEY = process.env.VITE_DEFAULT_KEY ?? "";
const DEFAULT_OAUTH_CREDS = process.env.VITE_DEFAULT_OAUTH_CREDS ?? "";
const BAD_KEY = process.env.VITE_BAD_KEY ?? "";
const BAD_OAUTH_CREDS = process.env.VITE_BAD_OAUTH_CREDS ?? "";
const WELL_FORMED_KEY_BAD_ENV = process.env.VITE_WELL_FORMED_KEY_BAD_ENV ?? "";
const WELL_FORMED_KEY_GOOD_ENV = process.env.VITE_WELL_FORMED_KEY_GOOD_ENV ?? "";

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

test("Initialize page displays OAuth Creds after valid client key is entered", async ({ page }) => {
    //arrange
    const initializePage = new InitializePage(page);

    //act
    await initializePage.navigate();
    await initializePage.enterClientKey(DEFAULT_KEY);
    await initializePage.clickInitializeButton();

    const oauthCredsLocator = await page.waitForSelector(initializePage.txtOauthCredsLocator);
    const oauthCredsVisible = await oauthCredsLocator.isVisible();

    //assert: oauth creds textarea should be visible
    expect(oauthCredsVisible).toBe(true);
});

test("Entering a valid key and valid oauth credential results in success", async ({ page }) => {
    //arrange
    const initializePage = new InitializePage(page);

    //act
    await initializePage.navigate();
    await initializePage.enterClientKey(DEFAULT_KEY);
    await initializePage.clickInitializeButton();

    const oauthCredsLocator = await page.waitForSelector(initializePage.txtOauthCredsLocator);
    await oauthCredsLocator.fill(DEFAULT_OAUTH_CREDS);

    await initializePage.clickSubmitButton();

    //assert: no error message should be displayed
    const infoElement = await page.waitForSelector(initializePage.infoMessageLocator);
    const infoText = await infoElement.textContent();
    expect(infoText).toBe("User successfully logged in.");
});



test("Entering an invalid client key results in an error", async ({ page }) => {
    //arrange
    const initializePage = new InitializePage(page);

    //act
    await initializePage.navigate();
    await initializePage.enterClientKey(BAD_KEY);
    await initializePage.clickInitializeButton();

    //assert: error message should be displayed
    const errorElement = await page.waitForSelector(initializePage.errorMessageLocator);
    const errorText = await errorElement.textContent();
    expect(errorText).toBe("It appears there is a problem with your Client Key. Contact b.well support for further assistance.");
});

test("Entering an invalid oauth credential results in an error", async ({ page }) => {
    //arrange
    const initializePage = new InitializePage(page);

    //act
    await initializePage.navigate();
    await initializePage.enterClientKey(DEFAULT_KEY);
    await initializePage.clickInitializeButton();

    const oauthCredsLocator = await page.waitForSelector(initializePage.txtOauthCredsLocator);
    await oauthCredsLocator.fill(BAD_OAUTH_CREDS);

    await initializePage.clickSubmitButton();
});