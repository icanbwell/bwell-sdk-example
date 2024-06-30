import { createAsyncThunk } from "@reduxjs/toolkit";
import { getSdk } from "@/sdk/bWellSdk";
import makeHealthDataSlice from "./healthData/makeHealthDataSlice";
import { BWellQueryResult } from "../../.yalc/@icanbwell/bwell-sdk-ts/dist/common/results";
import { MemberConnectionResults } from "../../.yalc/@icanbwell/bwell-sdk-ts/dist/api/base/connection-manager";

export const getMemberConnections = createAsyncThunk(
    "connections/memberConnections",
    async () => {
        const bWellSdk = getSdk();
        return bWellSdk?.connection.getMemberConnections();
    }
);

//TODO: Create makeConnectionDataSlice
export const connectionSlice = makeHealthDataSlice<BWellQueryResult<MemberConnectionResults>>("connections", getMemberConnections);