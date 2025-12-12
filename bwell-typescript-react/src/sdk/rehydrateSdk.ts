// sdkMiddleware.js
import { REHYDRATE } from 'redux-persist';
import { authenticateSdk, initializeSdk } from '@/sdk/bWellSdk';

// @ts-ignore TODO: strong-type these functions
const sdkMiddleware = () => (next) => async (action) => {
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

        // Reset to clean state - only restore if we have valid credentials
        action.payload.user.isInitialized = false;
        action.payload.user.isLoggedIn = false;
        action.payload.user.error = null;
        action.payload.user.clientKey = undefined;
        action.payload.user.oauthCreds = undefined;

        // Only rehydrate if we have both clientKey AND credentials (oauthCreds)
        // This ensures we don't persist a half-initialized state
        if (clientKey && oauthCreds) {
            try {
                await initializeSdk(clientKey);
                await authenticateSdk(oauthCreds);

                // Only persist state if both init and auth succeeded
                action.payload.user.clientKey = clientKey;
                action.payload.user.oauthCreds = oauthCreds;
                action.payload.user.isInitialized = true;
                action.payload.user.isLoggedIn = true;
            } catch (error) {
                console.error('Error rehydrating SDK:', error);
                // State remains cleared - user will need to re-initialize
            }
        }

        action.payload.user.isRehydrated = true;
    }

    return next(action);
};

export default sdkMiddleware;
