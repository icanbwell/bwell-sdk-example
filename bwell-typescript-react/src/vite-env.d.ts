/// <reference types="vite/client" />
interface ImportMetaEnv {
  readonly VITE_CLEAR_CALLBACK_URL?: string;
  readonly VITE_DESCOPE_PROJECT_ID?: string;
  readonly VITE_DESCOPE_AUTH_DOMAIN?: string;
  readonly VITE_BAILEY_BASE_URL?: string;
  readonly VITE_BAILEY_PROXY_TARGET?: string;
}
