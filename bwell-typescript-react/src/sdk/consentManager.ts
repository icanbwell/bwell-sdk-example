import { getSdk } from "@/sdk/bWellSdk";
import { CreateConsentRequest, GetConsentsRequest } from "@icanbwell/bwell-sdk-ts";

/**
 * Check if SDK is authenticated by trying to get user profile
 */
async function checkAuthentication() {
  const sdk = getSdk();
  if (!sdk) throw new Error("SDK not initialized - please go to Initialize page first");

  try {
    const profile = await sdk.user.getProfile();
    if (profile.hasError()) {
      throw new Error("Not authenticated - please log in on the Initialize page first");
    }
    console.log('[ConsentTest] ✓ User authenticated:', profile.data?.id);
    return true;
  } catch (error: any) {
    throw new Error(`Authentication check failed: ${error.message}. Please go to Initialize page and log in.`);
  }
}

/**
 * Creates a TOS consent for the authenticated user
 */
export async function createTOSConsent() {
  await checkAuthentication();
  
  const sdk = getSdk();
  if (!sdk) throw new Error("SDK not initialized");

  console.log('[ConsentTest] Creating TOS consent...');
  
  const consent = await sdk.user.createConsent(
    new CreateConsentRequest({
      status: 'ACTIVE',
      provision: 'PERMIT',
      category: 'TOS',
    })
  );

  // Using Citizen's exact pattern: .failure() instead of .success()
  if (consent.failure()) {
    console.warn(`[ConsentTest] Failed to create consent: ${consent.error().message}`);
    return null;
  }
  
  const consentData = consent.data();
  console.log('[ConsentTest] ✓ Consent created successfully');
  console.log('[ConsentTest] Consent ID:', consentData?.id);
  console.log('[ConsentTest] Consent Status:', consentData?.status);
  console.log('[ConsentTest] Consent Category:', consentData?.category);
  console.log('[ConsentTest] Full consent:', JSON.stringify(consentData, null, 2));
  
  return consentData ?? null;
}

/**
 * Creates a DATA_SHARING consent for a specific patient
 * This allows the user to consent to sharing their health data with third parties
 * 
 * NOTE: The SDK's CreateConsentRequest doesn't support specifying a patient ID.
 * Consents are always created for the authenticated user. To create a consent for
 * patient c67bb727-248b-5872-a7be-291d9c6ae252, you need to authenticate as that patient
 * or use a direct GraphQL mutation with appropriate permissions.
 */
export async function createDataSharingConsent() {
  await checkAuthentication();
  
  const sdk = getSdk();
  if (!sdk) throw new Error("SDK not initialized");

  // Target patient ID (consent will be for authenticated user, not this patient)
  const targetPatientId = 'c67bb727-248b-5872-a7be-291d9c6ae252';
  
  console.log('[ConsentTest] Creating DATA_SHARING consent...');
  console.warn('[ConsentTest] ⚠ Note: Consent will be created for authenticated user, not patient:', targetPatientId);
  console.warn('[ConsentTest] ⚠ To create for specific patient, authenticate as that patient first');
  
  const result = await sdk.user.createConsent(
    new CreateConsentRequest({
      status: 'ACTIVE',
      provision: 'PERMIT',
      category: 'DATA_SHARING',
    })
  );

  if (result.success()) {
    const consentData = result.data();
    console.log('[ConsentTest] ✓ DATA_SHARING consent created successfully');
    console.log('[ConsentTest] Consent ID:', consentData.id);
    console.log('[ConsentTest] Consent Status:', consentData.status);
    console.log('[ConsentTest] Consent Category:', consentData.category);
    console.log('[ConsentTest] Patient:', consentData.patient);
    console.log('[ConsentTest] Full consent:', JSON.stringify(consentData, null, 2));
    return consentData;
  } else {
    console.error('[ConsentTest] ✗ Failed to create DATA_SHARING consent:', result.error());
    throw new Error(`Failed to create DATA_SHARING consent: ${result.error()?.message}`);
  }
}

/**
 * Gets all consents (no filter)
 */
export async function getAllConsents() {
  await checkAuthentication();
  
  const sdk = getSdk();
  if (!sdk) throw new Error("SDK not initialized");

  console.log('[ConsentTest] Getting ALL consents (no filter)...');
  
  const result = await sdk.user.getConsents();

  if (result.hasError()) {
    console.error('[ConsentTest] ✗ Failed to get consents:', result.error);
    throw new Error(`Failed to get consents: ${result.error?.message}`);
  }

  const bundle = result.data;
  if (!bundle) {
    console.warn('[ConsentTest] ⚠ No data returned');
    throw new Error('No consent data returned');
  }

  // IMPORTANT: The GraphQL query returns a plain array, not a FHIR Bundle!
  // So bundle is actually Consent[] not ConsentBundle with entry property
  const consentsArray = Array.isArray(bundle) ? bundle : (bundle as any).entry || [];
  const count = consentsArray.length;
  
  console.log('[ConsentTest] ✓ Retrieved consents');
  console.log('[ConsentTest] Total count:', count);
  console.log('[ConsentTest] Full response:', JSON.stringify(bundle, null, 2));
  
  if (consentsArray.length > 0) {
    consentsArray.forEach((consent: any, index: number) => {
      console.log(`[ConsentTest] Consent ${index}:`, {
        id: consent.id,
        status: consent.status,
        category: consent.category
      });
    });
  }
  
  return bundle;
}

/**
 * Gets consents filtered by TOS category
 */
export async function getTOSConsents() {
  await checkAuthentication();
  
  const sdk = getSdk();
  if (!sdk) throw new Error("SDK not initialized");

  console.log('[ConsentTest] Getting TOS consents (filtered)...');
  
  const result = await sdk.user.getConsents(
    new GetConsentsRequest({ category: 'TOS' })
  );

  if (result.hasError()) {
    console.error('[ConsentTest] ✗ Failed to get TOS consents:', result.error);
    throw new Error(`Failed to get TOS consents: ${result.error?.message}`);
  }

  const bundle = result.data;
  if (!bundle) {
    console.warn('[ConsentTest] ⚠ No data returned');
    throw new Error('No consent data returned');
  }

  // IMPORTANT: The GraphQL query returns a plain array, not a FHIR Bundle!
  const consentsArray = Array.isArray(bundle) ? bundle : (bundle as any).entry || [];
  const count = consentsArray.length;
  
  console.log('[ConsentTest] ✓ Retrieved TOS consents');
  console.log('[ConsentTest] TOS consent count:', count);
  console.log('[ConsentTest] Full response:', JSON.stringify(bundle, null, 2));
  
  if (consentsArray.length > 0) {
    const firstConsent = consentsArray[0];
    console.log('[ConsentTest] First TOS consent:', {
      id: firstConsent.id,
      status: firstConsent.status,
      provision: firstConsent.provision?.type
    });
  } else {
    console.warn('[ConsentTest] ⚠ No TOS consents found');
  }
  
  return bundle;
}

/**
 * Get consents using Citizen's EXACT code pattern
 * This matches what they're currently using with beta SDK 2.0.0-beta-rc.1764971887
 */
export async function getConsentsLikeCitizen(): Promise<any> {
  const sdk = getSdk();
  
  if (!sdk) {
    console.warn('[Bwell] SDK not initialized');
    return null;
  }
  
  const consents = await sdk.user.getConsents(new GetConsentsRequest({category: 'TOS'}))
  
  if (consents.error) {
    console.warn(`[Bwell] Failed to get consents: ${consents.error.message}`);
    return null;
  }
  
  console.log('[ConsentTest] Citizen pattern result (beta SDK):', consents.data);
  console.log('[ConsentTest] Entry array:', consents.data?.entry);
  console.log('[ConsentTest] Entry length:', consents.data?.entry?.length);
  
  return consents.data ?? null;
}

/**
 * Full test: Create consent, then retrieve it with and without filter
 */
export async function testConsentFlow() {
  console.log('\n========== CONSENT FLOW TEST ==========\n');
  
  try {
    // Step 1: Create TOS consent
    console.log('STEP 1: Creating TOS consent...\n');
    const createdConsent = await createTOSConsent();
    
    // Step 2: Wait a moment for indexing
    console.log('\nSTEP 2: Waiting 2 seconds for indexing...\n');
    await new Promise(resolve => setTimeout(resolve, 2000));
    
    // Step 3: Get all consents (no filter)
    console.log('STEP 3: Retrieving ALL consents...\n');
    const allConsents = await getAllConsents();
    
    // Step 4: Get TOS consents (filtered)
    console.log('\nSTEP 4: Retrieving TOS consents (filtered)...\n');
    const tosConsents = await getTOSConsents();
    
    // Summary
    console.log('\n========== TEST SUMMARY ==========');
    console.log('✓ Consent created:', createdConsent.id);
    const allCount = Array.isArray(allConsents) ? allConsents.length : 0;
    const tosCount = Array.isArray(tosConsents) ? tosConsents.length : 0;
    console.log('✓ All consents count:', allCount);
    console.log('✓ TOS consents count:', tosCount);
    
    if (tosCount > 0) {
      console.log('✓ SUCCESS: TOS consent found via filtered search!');
    } else {
      console.warn('⚠ WARNING: TOS consent NOT found via filtered search (but might be in all consents)');
    }
    console.log('==================================\n');
    
    return {
      created: createdConsent,
      allConsents,
      tosConsents
    };
  } catch (error) {
    console.error('\n========== TEST FAILED ==========');
    console.error('Error:', error);
    console.error('==================================\n');
    throw error;
  }
}
