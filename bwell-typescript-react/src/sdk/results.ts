import type { BWellQueryResult, BWellTransactionResult } from "@icanbwell/bwell-sdk-ts";

// The SDK returns two result shapes: queries expose `.data`/`.error` properties,
// transactions expose `.success()`/`.data()`/`.error()` methods. These helpers give
// callers one throwing style so UI code only has to catch.
export class SdkError extends Error {}

/** The SDK couldn't renew the user's tokens: the session is over and they need to sign in again. */
export class SessionExpiredError extends SdkError {}
export const SESSION_EXPIRED_EVENT = "bwell:session-expired";
export const SESSION_EXPIRED_MESSAGE = "Your session has expired. Please sign in again.";

/**
 * A failed token refresh surfaces from any API call. Rather than show that raw message on every page,
 * tell the session (which signs the user out) and throw a clear error.
 */
function fail(message: string): SdkError {
  if (!/token refresh failed/i.test(message)) return new SdkError(message);
  if (typeof window !== "undefined") window.dispatchEvent(new Event(SESSION_EXPIRED_EVENT));
  return new SessionExpiredError(SESSION_EXPIRED_MESSAGE);
}

const messageOf = (error: unknown, fallback: string): string => {
  if (!error || typeof error !== "object") return fallback;
  const e = error as { name?: string; message?: unknown; cause?: unknown };
  const message = typeof e.message === "string" && e.message ? e.message : "";
  const cause = e.cause && typeof e.cause === "object" && typeof (e.cause as { message?: unknown }).message === "string"
    ? (e.cause as { message: string }).message : "";
  const detail = [message, cause && cause !== message ? cause : ""].filter(Boolean).join(": ");
  if (!detail) return fallback;
  return e.name && e.name !== "Error" && !detail.includes(e.name) ? `${detail} (${e.name})` : detail;
};

export function unwrapQuery<T>(result: BWellQueryResult<T, any>, fallback = "Request failed"): T {
  if (result.hasError()) throw fail(messageOf(result.error, fallback));
  if (result.data === undefined) throw new SdkError(`${fallback}: the server returned no data`);
  return result.data;
}

/**
 * Queries can return data AND errors (e.g. a resolver failing for part of a record set). Keep the
 * data when there is some and log the error (the message only) so a partial failure stays diagnosable.
 */
export function unwrapPartialQuery<T>(result: BWellQueryResult<T, any>, fallback = "Request failed"): { data: T } {
  if (result.data === undefined) return { data: unwrapQuery(result, fallback) };
  if (result.hasError()) console.warn("Query returned partial data:", messageOf(result.error, fallback));
  return { data: result.data };
}

export function unwrapTransaction<T>(result: BWellTransactionResult<T, any>, fallback = "Request failed"): T {
  if (!result.success()) throw fail(messageOf(result.error(), fallback));
  return result.data();
}

export const errorMessage = (e: unknown, fallback = "Something went wrong"): string =>
  e instanceof Error && e.message ? e.message : fallback;
