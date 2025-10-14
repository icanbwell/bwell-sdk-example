import { getSdk } from "@/sdk/bWellSdk";
import { DataSourceRequest } from "@icanbwell/bwell-sdk-ts";

export async function deleteConnectionById(connectionId: string) {
  const sdk = getSdk();
  if (!sdk) throw new Error("SDK not initialized");
  // DataSourceRequest expects { connectionId }
  const req = new DataSourceRequest({ connectionId });
  return sdk.connection.deleteConnection(req);
}
