import { BasePage } from "./BasePage";

export class InitializePage extends BasePage {
    public readonly txtKeyLocator = "#txtKey";
    public readonly btnInitializeLocator = "#btnInitialize";
    public readonly txtOauthCredsLocator = "#txtOauthCreds";
    public readonly btnSubmitLocator = "#btnSubmit";
    public readonly infoMessageLocator = "#authenticationInfo";
    public readonly errorMessageLocator = "#authenticationError";

    async navigate() {
        await super.navigate();
        await this.clickMainMenu();
        await this.clickMenuItem(`#initialize${this.menuItemSuffix}`);
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
