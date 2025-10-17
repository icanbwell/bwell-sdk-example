import { getSdk } from "@/sdk/bWellSdk";

/**
 * Calls the UserManager.getProfile function from the bWell SDK.
 * Returns the current user's profile information.
 */
export async function getUserProfile() {
  const bWellSdk = getSdk();
  const result = await bWellSdk?.user.getProfile();
  const data = (result && typeof result.data === 'object' && result.data !== null) ? result.data : {};
  const d: any = data; // Cast to any for safe property access
  const flat: any = { ...d };

  // Safely extract address fields
  const addressArr = Array.isArray(d.address) ? d.address : [];
  if (addressArr[0]) {
    flat.addressStreet = addressArr[0]?.line?.[0] || "";
    flat.city = addressArr[0]?.city || "";
    flat.state = addressArr[0]?.state || "";
    flat.postalCode = addressArr[0]?.postalCode || "";
  } else {
    flat.addressStreet = "";
    flat.city = "";
    flat.state = "";
    flat.postalCode = "";
  }

  // Safely extract telecom fields
  const telecomArr = Array.isArray(d.telecom) ? d.telecom : [];
  flat.homePhone = telecomArr.find((t: any) => t.system === "phone" && t.use === "home")?.value || "";
  flat.mobilePhone = telecomArr.find((t: any) => t.system === "phone" && t.use === "mobile")?.value || "";
  flat.workPhone = telecomArr.find((t: any) => t.system === "phone" && t.use === "work")?.value || "";
  flat.email = telecomArr.find((t: any) => t.system === "email")?.value || "";

  // Safely extract name fields
  const nameArr = Array.isArray(d.name) ? d.name : [];
  if (nameArr[0]) {
    flat.firstName = nameArr[0]?.given?.[0] || "";
    flat.lastName = nameArr[0]?.family || "";
  } else {
    flat.firstName = "";
    flat.lastName = "";
  }

  // Remove parent fields that are objects/arrays and have children already mapped
  ["address", "telecom", "name"].forEach((field) => {
    if (flat[field]) {
      delete flat[field];
    }
  });

  // Ensure all required fields are present
  return {
    id: flat.id ?? null,
    firstName: flat.firstName ?? "",
    lastName: flat.lastName ?? "",
    gender: flat.gender ?? "",
    birthDate: flat.birthDate ?? "",
    language: flat.language ?? "",
    addressStreet: flat.addressStreet ?? "",
    city: flat.city ?? "",
    state: flat.state ?? "",
    postalCode: flat.postalCode ?? "",
    homePhone: flat.homePhone ?? "",
    mobilePhone: flat.mobilePhone ?? "",
    workPhone: flat.workPhone ?? "",
    email: flat.email ?? "",
    ...flat
  };
}
