// The user's agreement to Smart Connect is collected before identity verification, but the
// consent is only created once verification succeeds. The agreement is remembered in
// sessionStorage so it survives moving between pages (and a full-page redirect if the CLEAR
// popup is blocked) until the user is verified.
const KEY = "bwell-sample-smart-connect-agreed";

export const markSmartConnectAgreed = () => sessionStorage.setItem(KEY, "1");
export const hasAgreedToSmartConnect = () => sessionStorage.getItem(KEY) === "1";
export const clearSmartConnectAgreement = () => sessionStorage.removeItem(KEY);
