// sdkMiddleware.js
import { REHYDRATE } from 'redux-persist';
import { authenticateSdk, initializeSdk } from '@/sdk/bWellSdk';

const sdkMiddleware = (store) => (next) => async (action) => {
    if (action.type === REHYDRATE) {
        const { clientKey, oauthCreds } = action.payload.user;

        action.payload.user.isInitialized = false;
        action.payload.user.isLoggedIn = false;

        try {
            if (clientKey && oauthCreds) {
                initializeSdk(clientKey);
                await authenticateSdk(oauthCreds);

                action.payload.user.isInitialized = true;
                action.payload.user.isLoggedIn = true;
            }
        } catch (error) {
            console.error('Error rehydrating SDK:', error);
        }
    }

    return next(action);
};

export default sdkMiddleware;
