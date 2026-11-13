const DEFAULT_IDENTITY_GATEWAY_URL = "https://api-gateway.prod.icanbwell.com/identity";
const FETCH_TIMEOUT_MS = 10000;

export type DescopeClientConfig = {
  projectId: string;
  authDomain: string;
  tenantId: string;
  flowId: string;
};

type ClientRecordResponse = {
  identityProvider?: {
    name?: string;
    projectId?: string;
    providerConfig?: {
      tenantId?: string;
      authDomain?: string;
      flows?: { id?: string; name?: string }[];
    };
  };
};

// Client keys are base64 JSON containing the environment. Old style keys are not JSON.
const decodeEnv = (clientKey: string): string | undefined => {
  try {
    const decoded = JSON.parse(atob(clientKey)) as { env?: string };
    return decoded.env;
  } catch {
    return undefined;
  }
};

const identityGatewayUrl = (clientKey: string): string => {
  const env = decodeEnv(clientKey);
  return env ? DEFAULT_IDENTITY_GATEWAY_URL.replace(".prod.", `.${env}.`) : DEFAULT_IDENTITY_GATEWAY_URL;
};

/**
 * Reads the client's Descope identity provider settings from the identity gateway
 * bootstrap record, the same source the b.well web app uses. Returns undefined when
 * the client does not use Descope.
 */
export const fetchDescopeClientConfig = async (clientKey: string): Promise<DescopeClientConfig | undefined> => {
  const response = await fetch(`${identityGatewayUrl(clientKey)}/admin/bootstrap`, {
    headers: { ClientKey: clientKey },
    signal: AbortSignal.timeout(FETCH_TIMEOUT_MS),
  });
  if (!response.ok) {
    throw new Error(`Client configuration request failed with status ${response.status}.`);
  }
  const { identityProvider } = (await response.json()) as ClientRecordResponse;
  if (identityProvider?.name !== "descope" || !identityProvider.projectId) {
    return undefined;
  }
  const flows = identityProvider.providerConfig?.flows ?? [];
  return {
    projectId: identityProvider.projectId,
    authDomain: identityProvider.providerConfig?.authDomain ?? "",
    tenantId: identityProvider.providerConfig?.tenantId ?? "",
    flowId: flows[0]?.id ?? "",
  };
};
