import { getSdk } from "@/sdk/bWellSdk";

/**
 * Calls the UserManager.getProfile function from the bWell SDK.
 * Returns the current user's profile information.
 */
export async function getUserProfile() {
  const bWellSdk = getSdk();
  return bWellSdk?.user.getProfile();
}
