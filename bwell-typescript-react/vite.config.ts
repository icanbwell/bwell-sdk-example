import { defineConfig, loadEnv } from "vite";
import react from "@vitejs/plugin-react-swc";
import { fileURLToPath } from "node:url";
import { apiHostForKey } from "./src/sdk/clientKeyEnv";

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), "VITE_");
  return {
    plugins: [react()],
    resolve: {
      alias: { "@": fileURLToPath(new URL("./src", import.meta.url)) },
    },
    server: {
      // Dev only: the bailey API rejects the browser's CORS pre-check from localhost, so the dev server
      // forwards /bailey/* to it and the browser sees a same-origin call. A deployed app needs the API to
      // allow its origin (or its own backend proxy). The target is the API host for the environment in the
      // request's client key (api-key header); VITE_BAILEY_PROXY_TARGET overrides it.
      proxy: {
        "/bailey": {
          target: apiHostForKey(undefined),
          router: (req) => env.VITE_BAILEY_PROXY_TARGET || apiHostForKey(req.headers["api-key"] as string | undefined),
          changeOrigin: true,
        },
      },
    },
  };
});
