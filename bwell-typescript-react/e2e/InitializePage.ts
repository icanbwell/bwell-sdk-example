import { Page } from "@playwright/test";
import config from "./configTypes";

class InitializePage {
    private readonly page: Page;

    public readonly mainMenuLocator = "#btnMainMenu";
    public readonly initializeMenuItemLocator = "#initializeMenuItem";
    public readonly txtKeyLocator = "#txtKey";
    public readonly btnInitializeLocator = "#btnInitialize";
    public readonly txtOauthCredsLocator = "#txtOauthCreds";
    public readonly btnSubmitLocator = "#btnSubmit";
    public readonly infoMessageLocator = "#authenticationInfo";
    public readonly errorMessageLocator = "#authenticationError";

    constructor(page: Page) {
        this.page = page;
    }

    async navigate() {
        await this.page.goto(config.TEST_APP_URL);
        await this.clickMainMenu();
        await this.clickInitializeMenuItem();
    }

    async clickMainMenu() {
        await this.page.click(this.mainMenuLocator);
    }

    async clickInitializeMenuItem() {
        await this.page.locator(this.initializeMenuItemLocator).click();
    }

    async clickInitializeButton() {
        await this.page.click(this.btnInitializeLocator);
    }

    async enterClientKey(key: string) {
        await this.page.fill(this.txtKeyLocator, key);
    }

    async enterOAuthCreds(creds: string) {
        await this.page.fill(this.txtOauthCredsLocator, creds);
    }

    async clickSubmitButton() {
        await this.page.click(this.btnSubmitLocator);
    }
}

export default InitializePage;
