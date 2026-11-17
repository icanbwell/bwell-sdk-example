# health.network sample app (v2)

A branded reference app for the b.well TypeScript SDK, built around the health.network journey:

login → terms of service → how to connect (CLEAR / provider search) → **Connect Records** · **View Health Data** · **Settings**

## Run

```bash
npm install
npm run dev
```

Sign in with a b.well **client key** plus either:

- **External IdP token**, for environments set up for token auth; or
- **b.well tokens** (access and ID, plus refresh if you have one), for username/password environments: sign in to the b.well web app and copy the tokens it issues. The refresh token is optional: without it the session works only until the access token expires (the SDK's refresh step fails and is tolerated).

The SDK keeps its tokens in `sessionStorage` (see `src/sdk/sessionTokenStorage.ts`) so the session survives a page reload (and the full-page redirect used when a popup is blocked). Everything is cleared on sign out or when the tab closes.

## Customise

- `src/config/app.ts`: customer name, disconnect instructions, nav items, health sub-pages.
- `src/config/tos.ts`: terms copy, assembled from the config above.
- `src/theme/theme.ts`: 2026 brand palette and fonts (Quicksand, Open Sans).
- Optional `VITE_CLEAR_CALLBACK_URL` in `.env.local`.
- Chat: `BAILEY_PROMPTS` in `src/config/app.ts` sets the preset questions. In `npm run dev`, calls go through a dev-server proxy to the sandbox bailey API (the API rejects the browser's CORS pre-check from localhost). Set `VITE_BAILEY_PROXY_TARGET` (e.g. `https://api.prod.icanbwell.com`) for production keys. A deployed build calls the API directly, so the API must allow your origin, or you need your own backend proxy; `VITE_BAILEY_BASE_URL` overrides the URL.

## Redirects

CLEAR and provider OAuth open in a popup (`src/components/useExternalFlow.ts`), so the app stays where it is. While the popup is open the page shows a panel with **Done verifying** / **Done connecting**, **Reopen window** and **Cancel**. Pressing Done re-checks the real state (verification status, connections) before moving on.

Redirect URLs are configured per environment in CCS. Register `/oauth/return` for OAuth, and set `VITE_CLEAR_CALLBACK_URL` to the same page for CLEAR (e.g. `http://localhost:5173/oauth/return`). When the popup lands there it tells the main tab through a `BroadcastChannel` and closes itself, so the user usually doesn't need to press Done. If the CLEAR callback is left at the platform default, the popup ends on a b.well page and the user presses Done instead.

If the browser blocks the popup, the app falls back to a full-page redirect. The return page then forwards to the connections page, and the session is restored from `sessionStorage`.

## Planned for v2

- **Share my records**: a sub-page of View Health Data (add to `HEALTH_TABS` and the `/app/health` route).
- **Chat**: a fourth main-nav item for a conversational AI screen (add to `MAIN_NAV`).
- Fix the OAuth drop-off so users return to where they left off.

## Structure

`src/sdk/journey.ts` holds every SDK call (inject the `BWellSDK`); `src/auth/` holds the session and route guards; `src/pages/` has one file per screen.
