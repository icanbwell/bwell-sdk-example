import { describe, expect, test } from "vitest";
import { connectionSlice, getMemberConnections } from "./connectionSlice";

const reducer = connectionSlice.reducer;

describe("connectionSlice", () => {
  test("initial state has no connections and no error", () => {
    expect(reducer(undefined, { type: "@@INIT" })).toEqual({
      memberConnections: null,
      dataSource: null,
      loading: false,
      error: null,
    });
  });

  test("pending clears data and sets loading", () => {
    const state = reducer(
      { memberConnections: [{ id: "old" }] as any, dataSource: null, loading: false, error: "e" },
      getMemberConnections.pending("rid", undefined),
    );
    expect(state.loading).toBe(true);
    expect(state.memberConnections).toBeNull();
    expect(state.error).toBeNull();
  });

  test("fulfilled stores the returned connections and stops loading", () => {
    const state = reducer(
      { memberConnections: null, dataSource: null, loading: true, error: null },
      getMemberConnections.fulfilled([{ id: "a" }] as any, "rid", undefined),
    );
    expect(state.memberConnections).toEqual([{ id: "a" }]);
    expect(state.loading).toBe(false);
  });

  test("rejected with 'Uninitialized' keeps the slice loading", () => {
    const state = reducer(
      { memberConnections: null, dataSource: null, loading: false, error: null },
      getMemberConnections.rejected(new Error("Uninitialized"), "rid", undefined),
    );
    expect(state.loading).toBe(true);
  });

  test("rejected with another error records the message", () => {
    const state = reducer(
      { memberConnections: null, dataSource: null, loading: true, error: null },
      getMemberConnections.rejected(new Error("network down"), "rid", undefined),
    );
    expect(state.error).toBe("network down");
  });

  // BUG (see .qa/bugs-found.md #B1): same defect as makeHealthDataSlice — the fulfilled handler
  // reads `action.payload.error.message` but then unconditionally overwrites `state.error` with "".
  // A fulfilled response that carries a business error therefore surfaces NO error. This asserts
  // the CORRECT behavior and is expected to fail until fixed.
  test.fails(
    "BUG: a fulfilled-with-error payload should surface the error, but it is overwritten with ''",
    () => {
      const state = reducer(
        { memberConnections: null, dataSource: null, loading: true, error: null },
        getMemberConnections.fulfilled({ error: { message: "eek" } } as any, "rid", undefined),
      );
      expect(state.error).toBe("eek");
    },
  );
});
