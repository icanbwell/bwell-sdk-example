# health.network sample app (v2)

A branded reference app for the b.well TypeScript SDK, built around the health.network journey:

login → terms of service → how to connect (CLEAR / provider search) → **Connect Records** · **View Health Data** · **Settings**

## Run

```bash
npm install
npm run dev
```

Enter your b.well **client key**, then sign in with one of these methods, in order of preference:

1. **Username and password (Descope)**, for clients that use Descope as their identity provider. Once the client key is entered, the app reads the client's Descope configuration and shows an email and password form. Descope's tokens are kept in memory only and handed to the SDK. See `src/components/DescopeLogin.tsx`.
2. **External IdP token**, for environments set up for token auth.
3. **b.well tokens** (access and ID, plus refresh if you have one), as a fallback for when the options above don't fit: sign in to the b.well web app and copy the tokens it issues. The refresh token is optional: without it the session works only until the access token expires (the SDK's refresh step fails and is tolerated).

The SDK keeps its tokens in `sessionStorage` (see `src/sdk/sessionTokenStorage.ts`) so the session survives a page reload (and the full-page redirect used when a popup is blocked). Everything is cleared on sign out or when the tab closes.

## Redirects

CLEAR and provider OAuth open in a popup (`src/components/useExternalFlow.ts`), so the app stays where it is. While the popup is open the page shows a panel with **Done verifying** / **Done connecting**, **Reopen window** and **Cancel**. Pressing Done re-checks the real state (verification status, connections) before moving on.

After CLEAR or provider OAuth, the popup returns to `/oauth/return`. It tells the main tab through a `BroadcastChannel` and closes itself, so the user usually doesn't need to press Done.

If the browser blocks the popup, the app falls back to a full-page redirect. The return page then forwards to the connections page, and the session is restored from `sessionStorage`.

## Structure

`src/sdk/journey.ts` holds every SDK call (inject the `BWellSDK`); `src/auth/` holds the session and route guards; `src/pages/` has one file per screen.
