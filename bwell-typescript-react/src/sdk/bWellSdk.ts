import { RootState } from '@/store/store';
import { BWellSDK } from '@icanbwell/bwell-sdk-ts';

let bWellSdk: BWellSDK;

export const authenticateSdk = async (oauthCredentials: string) => {
    return bWellSdk.authenticate({ token: oauthCredentials });
}

export const initializeSdk = (clientKey: string) => {
    bWellSdk = new BWellSDK({ clientKey });
};

export const getSdk = async (state: RootState) => {
    if (bWellSdk) return bWellSdk;

    if (!state.user) throw new Error("SDK not initialized");

    const { clientKey, oauthCreds } = state.user;

    if (clientKey && oauthCreds) {
        bWellSdk = new BWellSDK({ clientKey });
        await bWellSdk.authenticate({ token: oauthCreds });

        return bWellSdk;
    }

    throw new Error("SDK not initialized");
}