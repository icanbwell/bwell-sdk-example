import { describe, it, expect, beforeAll } from 'vitest';
import { BWellSDK, UpdateProfileRequest } from '@icanbwell/bwell-sdk-ts';

const DEFAULT_KEY = process.env.VITE_DEFAULT_KEY ?? "";
const DEFAULT_OAUTH_CREDS = process.env.VITE_DEFAULT_OAUTH_CREDS ?? "";

let sdk: BWellSDK;

describe("User Manager", () => {
    beforeAll(async () => {
        sdk = new BWellSDK({
            clientKey: DEFAULT_KEY,
        });

        await sdk.initialize();

        await sdk.authenticate({
            token: DEFAULT_OAUTH_CREDS,
        });
    });

    it("Should handle HTTP 500 errors well", async () => {
        const updateProfileRequest = new UpdateProfileRequest({
            firstName: 'Kate2',
            lastName: 'Kolchier2',
            addressStreet: '123 Test St',
            addressUnit: '#1',
            city: 'Austin2',
            stateOrProvidence: 'OH',
            postageOrZipCode: '78653',
            homePhone: '1234567890',
            mobilePhone: '4567890123',
            workPhone: '7890123456',
            birthDate: '2000-01-01',
            gender: 'female',
        });

        const updateProfileResult = await sdk.user.updateProfile(updateProfileRequest)

        expect(updateProfileResult).toBeDefined();
        expect(updateProfileResult.failure()).toBe(true);
        expect(updateProfileResult.error().message).toBe('');
    });
});