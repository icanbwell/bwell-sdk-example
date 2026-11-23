import { apiHostForKey } from "@/sdk/clientKeyEnv";

// Everything a client is expected to customise lives here.
export const CUSTOMER_NAME = "[Customer]";
export const DISCONNECT_METHOD = "[describe method]";
export const PRIVACY_STATEMENT_URL = "https://www.icanbwell.com/legal/privacy-and-security-statement/";

// Optional: where CLEAR should send the user back. Redirects are configured per
// environment in CCS; leave unset to let the platform default apply.
export const CLEAR_CALLBACK_URL: string | undefined = import.meta.env.VITE_CLEAR_CALLBACK_URL || undefined;

export type NavItem = { id: string; label: string; to: string };

export const MAIN_NAV: readonly NavItem[] = [
  { id: "connections", label: "Connect Records", to: "/app/connections" },
  { id: "health", label: "View Health Data", to: "/app/health" },
  { id: "chat", label: "Chat", to: "/app/chat" },
  { id: "settings", label: "Settings", to: "/app/settings" },
];

// The Chat page uses the b.well health assistant API (the "bailey" Responses API) (OpenAI-compatible).
// In dev, requests always go through the Vite dev server's /bailey proxy (see vite.config.ts) because the bailey
// API rejects the browser's CORS pre-check from localhost; the proxy picks the host from the client key.
// Otherwise they go straight to the API, on the host for the client key's environment
// (https://api.<env>.icanbwell.com). Override a built app's URL with VITE_BAILEY_BASE_URL.
export const baileyBaseUrl = (clientKey: string): string =>
  import.meta.env.DEV ? "/bailey/v1" : import.meta.env.VITE_BAILEY_BASE_URL || `${apiHostForKey(clientKey)}/bailey/v1`;
export const BAILEY_MODEL = "Bailey AI";
// The preset questions shown as buttons on the Chat page. Edit freely.
export const BAILEY_PROMPTS: readonly string[] = [
  "Give me a summary of my health.",
  "What medications am I currently taking?",
  "What were my most recent lab results, and what do they mean?",
  "What conditions are in my records?",
  "Are there any health screenings or vaccines I may be due for?",
];

// Health data types shown under "View Health Data". Order is the order of the tabs.
export const HEALTH_DATA_TYPES = [
  { id: "conditions", label: "Conditions" },
  { id: "medications", label: "Medications" },
  { id: "allergies", label: "Allergies" },
  { id: "labs", label: "Labs" },
  { id: "vitals", label: "Vital signs" },
  { id: "immunizations", label: "Immunizations" },
  { id: "procedures", label: "Procedures" },
  { id: "encounters", label: "Encounters" },
  { id: "care-plans", label: "Care plans" },
  { id: "reports", label: "Diagnostic reports" },
] as const;

export type HealthDataTypeId = (typeof HEALTH_DATA_TYPES)[number]["id"];

// Extra, non-data sub-pages for "View Health Data". v2 adds
// { id: "share", label: "Share my records", to: "/app/health/share" } and a matching route.
export const HEALTH_EXTRA_TABS: readonly NavItem[] = [];
