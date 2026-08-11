import { describe, expect, test } from "vitest";
import { authenticate, initialize, userSlice } from "./userSlice";

const reducer = userSlice.reducer;
const initial = reducer(undefined, { type: "@@INIT" });

describe("userSlice", () => {
  test("initial state is logged out, uninitialized, and not loading", () => {
    expect(initial.isLoggedIn).toBe(false);
    expect(initial.isInitialized).toBe(false);
    expect(initial.loading).toBe(false);
    expect(initial.error).toBeNull();
  });

  test("resetState clears session flags and marks the store rehydrated", () => {
    const state = reducer(
      { ...initial, isInitialized: true, isLoggedIn: true, clientKey: "k", oauthCreds: "c" },
      userSlice.actions.resetState(),
    );

    expect(state.isInitialized).toBe(false);
    expect(state.isLoggedIn).toBe(false);
    expect(state.isRehydrated).toBe(true);
    expect(state.clientKey).toBeUndefined();
    expect(state.oauthCreds).toBeUndefined();
  });

  describe("authenticate thunk", () => {
    test("pending sets loading and clears the logged-in flag", () => {
      const state = reducer(initial, authenticate.pending("rid", {}));
      expect(state.loading).toBe(true);
      expect(state.isLoggedIn).toBe(false);
    });

    test("fulfilled stores the credentials and marks logged in", () => {
      const state = reducer(initial, authenticate.fulfilled("creds-123", "rid", {}));
      expect(state.oauthCreds).toBe("creds-123");
      expect(state.isLoggedIn).toBe(true);
      expect(state.loading).toBe(false);
    });

    test("rejected records the reject payload and stays logged out", () => {
      const state = reducer(
        { ...initial, loading: true },
        authenticate.rejected(new Error("boom"), "rid", {}, "bad creds"),
      );
      expect(state.error).toBe("bad creds");
      expect(state.isLoggedIn).toBe(false);
      expect(state.loading).toBe(false);
    });

    test("rejected falls back to a default message when no payload is provided", () => {
      const state = reducer(initial, authenticate.rejected(new Error("boom"), "rid", {}));
      expect(state.error).toBe("Error while authenticating");
    });
  });

  describe("initialize thunk", () => {
    test("fulfilled stores the client key and marks initialized", () => {
      const state = reducer(
        initial,
        initialize.fulfilled("client-key", "rid", { clientKey: "client-key" }),
      );
      expect(state.clientKey).toBe("client-key");
      expect(state.isInitialized).toBe(true);
      expect(state.loading).toBe(false);
    });

    test("rejected clears credentials and marks the SDK uninitialized", () => {
      const state = reducer(
        { ...initial, clientKey: "k", oauthCreds: "c", isInitialized: true, isLoggedIn: true },
        initialize.rejected(new Error("boom"), "rid", { clientKey: "k" }, "init failed"),
      );
      expect(state.isInitialized).toBe(false);
      expect(state.error).toBe("init failed");
      expect(state.clientKey).toBeUndefined();
      expect(state.oauthCreds).toBeUndefined();
    });
  });
});
