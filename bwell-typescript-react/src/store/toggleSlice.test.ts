import { describe, expect, test } from "vitest";
import toggleReducer, { selectToggle, toggleValue } from "./toggleSlice";

describe("toggleSlice", () => {
  test("selectToggle defaults to true for a locator that was never toggled", () => {
    const state = toggleReducer(undefined, { type: "@@INIT" });

    expect(selectToggle({ toggle: state } as any, "never-seen")).toBe(true);
  });

  // The reducer initializes an unseen locator to `true`, then immediately applies `!value`,
  // so the FIRST toggle of an unseen locator lands on `false` (turning the default-on view off).
  test("first toggle of an unseen locator sets it to false", () => {
    const state = toggleReducer(undefined, toggleValue("panel"));

    expect(state.panel).toBe(false);
  });

  test("a second toggle flips it back to true", () => {
    let state = toggleReducer(undefined, toggleValue("panel"));
    state = toggleReducer(state, toggleValue("panel"));

    expect(state.panel).toBe(true);
  });

  test("toggles distinct locators independently", () => {
    let state = toggleReducer(undefined, toggleValue("a"));
    state = toggleReducer(state, toggleValue("b"));
    state = toggleReducer(state, toggleValue("b"));

    expect(state.a).toBe(false);
    expect(state.b).toBe(true);
  });

  test("selectToggle reflects the stored value once a locator has been toggled", () => {
    const state = toggleReducer(undefined, toggleValue("x"));

    expect(selectToggle({ toggle: state } as any, "x")).toBe(false);
  });
});
