import { SearchToken } from "@icanbwell/bwell-sdk-ts";
import { createSlice, PayloadAction } from "@reduxjs/toolkit";

export interface HealthDataRequestInfo {
    page: number;
    pageSize: number;
    groupCode?: SearchToken;
}

interface HealthDataRequestInfoCollection {
    [locator: string]: HealthDataRequestInfo;
}

export const INITIAL_REQUEST: HealthDataRequestInfo = {
    page: 0,
    pageSize: 10,
};

const getOrInitializeRequestInfo = (state: HealthDataRequestInfoCollection, selector: string): HealthDataRequestInfo => {
    if (!state[selector]) {
        state[selector] = { ...INITIAL_REQUEST };
    }

    return state[selector];
}

const createRequestInfoReducer = <T>(updateFn: (requestInfo: HealthDataRequestInfo, action: PayloadAction<T>) => void) => {
    return (state: HealthDataRequestInfoCollection, action: PayloadAction<T>) => {
        const key = (action.payload as any).selector || action.payload as string;
        const requestInfo = getOrInitializeRequestInfo(state, key);
        updateFn(requestInfo, action);
        state[key] = requestInfo;
    };
};

const initialState: HealthDataRequestInfoCollection = {};

export const requestInfoSlice = createSlice({
    name: "requests",
    initialState,
    reducers: {
        setPage: createRequestInfoReducer<{ selector: string, page: number }>((requestInfo, action) => {
            requestInfo.page = action.payload.page;
        }),
        setPageSize: createRequestInfoReducer<{ selector: string, pageSize: number }>((requestInfo, action) => {
            requestInfo.pageSize = action.payload.pageSize;
        }),
        clearRequestInfo: createRequestInfoReducer<string>((requestInfo) => {
            requestInfo.page = INITIAL_REQUEST.page;
            requestInfo.pageSize = INITIAL_REQUEST.pageSize;
            requestInfo.groupCode = undefined;
        }),
        setGroupCode: createRequestInfoReducer<{ selector: string, groupCode: string }>((requestInfo, action) => {
            requestInfo.groupCode = {
                value: {
                    code: action.payload.groupCode,
                }
            }
        }),
        clearGroupCode: createRequestInfoReducer<string>((requestInfo) => {
            requestInfo.groupCode = undefined;
        }),
    },
});