// sdkMiddleware.js
import { REHYDRATE } from 'redux-persist';
import { authenticateSdk, initializeSdk } from '@/sdk/bWellSdk';

const sdkMiddleware = (store) => (next) => async (action) => {
    if (action.type === REHYDRATE) {
        //if it's a brand-new session, just set the user state to the default 
        if (!action.payload?.user) {
            action.payload = {
                user: {
                    isRehydrated: true,
                }
            }

            return next(action);
        }

        const { clientKey, oauthCreds } = action.payload.user;

        action.payload.user.isInitialized = false;
        action.payload.user.isLoggedIn = false;

        try {
            if (clientKey && oauthCreds) {
                await initializeSdk(clientKey);
                await authenticateSdk(oauthCreds);

                action.payload.user.isInitialized = true;
                action.payload.user.isLoggedIn = true;
            }
        } catch (error) {
            console.error('Error rehydrating SDK:', error);
        }

        action.payload.user.isRehydrated = true;
    }

    return next(action);
};

export default sdkMiddleware;
