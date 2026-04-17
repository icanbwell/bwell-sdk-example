import { BWellSDK } from '@icanbwell/bwell-sdk-ts';

let bWellSdk: BWellSDK | undefined;

export const authenticateSdk = async (creds: string | { email: string; password: string }) => {
    if (!bWellSdk) {
        throw new Error('SDK not initialized. Please initialize the SDK first.');
    }
    if (typeof creds === 'string') {
        return bWellSdk.authenticate({ token: creds });
    } else {
        // Some SDK versions validate username/password, others validate email/password.
        // Try username first (matches current lockfile), then retry with email only
        // when the SDK explicitly reports an invalid credentials-type shape.
        const usernameAttempt = await bWellSdk.authenticate({
            username: creds.email,
            password: creds.password,
        });
        if (
            !usernameAttempt.success() &&
            usernameAttempt.error()?.message?.includes('Invalid credentials type provided')
        ) {
            return bWellSdk.authenticate({ email: creds.email, password: creds.password } as any);
        }
        return usernameAttempt;
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