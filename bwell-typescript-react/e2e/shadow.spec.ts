import ShadowPage from "./ShadowPage";
import { test, expect } from "@playwright/test";

test("Spinner starts on page load", async ({ page }) => {
    const shadowPage = new ShadowPage(page);

    await shadowPage.navigate();

    expect(await shadowPage.isSpinnerVisible()).toBe(true);
});

test("Spinner stops after 3 seconds", async ({ page }) => {
    const shadowPage = new ShadowPage(page);

    await shadowPage.navigate();

    expect(await shadowPage.isSpinnerVisible()).toBe(true);
    await page.waitForTimeout(3000);
    expect(await shadowPage.isSpinnerVisible()).toBe(false);
});

test("When spinner stops, the shadow component is visible", async ({ page }) => {
    const shadowPage = new ShadowPage(page);

    await shadowPage.navigate();

    expect(await shadowPage.getInnerDivText()).toBe("This is the inner div in the Shadow DOM");
    expect(await shadowPage.getOuterDivText()).toBe("This is the outer div in the Shadow DOM");
});