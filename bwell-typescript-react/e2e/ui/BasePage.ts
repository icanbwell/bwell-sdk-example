import { Page } from "@playwright/test";
import config from "./configTypes";

export class BasePage {
    protected readonly page: Page;

    public readonly mainMenuLocator = "#btnMainMenu";
    public readonly menuItemSuffix = "MenuItem";

    constructor(page: Page) {
        this.page = page;
    }

    protected async clickMainMenu() {
        await this.page.click(this.mainMenuLocator);
    }

    protected async navigate() {
        await this.page.goto(config.TEST_APP_URL);
    }

    protected async clickMenuItem(menuItem: string) {
        await this.page.click(menuItem);
    }
}