import { createAsyncThunk } from "@reduxjs/toolkit";
import { getSdk } from "@/sdk/bWellSdk";
import { DataSourceRequest } from "@icanbwell/bwell-sdk-ts";

export const deleteConnection = createAsyncThunk(
  "connections/deleteConnection",
  async (connectionId: string) => {
    const sdk = getSdk();
    if (!sdk) throw new Error("SDK not initialized");
    await sdk.connection.deleteConnection(
      new DataSourceRequest({ connectionId })
    );
    return connectionId;
  }
);
