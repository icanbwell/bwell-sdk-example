import { BWellSDK } from '@icanbwell/bwell-sdk-ts';

export let bWellSdk: BWellSDK;

export const initializeSdk = async (clientKey: string) => {
    bWellSdk = new BWellSDK({ clientKey });
}