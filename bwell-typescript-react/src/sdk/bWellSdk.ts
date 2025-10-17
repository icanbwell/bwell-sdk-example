import { BWellSDK } from '@icanbwell/bwell-sdk-ts';

let bWellSdk: BWellSDK;

export const authenticateSdk = async (creds: string | { email: string; password: string }) => {
    if (typeof creds === 'string') {
        return bWellSdk.authenticate({ token: creds });
    } else {
        return bWellSdk.authenticate({ email: creds.email, password: creds.password });
    }
}

export const initializeSdk = async (clientKey: string) => {
    bWellSdk = new BWellSDK({ clientKey });
    const initializationResult = await bWellSdk.initialize();
    return initializationResult;
};    

export const getSdk = () => {
    if (bWellSdk) return bWellSdk;
}