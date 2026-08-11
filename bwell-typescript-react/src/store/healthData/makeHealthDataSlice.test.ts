import { createAsyncThunk } from "@reduxjs/toolkit";
import { describe, expect, test } from "vitest";
import { makeHealthDataSlice } from "./makeHealthDataSlice";

const makeGetter = () => createAsyncThunk<any, void>("test/get", async () => ({}));

const build = () => {
  const getter = makeGetter();
  const slice = makeHealthDataSlice("test", getter);
  return { getter, slice, reducer: slice.reducer };
};

describe("makeHealthDataSlice", () => {
  test("initial state has no data, is not loading, and has no error", () => {
    const { reducer } = build();

    expect(reducer(undefined, { type: "@@INIT" })).toEqual({
      healthData: null,
      loading: false,
      error: null,
    });
  });

  test("pending clears prior data/error and sets loading", () => {
    const { getter, reducer } = build();

    const state = reducer(
      { healthData: [{ a: 1 }] as any, loading: false, error: "old" },
      getter.pending("rid", undefined),
    );

    expect(state.loading).toBe(true);
    expect(state.error).toBeNull();
    expect(state.healthData).toBeNull();
  });

  test("fulfilled stores the payload as healthData and stops loading", () => {
    const { getter, reducer } = build();
    const payload = { entry: [{ id: "x" }] };

    const state = reducer(
      { healthData: null, loading: true, error: null },
      getter.fulfilled(payload as any, "rid", undefined),
    );

    expect(state.healthData).toEqual(payload);
    expect(state.loading).toBe(false);
  });

  test("resetState returns the slice to its initial state", () => {
    const { slice } = build();

    const state = slice.reducer(
      { healthData: [{ a: 1 }] as any, loading: true, error: "e" },
      slice.actions.resetState(),
    );

    expect(state).toEqual({ healthData: null, loading: false, error: null });
  });

  test("rejected with 'Uninitialized' keeps the slice loading (waiting for init)", () => {
    const { getter, reducer } = build();

    const state = reducer(
      { healthData: null, loading: false, error: null },
      getter.rejected(new Error("Uninitialized"), "rid", undefined),
    );

    expect(state.loading).toBe(true);
  });

  test("rejected with any other error records the error message", () => {
    const { getter, reducer } = build();

    const state = reducer(
      { healthData: null, loading: true, error: null },
      getter.rejected(new Error("network down"), "rid", undefined),
    );

    expect(state.error).toBe("network down");
  });

  // BUG (see .qa/bugs-found.md #B1): the fulfilled handler sets `state.error` from
  // `action.payload.error.message`, then UNCONDITIONALLY overwrites it with "". A fulfilled
  // action that carries a business error therefore surfaces NO error to the UI. This asserts
  // the CORRECT behavior (error should be preserved) and is expected to fail until fixed.
  test.fails(
    "BUG: a fulfilled-with-error payload should surface the error, but it is overwritten with ''",
    () => {
      const { getter, reducer } = build();

      const state = reducer(
        { healthData: null, loading: true, error: null },
        getter.fulfilled(
          { error: { message: "boom" } } as any,
          "rid",
          undefined,
        ),
      );

      expect(state.error).toBe("boom");
    },
  );
});
