import { Page } from 'playwright';
import config from "./configTypes";

export class ShadowPageModel {
    private page: Page;

    private readonly mainMenuLocator = "#btnMainMenu";
    private readonly shadowMenuItemLocator = "#shadowMenuItem";
    private readonly cpShadowLocator = "#cpShadow";
    private readonly btnStartSpinnerLocator = "#btnStartSpinner";
    private readonly shadowOuterTextSpanLocator = "#spanOuterText";
    private readonly shadowInnerTextSpanLocator = "#spanInnerText";

    // Initialize the page object
    constructor(page: Page) {
        this.page = page;
    }

    async navigate() {
        await this.page.goto(config.TEST_APP_URL);
        await this.clickMainMenu();
        await this.clickShadowMenuItem();
    }

    async clickMainMenu() {
        await this.page.click(this.mainMenuLocator);
    }

    async clickShadowMenuItem() {
        await this.page.locator(this.shadowMenuItemLocator).click();
    }

    async getOuterDivText() {
        return await this.page.locator(this.shadowOuterTextSpanLocator).innerText();
    }

    async getInnerDivText() {
        return await this.page.locator(this.shadowInnerTextSpanLocator).innerText();
    }

    async startSpinner() {
        await this.page.click(this.btnStartSpinnerLocator);
    }

    async isSpinnerVisible() {
        return await this.page.locator(this.cpShadowLocator).isVisible();
    }
}

export default ShadowPageModel;