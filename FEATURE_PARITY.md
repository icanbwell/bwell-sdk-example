# b.well SDK Feature Parity

This document compares what is implemented across each b.well SDK sample application. It is intended for engineers, product managers, and stakeholders who need a clear picture of where each platform stands today.

**Platforms covered:**
- **TypeScript React** — Web browser app (`bwell-typescript-react/`)
- **Kotlin Android** — Native Android app (`bwell-kotlin-android/`)
- **Swift iOS** — Native iOS app (`bwell-swift-ios/`)
- **React Native** — Cross-platform mobile app (`bwell-react-native/`)

---

## Quick Reference

| Feature | TypeScript React | Kotlin Android | Swift iOS | React Native |
|---|:---:|:---:|:---:|:---:|
| **Authentication** | | | | |
| Client Key | ✅ | | | ✅ |
| JWT | ✅ | ✅ | ✅ | ✅ |
| Email / Password | ✅ | | | ✅ |
| OAuth | ✅ | | | |
| **Health Data** | | | | |
| Health Summary | ✅ | ✅ | ✅ | ✅ |
| Allergies & Intolerances | ✅ | ✅ | ✅ | ✅ |
| Care Plans | ✅ | ✅ | ✅ | |
| Care Teams | | | ✅ | |
| Conditions | ✅ | ✅ | ✅ | |
| Devices | | | ✅ | |
| Diagnostic Reports | | | ✅ | |
| Document References | | | ✅ | |
| Encounters | ✅ | ✅ | ✅ | |
| Goals | | | ✅ | |
| Immunizations | ✅ | ✅ | ✅ | |
| Labs | ✅ | ✅ | ✅ | |
| Medications | ✅ | ✅ | ✅ | |
| Procedures | ✅ | ✅ | ✅ | |
| Questionnaire Responses | | | ✅ | |
| Vital Signs | ✅ | ✅ | ✅ | |
| **Features** | | | | |
| Consents | ✅ | ✅ | ✅ | |
| Data Connections | ✅ | ✅ | ✅ | |
| Provider Search | ✅ | ✅ | ✅ | |
| Health Journey / Tasks | | ✅ | ✅ | |
| Insurance / Financial Data | | ✅ | ✅ | |
| Profile | ✅ | ✅ | ✅ | |
| Account Settings | | | ✅ | |
| Device Registration | | | ✅ | |
| Lab / Vital Trend Charts | | | ✅ | |
| PROA Web View | | ✅ | | |
| Provider Detail View | | ✅ | ✅ | |
| Provider Filtering | | ✅ | ✅ | |
| Clinic Search | | ✅ | | |

---

## Detailed Feature Breakdown

The sections below describe what each feature does in plain terms, so anyone reading this document understands what is and is not available on each platform.

---

### Authentication

Authentication is the process of verifying who the user is before granting access to their health data. Different authentication methods suit different deployment contexts.

---

#### Client Key
A unique API key that identifies the application itself (not the individual user) to the b.well platform. This is typically the first step in initializing the SDK — the app registers itself before a user logs in.

| Platform | Status |
|---|---|
| TypeScript React | ✅ Implemented |
| Kotlin Android | Not implemented |
| Swift iOS | Not implemented |
| React Native | ✅ Implemented |

---

#### JWT (JSON Web Token)
A secure, time-limited token issued after a successful login. The app sends this token with every API call to prove the user is authenticated. JWT login is the most common authentication path across all platforms.

| Platform | Status |
|---|---|
| TypeScript React | ✅ Implemented |
| Kotlin Android | ✅ Implemented |
| Swift iOS | ✅ Implemented |
| React Native | ✅ Implemented |

---

#### Email / Password
Standard username and password login. The user enters their b.well credentials directly inside the app. Simple and familiar, but requires the user to have a b.well account.

| Platform | Status |
|---|---|
| TypeScript React | ✅ Implemented |
| Kotlin Android | Not implemented |
| Swift iOS | Not implemented |
| React Native | ✅ Implemented |

---

#### OAuth
A standards-based login flow where the user is redirected to an external identity provider (such as a hospital system, Google, or another trusted party) to authenticate, then returned to the app with a secure token. Enables single sign-on and federated identity scenarios.

| Platform | Status |
|---|---|
| TypeScript React | ✅ Implemented |
| Kotlin Android | Not implemented |
| Swift iOS | Not implemented |
| React Native | Not implemented |

---

### Health Data

Health data represents the clinical records pulled from a user's connected health sources. All health data in b.well follows the FHIR standard (Fast Healthcare Interoperability Resources), which is the industry-wide format for exchanging electronic health information.

---

#### Health Summary
A high-level dashboard of all available health data for the user. This is typically the first screen a user sees after logging in — it shows which categories of health data are available and acts as the entry point into the rest of their records.

| Platform | Status |
|---|---|
| TypeScript React | ✅ Implemented |
| Kotlin Android | ✅ Implemented |
| Swift iOS | ✅ Implemented |
| React Native | ✅ Implemented |

---

#### Allergies & Intolerances
A list of substances — foods, medications, environmental agents — that the user has documented allergic or adverse reactions to. Includes the reaction type, severity, and the source that recorded it.

| Platform | Status |
|---|---|
| TypeScript React | ✅ Implemented |
| Kotlin Android | ✅ Implemented |
| Swift iOS | ✅ Implemented |
| React Native | ✅ Implemented (only fully built-out health data category) |

---

#### Care Plans
Structured treatment plans created by a healthcare provider. A care plan outlines the goals, activities, and instructions for managing a specific condition — for example, a diabetes management plan with diet targets and check-in schedules.

| Platform | Status |
|---|---|
| TypeScript React | ✅ Implemented |
| Kotlin Android | ✅ Implemented |
| Swift iOS | ✅ Implemented |
| React Native | Not implemented |

---

#### Care Teams
The group of healthcare providers and support staff assigned to a user's care. May include primary care physicians, specialists, nurses, and care coordinators. Useful for giving users visibility into who is managing their health.

| Platform | Status |
|---|---|
| TypeScript React | Not implemented |
| Kotlin Android | Not implemented |
| Swift iOS | ✅ Implemented |
| React Native | Not implemented |

---

#### Conditions
Documented medical diagnoses and active health problems on record for the user — for example, Type 2 Diabetes, Hypertension, or Asthma. Sourced from connected providers and health systems.

| Platform | Status |
|---|---|
| TypeScript React | ✅ Implemented |
| Kotlin Android | ✅ Implemented |
| Swift iOS | ✅ Implemented |
| React Native | Not implemented |

---

#### Devices
Medical devices associated with the user's health record — such as pacemakers, insulin pumps, hearing aids, or connected wearables. Includes device type, manufacturer, and relevant identifiers.

| Platform | Status |
|---|---|
| TypeScript React | Not implemented |
| Kotlin Android | Not implemented |
| Swift iOS | ✅ Implemented |
| React Native | Not implemented |

---

#### Diagnostic Reports
Formal reports generated from diagnostic tests. This includes lab panels (groups of related test results), imaging summaries (e.g. X-ray or MRI findings), and pathology reports. More structured and detailed than individual lab results.

| Platform | Status |
|---|---|
| TypeScript React | Not implemented |
| Kotlin Android | Not implemented |
| Swift iOS | ✅ Implemented |
| React Native | Not implemented |

---

#### Document References
Links to clinical documents stored in connected health systems — such as after-visit summaries, discharge instructions, referral letters, and clinical notes. Users can view the document content where available.

| Platform | Status |
|---|---|
| TypeScript React | Not implemented |
| Kotlin Android | Not implemented |
| Swift iOS | ✅ Implemented |
| React Native | Not implemented |

---

#### Encounters
Records of interactions between the user and a healthcare provider. This includes in-person clinic visits, hospital stays, emergency room visits, and telehealth appointments. Each encounter includes the date, provider, location, and reason for the visit.

| Platform | Status |
|---|---|
| TypeScript React | ✅ Implemented |
| Kotlin Android | ✅ Implemented |
| Swift iOS | ✅ Implemented |
| React Native | Not implemented |

---

#### Goals
Patient or provider-defined health objectives being tracked over time — for example, "reduce blood pressure below 130/80" or "walk 10,000 steps per day." Shows target values, current status, and progress toward completion.

| Platform | Status |
|---|---|
| TypeScript React | Not implemented |
| Kotlin Android | Not implemented |
| Swift iOS | ✅ Implemented |
| React Native | Not implemented |

---

#### Immunizations
A complete record of vaccinations the user has received — including vaccine name, dose number, date administered, and administering provider. Useful for verifying vaccination history and identifying gaps.

| Platform | Status |
|---|---|
| TypeScript React | ✅ Implemented |
| Kotlin Android | ✅ Implemented |
| Swift iOS | ✅ Implemented |
| React Native | Not implemented |

---

#### Labs
Individual laboratory test results — blood tests, urine tests, metabolic panels, and other diagnostic measurements. Each result includes the test name, value, unit, reference range, and whether the result is within normal bounds.

| Platform | Status |
|---|---|
| TypeScript React | ✅ Implemented |
| Kotlin Android | ✅ Implemented (with detail view) |
| Swift iOS | ✅ Implemented (with trend charts) |
| React Native | Not implemented |

---

#### Medications
Current and historical prescriptions and over-the-counter medications associated with the user. Includes medication name, dosage, frequency, prescribing provider, and pharmacy information where available.

| Platform | Status |
|---|---|
| TypeScript React | ✅ Implemented |
| Kotlin Android | ✅ Implemented (with detail view) |
| Swift iOS | ✅ Implemented (with detail view) |
| React Native | Not implemented |

---

#### Procedures
Medical procedures the user has undergone — including surgeries, biopsies, infusions, and diagnostic procedures. Each record includes the procedure name, date, performing provider, and outcome where recorded.

| Platform | Status |
|---|---|
| TypeScript React | ✅ Implemented |
| Kotlin Android | ✅ Implemented |
| Swift iOS | ✅ Implemented |
| React Native | Not implemented |

---

#### Questionnaire Responses
Answers to structured health surveys and intake forms completed by the user — such as depression screenings (PHQ-9), pain assessments, or pre-visit intake questionnaires. Gives providers and care teams insight into a patient's self-reported health status.

| Platform | Status |
|---|---|
| TypeScript React | Not implemented |
| Kotlin Android | Not implemented |
| Swift iOS | ✅ Implemented |
| React Native | Not implemented |

---

#### Vital Signs
Clinical measurements that indicate the user's basic health status — including blood pressure, heart rate, body temperature, weight, height, BMI, respiratory rate, and blood oxygen saturation. Sourced from connected devices and provider visits.

| Platform | Status |
|---|---|
| TypeScript React | ✅ Implemented |
| Kotlin Android | ✅ Implemented (with detail view) |
| Swift iOS | ✅ Implemented (with trend charts) |
| React Native | Not implemented |

---

### Features

Features are capabilities that go beyond displaying health data — they involve user actions, integrations, and workflows.

---

#### Consents
The ability to view, record, and manage a user's consent to specific data-sharing policies. A consent record states whether the user permits or denies access to their health data for a given purpose or organization. This is a legal and regulatory requirement in many health data scenarios.

| Platform | Status | Notes |
|---|---|---|
| TypeScript React | ✅ Implemented | Full create + fetch UI with `SdkCallResult` wrapper pattern |
| Kotlin Android | ✅ Implemented | Implemented as "HealthMatch" consent with a feedback flow |
| Swift iOS | ✅ Implemented | Accessible from the Home tab |
| React Native | Not implemented | |

---

#### Data Connections
The ability for a user to link external health data sources — such as hospitals, health systems, labs, and pharmacies — to their b.well account. Once connected, records from those sources are pulled into the user's health profile automatically.

| Platform | Status |
|---|---|
| TypeScript React | ✅ Implemented |
| Kotlin Android | ✅ Implemented |
| Swift iOS | ✅ Implemented |
| React Native | Not implemented |

---

#### Provider Search
A search tool that lets users find healthcare providers — doctors, specialists, and other clinicians — within the b.well network. Users can search by name, specialty, and location.

| Platform | Status |
|---|---|
| TypeScript React | ✅ Implemented |
| Kotlin Android | ✅ Implemented |
| Swift iOS | ✅ Implemented |
| React Native | Not implemented |

---

#### Health Journey / Tasks
A guided experience that helps users track and complete health-related tasks and milestones — such as scheduling a preventive screening, completing a health assessment, or following up with a specialist. Tasks are assigned and tracked over time.

| Platform | Status |
|---|---|
| TypeScript React | Not implemented |
| Kotlin Android | ✅ Implemented |
| Swift iOS | ✅ Implemented |
| React Native | Not implemented |

---

#### Insurance / Financial Data
Access to the user's insurance plan information and health-related financial data — including plan name, coverage details, payer information, and billing records where available from connected sources.

| Platform | Status |
|---|---|
| TypeScript React | Not implemented |
| Kotlin Android | ✅ Implemented |
| Swift iOS | ✅ Implemented |
| React Native | Not implemented |

---

#### Profile
The user's personal account information — name, date of birth, contact details, and demographic data. Allows the user to view and, where supported, update their information within the app.

| Platform | Status |
|---|---|
| TypeScript React | ✅ Implemented |
| Kotlin Android | ✅ Implemented |
| Swift iOS | ✅ Implemented |
| React Native | Not implemented |

---

#### Account Settings
App-level preferences and configuration options for the user's account — such as notification preferences, privacy settings, and display options. Separate from the user's health profile.

| Platform | Status |
|---|---|
| TypeScript React | Not implemented |
| Kotlin Android | Not implemented |
| Swift iOS | ✅ Implemented |
| React Native | Not implemented |

---

#### Device Registration
The ability to register the user's physical device (e.g. a smartphone) with the b.well platform — enabling push notifications, device-specific features, and session management tied to a specific device.

| Platform | Status |
|---|---|
| TypeScript React | Not implemented |
| Kotlin Android | Not implemented |
| Swift iOS | ✅ Implemented |
| React Native | Not implemented |

---

#### Lab / Vital Trend Charts
Visual charts that display how a user's lab values or vital signs have changed over time. Helps users and care teams spot trends, track progress toward goals, and identify concerning changes at a glance.

| Platform | Status |
|---|---|
| TypeScript React | Not implemented |
| Kotlin Android | Not implemented |
| Swift iOS | ✅ Implemented (SwiftUI Charts for labs and vitals) |
| React Native | Not implemented |

---

#### PROA Web View
An embedded browser view for accessing PROA (Patient Record of Activity) — a specific integration that lets users review a web-based record of their health activity. Renders the PROA experience inside the native app without leaving it.

| Platform | Status |
|---|---|
| TypeScript React | Not implemented |
| Kotlin Android | ✅ Implemented |
| Swift iOS | Not implemented |
| React Native | Not implemented |

---

#### Provider Detail View
A full profile screen for an individual healthcare provider. Displays specialty, credentials, practice location, contact information, and affiliated organizations. Accessed by tapping a provider in search results.

| Platform | Status |
|---|---|
| TypeScript React | Not implemented |
| Kotlin Android | ✅ Implemented |
| Swift iOS | ✅ Implemented |
| React Native | Not implemented |

---

#### Provider Filtering
The ability to narrow down provider search results using filters — such as specialty, gender, distance, and availability. Helps users find the most relevant provider for their needs.

| Platform | Status |
|---|---|
| TypeScript React | Not implemented |
| Kotlin Android | ✅ Implemented |
| Swift iOS | ✅ Implemented |
| React Native | Not implemented |

---

#### Clinic Search
A dedicated search experience for finding healthcare facilities and clinics — separate from searching for individual providers. Lets users locate physical locations such as urgent care centers, imaging facilities, and specialty clinics.

| Platform | Status |
|---|---|
| TypeScript React | Not implemented |
| Kotlin Android | ✅ Implemented |
| Swift iOS | Not implemented |
| React Native | Not implemented |

---

## Platform Summaries

### TypeScript React
The most complete web implementation. Covers all core health data categories and is the reference platform for the `SdkCallResult` wrapper pattern introduced with Consents. Best starting point for new feature documentation and integration examples.

**Strengths:** Multiple authentication methods, full health data coverage, clean wrapper pattern for SDK calls.
**Gaps:** No health journey, no insurance data, no trend charts, no provider detail or filtering.

---

### Kotlin Android
The broadest feature set of the three mobile platforms. The only platform with PROA web view and clinic search. Consent is implemented as a "HealthMatch" flow with user feedback collection.

**Strengths:** Provider search with filtering and detail views, health journey, insurance data, PROA integration.
**Gaps:** Fewer authentication methods than web; some health data categories (care teams, goals, devices, diagnostic reports) not implemented.

---

### Swift iOS
The most comprehensive platform overall. The only implementation with trend charts for labs and vitals, and the only one covering care teams, goals, devices, diagnostic reports, document references, questionnaire responses, account settings, and device registration. Exercises all 65 SDK methods across 9 managers.

**Strengths:** Widest health data coverage, visual trend charts, account and device management, full provider experience.
**Gaps:** No PROA web view, no clinic search, fewer auth options documented.

---

### React Native
A minimal cross-platform MVP. Authentication and health summary are functional. Allergies & Intolerances is the only health data category fully built out — other categories are architecturally stubbed but not implemented.

**Strengths:** Cross-platform (iOS + Android from one codebase), dynamic category routing is extensible.
**Gaps:** Significantly behind all other platforms. Most health data categories, all features (consents, connections, provider search, profile, etc.) are not yet implemented.
