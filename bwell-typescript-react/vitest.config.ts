import { fileURLToPath } from "node:url";
import { defineConfig } from "vitest/config";

// Unit-test config for the sample app's logic layer (Redux slices + SDK wrappers).
// Mirrors the "@" -> ./src alias from vite.config.ts. Node environment (no DOM) is
// sufficient because this suite targets logic only, not React components.
export default defineConfig({
  resolve: {
    alias: {
      "@": fileURLToPath(new URL("./src", import.meta.url)),
    },
  },
  test: {
    globals: true,
    environment: "node",
    include: ["src/**/*.test.ts"],
  },
});
