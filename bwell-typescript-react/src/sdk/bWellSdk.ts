import { BWellSDK } from '@icanbwell/bwell-sdk-ts';

let bWellSdk: BWellSDK;

export const authenticateSdk = async (oauthCreds: string) => {
    return bWellSdk.authenticate({ token: oauthCreds });
}

export const initializeSdk = (clientKey: string) => {
    bWellSdk = new BWellSDK({ clientKey });
};    

export const getSdk = () => {
    if (bWellSdk) return bWellSdk;
}