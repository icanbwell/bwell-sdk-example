import type { TokenStorage } from "@icanbwell/bwell-sdk-ts";

const PREFIX = "bwell-sdk:";

/**
 * Keeps the SDK's tokens in sessionStorage so they survive the full-page redirects
 * used by CLEAR and OAuth, and are dropped when the tab closes. Because the SDK
 * writes refreshed tokens back here, a restored session never replays stale ones.
 */
export class SessionTokenStorage implements TokenStorage {
  async get(key: string) {
    return sessionStorage.getItem(PREFIX + key) ?? undefined;
  }
  async getMany(keys: string[]): Promise<[string, string | undefined][]> {
    return keys.map((k) => [k, sessionStorage.getItem(PREFIX + k) ?? undefined]);
  }
  async set(key: string, value: string) {
    sessionStorage.setItem(PREFIX + key, value);
  }
  async setMany(keyValues: [string, string][]) {
    keyValues.forEach(([k, v]) => sessionStorage.setItem(PREFIX + k, v));
  }
  async delete(key: string) {
    const had = sessionStorage.getItem(PREFIX + key) !== null;
    sessionStorage.removeItem(PREFIX + key);
    return had;
  }
}
