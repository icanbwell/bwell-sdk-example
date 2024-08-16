import { describe, it, expect, beforeAll } from 'vitest';
import { AuthType, BWellSDK, UpdateProfileRequest } from '@icanbwell/bwell-sdk-ts';

const DEFAULT_KEY = process.env.VITE_DEFAULT_KEY ?? "";
const DEFAULT_OAUTH_CREDS = process.env.VITE_DEFAULT_OAUTH_CREDS ?? "";
const VALID_USERNAME = process.env.VITE_VALID_USERNAME ?? "";
const VALID_PASSWORD = process.env.VITE_VALID_PASSWORD ?? "";
const BAD_PASSWORD = process.env.VITE_BAD_PASSWORD ?? "";;
const USERNAME_PASSWORD_KEY = process.env.VITE_USERNAME_PASSWORD_KEY ?? "";

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

describe("Username/Password Authentication", () => {
    it("Should work with valid username/password", async () => {
        sdk = new BWellSDK({
            clientKey: USERNAME_PASSWORD_KEY,
            authType: AuthType.UsernamePassword,
        });

        await sdk.initialize();

        const authenticationResult = await sdk.authenticate({
            email: VALID_USERNAME,
            password: VALID_PASSWORD,
        });

        expect(authenticationResult).toBeDefined();
        expect(authenticationResult.success()).toBe(true);
    });

    it("Should fail with invalid username/password", async () => {
        sdk = new BWellSDK({
            clientKey: USERNAME_PASSWORD_KEY,
        });

        await sdk.initialize();

        const authenticationResult = await sdk.authenticate({
            email: VALID_USERNAME,
            password: BAD_PASSWORD,
        });

        expect(authenticationResult).toBeDefined();
        expect(authenticationResult.failure()).toBe(true);
        expect(authenticationResult.error().message).toBe('Invalid username or password');
    });
});