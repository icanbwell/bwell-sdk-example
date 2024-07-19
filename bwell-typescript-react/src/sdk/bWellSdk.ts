import { BWellSDK } from '@icanbwell/bwell-sdk-ts';

let bWellSdk: BWellSDK;

export const authenticateSdk = async (oauthCreds: string) => {
    return bWellSdk.authenticate({ token: oauthCreds });
}

export const initializeSdk = async (clientKey: string) => {
    bWellSdk = new BWellSDK({ clientKey });
    const initializationResult = await bWellSdk.initialize();
    console.log(initializationResult);
    return initializationResult;
};    

export const getSdk = () => {
    if (bWellSdk) return bWellSdk;
}