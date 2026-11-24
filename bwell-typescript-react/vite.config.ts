import { defineConfig, loadEnv, type Plugin } from "vite";
import { createProxyMiddleware } from "http-proxy-middleware";
import react from "@vitejs/plugin-react-swc";
import { fileURLToPath } from "node:url";
import { apiHostForKey } from "./src/sdk/clientKeyEnv";

// Dev only: the bailey API rejects the browser's CORS pre-check from localhost, so the dev server forwards
// /bailey/* to it and the browser sees a same-origin call. A deployed app needs the API to allow its origin
// (or its own backend proxy). Vite's own server.proxy can't pick a target per request, so this uses
// http-proxy-middleware: the target is the API host for the environment in the request's client key
// (api-key header), and VITE_BAILEY_PROXY_TARGET overrides it.
const baileyDevProxy = (override?: string): Plugin => ({
  name: "bailey-dev-proxy",
  configureServer(server) {
    server.middlewares.use(
      createProxyMiddleware({
        pathFilter: "/bailey",
        target: apiHostForKey(undefined),
        router: (req) => override || apiHostForKey(req.headers["api-key"] as string | undefined),
        changeOrigin: true,
      }),
    );
  },
});

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), "VITE_");
  return {
    plugins: [react(), baileyDevProxy(env.VITE_BAILEY_PROXY_TARGET)],
    resolve: {
      alias: { "@": fileURLToPath(new URL("./src", import.meta.url)) },
    },
    test: { globalSetup: ["./src/test/globalSetup.ts"] },
  };
});
