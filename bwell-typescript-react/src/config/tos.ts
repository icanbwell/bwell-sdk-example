import { CUSTOMER_NAME, DISCONNECT_METHOD } from "@/config/app";

// Text before and after the Privacy link, so the link can be rendered as a real anchor.
export const TOS_HEADING = "Secure Data Intermediary";

export const TOS_BEFORE_LINK = `${CUSTOMER_NAME} partners with b.well Connected Health, Inc., a trusted health data intermediary, to securely connect your health information. When you authorize data connections, you consent to b.well accessing your health records using your credentials and providing them to ${CUSTOMER_NAME} for purposes consistent with our Terms and Privacy Policy. You also authorize ${CUSTOMER_NAME} to share information from your account with b.well as needed to maintain these connections. When connecting to patient portals, you confirm you are using your own credentials. You can disconnect any source or delete data from specific sources at any time by ${DISCONNECT_METHOD}. Disconnecting stops future data collection but does not affect information already provided to ${CUSTOMER_NAME}, though you can separately request deletion of such data. Learn more about how b.well uses your data in its `;

export const TOS_LINK_TEXT = "Privacy and Security Statement";

export const TOS_AFTER_LINK = `. ${CUSTOMER_NAME} will not use or sell your health data for marketing purposes without your separate, explicit consent.`;
