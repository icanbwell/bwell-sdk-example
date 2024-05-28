import { Page } from "@playwright/test";
import config from "./configTypes";

class HelloPage {
  private readonly page: Page;

  private readonly mainMenuLocator = "#btnMainMenu";
  private readonly helloMenuItemLocator = "#helloMenuItem";
  private readonly helloButtonLocator = "#btnHello";
  private readonly clearButtonLocator = "#btnClear";
  private readonly helloResponseLocator = "#preHelloResponse";

  constructor(page: Page) {
    this.page = page;
  }

  async navigate() {
    await this.page.goto(config.TEST_APP_URL);
    await this.clickMainMenu();
    await this.clickHelloMenuItem();
  }

  async clickMainMenu() {
    await this.page.click(this.mainMenuLocator);
  }

  async clickHelloMenuItem() {
    await this.page.locator(this.helloMenuItemLocator).click();
  }

  async clickHelloButton() {
    await this.page.click(this.helloButtonLocator);
  }

  async clickClearButton() {
    await this.page.click(this.clearButtonLocator);
  }

  async getHelloResponse() {
    return await this.page.textContent(this.helloResponseLocator);
  }
}

export default HelloPage;
