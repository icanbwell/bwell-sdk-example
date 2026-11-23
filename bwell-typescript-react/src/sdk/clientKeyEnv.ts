// Client keys are base64 JSON containing the environment (for example "prod" or "client-sandbox").
// Old style keys are not JSON, so they have no environment.
// Kept free of app imports so vite.config.ts can use it too.
export const decodeEnv = (clientKey: string): string | undefined => {
  try {
    const decoded = JSON.parse(atob(clientKey)) as { env?: string };
    return typeof decoded.env === "string" && /^[a-z0-9-]+$/i.test(decoded.env) ? decoded.env : undefined;
  } catch {
    return undefined;
  }
};

const FALLBACK_ENV = "client-sandbox";

/** The b.well API host for the key's environment, for example https://api.prod.icanbwell.com. */
export const apiHostForKey = (clientKey: string | undefined | null): string =>
  `https://api.${(clientKey && decodeEnv(clientKey)) || FALLBACK_ENV}.icanbwell.com`;
