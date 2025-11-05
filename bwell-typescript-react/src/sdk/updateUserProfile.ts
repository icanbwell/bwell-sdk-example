import { getSdk } from "@/sdk/bWellSdk";
import { UpdateProfileRequest } from "@icanbwell/bwell-sdk-ts";

/**
 * Calls the UserManager.updateProfile function from the bWell SDK.
 * Updates the current user's profile information.
 * @param profileData - The profile fields to update (excluding id)
 */
export async function updateUserProfile(profileData: Record<string, any>) {
  const bWellSdk = getSdk();
  // Directly pass the flat profileData to UpdateProfileRequest
  return bWellSdk?.user.updateProfile(new UpdateProfileRequest(profileData));
}
