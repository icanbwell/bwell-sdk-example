import { getSdk } from "@/sdk/bWellSdk";
import {
    CreateConsentRequest,
    type CategoryCode,
    type ConsentProvisionType,
    type ConsentStatus,
} from "@icanbwell/bwell-sdk-ts";

export type CreateConsentInput = {
    category: CategoryCode;
    provision: ConsentProvisionType;
    status?: ConsentStatus;
    organizationId?: string;
};

export type SdkCallResult<T = unknown> = {
    ok: boolean;
    data: T | null;
    error: unknown;
    raw: unknown;
    shape: {
        hasSuccessFn: boolean;
        hasFailureFn: boolean;
        hasDataFn: boolean;
        hasErrorFn: boolean;
        keys: string[];
        constructorName: string | null;
    };
};

function inspectSdkResultShape(result: any) {
    return {
        hasSuccessFn: typeof result?.success === "function",
        hasFailureFn: typeof result?.failure === "function",
        hasDataFn: typeof result?.data === "function",
        hasErrorFn: typeof result?.error === "function",
        keys: result ? Object.keys(result) : [],
        constructorName: result?.constructor?.name ?? null,
    };
}

export function serializeForDebug(value: unknown) {
    try {
        return JSON.parse(
            JSON.stringify(
                value,
                (_key, currentValue) => {
                    if (typeof currentValue === "function") {
                        return `[Function ${currentValue.name || "anonymous"}]`;
                    }

                    if (currentValue instanceof Error) {
                        return {
                            name: currentValue.name,
                            message: currentValue.message,
                            stack: currentValue.stack,
                        };
                    }

                    return currentValue;
                },
                2
            )
        );
    } catch {
        return {
            note: "Value could not be JSON serialized cleanly",
            fallback: String(value),
        };
    }
}

export function getErrorMessage(error: unknown): string {
    if (!error) return "Unexpected error";
    if (typeof error === "string") return error;

    if (typeof error === "object" && error !== null) {
        const maybeMessage = (error as any).message;
        if (typeof maybeMessage === "string" && maybeMessage.trim()) {
            return maybeMessage;
        }

        try {
            return JSON.stringify(serializeForDebug(error), null, 2);
        } catch {
            return "Unexpected error";
        }
    }

    return String(error);
}

function normalizeSdkResult<T = unknown>(result: any): SdkCallResult<T> {
    const shape = inspectSdkResultShape(result);

    console.log("[consents sdk] normalizeSdkResult.raw =", result);
    console.log("[consents sdk] normalizeSdkResult.shape =", shape);

    if (!result) {
        return {
            ok: false,
            data: null,
            error: new Error("SDK call returned no result."),
            raw: result,
            shape,
        };
    }

    try {
        if (typeof result.failure === "function" && result.failure()) {
            const parsedError =
                typeof result.error === "function" ? result.error() : result.error;

            console.error("[consents sdk] normalizeSdkResult.failure.error =", parsedError);

            return {
                ok: false,
                data: null,
                error: parsedError,
                raw: result,
                shape,
            };
        }

        if (typeof result.success === "function" && result.success()) {
            const parsedData =
                typeof result.data === "function" ? result.data() : result.data;

            console.log("[consents sdk] normalizeSdkResult.success.data =", parsedData);

            return {
                ok: true,
                data: (parsedData ?? null) as T | null,
                error: null,
                raw: result,
                shape,
            };
        }

        const fallbackData =
            typeof result?.data === "function" ? result.data() : result?.data;
        const fallbackError =
            typeof result?.error === "function" ? result.error() : result?.error;

        console.warn("[consents sdk] normalizeSdkResult.fallback.data =", fallbackData);
        console.warn("[consents sdk] normalizeSdkResult.fallback.error =", fallbackError);

        return {
            ok: !fallbackError,
            data: (fallbackData ?? null) as T | null,
            error: fallbackError ?? null,
            raw: result,
            shape,
        };
    } catch (error) {
        console.error("[consents sdk] normalizeSdkResult.threw =", error);

        return {
            ok: false,
            data: null,
            error,
            raw: result,
            shape,
        };
    }
}

export async function getConsents(): Promise<SdkCallResult<any>> {
    console.log("[consents sdk] getConsents.start");

    const sdk = getSdk();

    console.log("[consents sdk] getConsents.sdkExists =", !!sdk);

    if (!sdk) {
        return {
            ok: false,
            data: null,
            error: new Error("SDK not initialized."),
            raw: null,
            shape: {
                hasSuccessFn: false,
                hasFailureFn: false,
                hasDataFn: false,
                hasErrorFn: false,
                keys: [],
                constructorName: null,
            },
        };
    }

    try {
        console.log("[consents sdk] getConsents.aboutToCall sdk.user.getConsents()");
        const result = await sdk.user.getConsents();
        console.log("[consents sdk] getConsents.rawResult =", result);

        return normalizeSdkResult(result);
    } catch (error) {
        console.error("[consents sdk] getConsents.threw =", error);

        return {
            ok: false,
            data: null,
            error,
            raw: error,
            shape: {
                hasSuccessFn: false,
                hasFailureFn: false,
                hasDataFn: false,
                hasErrorFn: false,
                keys: [],
                constructorName: error instanceof Error ? error.constructor.name : null,
            },
        };
    }
}

export async function createConsent(input: CreateConsentInput): Promise<SdkCallResult<any>> {
    console.log("[consents sdk] createConsent.start input =", input);

    const sdk = getSdk();

    console.log("[consents sdk] createConsent.sdkExists =", !!sdk);

    if (!sdk) {
        return {
            ok: false,
            data: null,
            error: new Error("SDK not initialized."),
            raw: null,
            shape: {
                hasSuccessFn: false,
                hasFailureFn: false,
                hasDataFn: false,
                hasErrorFn: false,
                keys: [],
                constructorName: null,
            },
        };
    }

    try {
        const request = new CreateConsentRequest({
            status: input.status ?? "ACTIVE",
            provision: input.provision,
            category: input.category,
            organizationId: input.organizationId,
        });

        console.log("[consents sdk] createConsent.request =", request);
        console.log("[consents sdk] createConsent.requestPayload =", {
            status: input.status ?? "ACTIVE",
            provision: input.provision,
            category: input.category,
            organizationId: input.organizationId,
        });

        const result = await sdk.user.createConsent(request);

        console.log("[consents sdk] createConsent.rawResult =", result);

        return normalizeSdkResult(result);
    } catch (error) {
        console.error("[consents sdk] createConsent.threw =", error);

        return {
            ok: false,
            data: null,
            error,
            raw: error,
            shape: {
                hasSuccessFn: false,
                hasFailureFn: false,
                hasDataFn: false,
                hasErrorFn: false,
                keys: [],
                constructorName: error instanceof Error ? error.constructor.name : null,
            },
        };
    }
}
