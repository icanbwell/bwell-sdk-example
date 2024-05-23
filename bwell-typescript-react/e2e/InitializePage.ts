import { Page } from "@playwright/test";
import config from "./configTypes";

class InitializePage {
    private readonly page: Page;

    private readonly mainMenuLocator = "#btnMainMenu";
    private readonly initializeMenuItemLocator = "#initializeMenuItem";
    private readonly txtKeyLocator = "#txtKey";
    private readonly btnInitializeLocator = "#btnInitialize";
    private readonly txtOauthCredsLocator = "#txtOauthCreds";
    private readonly btnLoginLocator = "#btnLogin";

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
}

export default InitializePage;
