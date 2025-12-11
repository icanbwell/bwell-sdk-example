import { BWellSDK } from '@icanbwell/bwell-sdk-ts';

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