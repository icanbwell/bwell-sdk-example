import HelloPage from "./HelloPage";
import { test, expect } from "@playwright/test";

test("Hello World", async ({ page }) => {
  const helloPage = new HelloPage(page);
  await helloPage.navigate();

  //verify that the response is empty
  const helloResponseEmpty = await helloPage.getHelloResponse();
  expect(helloResponseEmpty).toBe("");

  //click the hello button, check that the box populates as expected
  await helloPage.clickHelloButton();
  const helloResponsePopulated = await helloPage.getHelloResponse();
  expect(helloResponsePopulated).toBe("World!");

  //test the clear button as long as we're in here
  await helloPage.clickClearButton();
  const secondEmptyHelloResponse = await helloPage.getHelloResponse();
  expect(secondEmptyHelloResponse).toBe("");
});
