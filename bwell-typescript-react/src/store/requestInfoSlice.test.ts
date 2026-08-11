import { describe, expect, test } from "vitest";
import { INITIAL_REQUEST, requestInfoSlice } from "./requestInfoSlice";

const reducer = requestInfoSlice.reducer;
const actions = requestInfoSlice.actions;
const empty = () => reducer(undefined, { type: "@@INIT" });

describe("requestInfoSlice", () => {
  test("lazily initializes a request entry for a new selector on setPage", () => {
    const state = reducer(empty(), actions.setPage({ selector: "labs", page: 3 }));

    expect(state.labs.page).toBe(3);
    expect(state.labs.pageSize).toBe(INITIAL_REQUEST.pageSize);
  });

  test("setPageSize updates pageSize while preserving page", () => {
    let state = reducer(empty(), actions.setPage({ selector: "labs", page: 3 }));
    state = reducer(state, actions.setPageSize({ selector: "labs", pageSize: 25 }));

    expect(state.labs.pageSize).toBe(25);
    expect(state.labs.page).toBe(3);
  });

  test("tracks selectors independently", () => {
    let state = reducer(empty(), actions.setPage({ selector: "labs", page: 2 }));
    state = reducer(state, actions.setPage({ selector: "conditions", page: 5 }));

    expect(state.labs.page).toBe(2);
    expect(state.conditions.page).toBe(5);
  });

  test("setGroupCode uses the encounters-specific shape for the 'encounters' selector", () => {
    const state = reducer(
      empty(),
      actions.setGroupCode({ selector: "encounters", groupCode: { code: "E1", system: "sys" } as any }),
    );

    expect(state.encounters.groupCode).toEqual({ value: { value: "E1", system: "sys" } });
  });

  test("setGroupCode uses the default (code/value) shape for other selectors", () => {
    const state = reducer(
      empty(),
      actions.setGroupCode({ selector: "labs", groupCode: { code: "C1", value: "V1" } as any }),
    );

    expect(state.labs.groupCode).toEqual({ value: { code: "C1", value: "V1" } });
  });

  test("clearRequestInfo resets page, pageSize, and groupCode to defaults", () => {
    let state = reducer(empty(), actions.setPage({ selector: "labs", page: 7 }));
    state = reducer(state, actions.setPageSize({ selector: "labs", pageSize: 50 }));
    state = reducer(state, actions.setGroupCode({ selector: "labs", groupCode: { code: "C", value: "V" } as any }));

    state = reducer(state, actions.clearRequestInfo("labs"));

    expect(state.labs.page).toBe(INITIAL_REQUEST.page);
    expect(state.labs.pageSize).toBe(INITIAL_REQUEST.pageSize);
    expect(state.labs.groupCode).toBeUndefined();
  });

  test("clearGroupCode removes only the group code", () => {
    let state = reducer(
      empty(),
      actions.setGroupCode({ selector: "labs", groupCode: { code: "C", value: "V" } as any }),
    );
    state = reducer(state, actions.setPage({ selector: "labs", page: 4 }));

    state = reducer(state, actions.clearGroupCode("labs"));

    expect(state.labs.groupCode).toBeUndefined();
    expect(state.labs.page).toBe(4);
  });
});
