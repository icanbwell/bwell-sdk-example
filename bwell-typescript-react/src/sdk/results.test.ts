import { afterEach, beforeEach, describe, expect, test, vi } from "vitest";
import type { BWellQueryResult } from "@icanbwell/bwell-sdk-ts";
import { SdkError, SESSION_EXPIRED_EVENT, SessionExpiredError, unwrapQuery } from "@/sdk/results";

const failing = (message: string) =>
  ({ hasError: () => true, data: undefined, error: { name: "OperationOutcomeError", message } }) as unknown as BWellQueryResult<unknown, any>;

describe("unwrapQuery with a failed token refresh", () => {
  const events = new EventTarget();
  const heard = vi.fn();
  beforeEach(() => {
    vi.stubGlobal("window", events);
    events.addEventListener(SESSION_EXPIRED_EVENT, heard);
  });
  afterEach(() => {
    events.removeEventListener(SESSION_EXPIRED_EVENT, heard);
    heard.mockClear();
    vi.unstubAllGlobals();
  });

  test.each(["Token refresh failed", "token refresh failed (identity-gateway)"])("%s ends the session", (message) => {
    expect(() => unwrapQuery(failing(message))).toThrow(SessionExpiredError);
    expect(heard).toHaveBeenCalledTimes(1);
  });

  test("other errors are passed through without ending the session", () => {
    expect(() => unwrapQuery(failing("Something else broke"))).toThrow(SdkError);
    expect(() => unwrapQuery(failing("Something else broke"))).not.toThrow(SessionExpiredError);
    expect(heard).not.toHaveBeenCalled();
  });
});
