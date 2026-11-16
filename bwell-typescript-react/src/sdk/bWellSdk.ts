import { BWellSDK, BWellTransactionResult } from '@icanbwell/bwell-sdk-ts';

let bWellSdk: BWellSDK | undefined;

export const authenticateSdk = async (creds: string | { username: string; password: string }) => {
    if (!bWellSdk) {
        throw new Error('SDK not initialized. Please initialize the SDK first.');
    }
    if (typeof creds === 'string') {
        return bWellSdk.authenticate({ token: creds });
    } else {
        return bWellSdk.authenticate({ username: creds.username, password: creds.password });
    }
}

export type DescopeAuthTokens = {
    accessToken: string;
    idToken: string;
    // Descope normally keeps the refresh token in an HttpOnly cookie, so a browser
    // app usually has only the session token.
    refreshToken?: string;
};

type SdkAuthTokens = Parameters<BWellSDK['setAuthTokens']>[0];

// Used for clients on Descope: the user signs in with Descope, then the resulting
// tokens are handed to the SDK. Username/password login via the SDK only works for
// clients that are still on Cognito.
//
// Without a refresh token, v1 of the SDK accepts the session token as is. v2 finishes
// setting up the session and then tries to refresh, and reports InvalidTokenError because
// there is no refresh token. The access token is already installed at that point and
// works until it expires, so that one error is treated as success. This keeps the same
// code working on both SDK versions. When the token expires, the user signs in again.
export const authenticateSdkWithTokens = async (tokens: DescopeAuthTokens) => {
    if (!bWellSdk) {
        throw new Error('SDK not initialized. Please initialize the SDK first.');
    }
    const result = await bWellSdk.setAuthTokens(tokens as SdkAuthTokens);
    if (!tokens.refreshToken && result.failure() && result.error().name === 'InvalidTokenError') {
        return BWellTransactionResult.success(null);
    }
    return result;
}

export const initializeSdk = async (clientKey: string) => {
    bWellSdk = new BWellSDK({ clientKey });
    const initializationResult = await bWellSdk.initialize();
    return initializationResult;
};

export const getSdk = () => {
    return bWellSdk;
}

export const isSdkInitialized = () => {
    return bWellSdk !== undefined;
}