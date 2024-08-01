import { describe, it, expect } from 'vitest';
import { AllergyIntolerancesRequest, BWellSDK } from '@icanbwell/bwell-sdk-ts';

const DEFAULT_KEY = process.env.VITE_DEFAULT_KEY ?? "";
const DEFAULT_OAUTH_CREDS = process.env.VITE_DEFAULT_OAUTH_CREDS ?? "";

describe("SDK Health Data", () => {
    it("should fetch allergy intolerances with an empty code", async () => {
        const sdk = new BWellSDK({
            clientKey: DEFAULT_KEY,
        });

        await sdk.initialize();

        await sdk.authenticate({
            token: DEFAULT_OAUTH_CREDS,
        });

        const request = new AllergyIntolerancesRequest({
            page: 0,
            pageSize: 10,
            groupCode: {
                values: [
                    {
                        code: "",
                    }
                ]
            }
        });

        const result = await sdk.health.getAllergyIntolerances(request);

        expect(result).toBeDefined();
        expect(result.hasError()).toBe(true);
        expect(result.error?.message).toBe('Each Coding in the list must have at least one non-null and non-empty value for system or code');
    });
});