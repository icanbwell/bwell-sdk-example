# bwell SDK Examples

This repository contains example applications demonstrating the capabilities and usage of the b.well SDK across multiple platforms and languages. These examples serve as practical guides for developers looking to integrate b.well services into their own applications.

## Available Examples

### 📱 [Android/Kotlin Example](./bwell-kotlin-android/)
A native Android application built with Kotlin that demonstrates b.well SDK integration using Android best practices.

**Dependencies:**
- [`com.bwell:bwell-sdk-kotlin`](https://artifacts.icanbwell.com/repository/bwell-public/) from b.well's Maven repository

**Features:**
- Native Android UI with Jetpack Compose
- Authentication flow
- Health data retrieval
- Firebase messaging integration

### 🌐 [TypeScript/React Web Example](./bwell-typescript-react/)
A modern web application built with React, Redux Toolkit, and Vite that showcases b.well SDK usage in web browsers.

**Dependencies:**
- [`@icanbwell/bwell-sdk-ts`](https://www.npmjs.com/package/@icanbwell/bwell-sdk-ts) from NPM

**Features:**
- Material-UI components
- Redux state management
- Health data visualization with data grids
- Playwright E2E testing

### 📲 [React Native/Expo Example](./bwell-react-native/)
A cross-platform mobile application built with React Native and Expo that demonstrates b.well SDK integration for iOS and Android.

**Dependencies:**
- [`@icanbwell/bwell-sdk-ts`](https://www.npmjs.com/package/@icanbwell/bwell-sdk-ts) from NPM

**Features:**
- Cross-platform mobile app
- Expo development workflow
- Health summary display
- Allergy intolerances viewer

## SDK Package Information

### External Package Links

- **Android/Kotlin SDK**: Available from [b.well's Maven Repository](https://artifacts.icanbwell.com/repository/bwell-public/)
  - Package: `com.bwell:bwell-sdk-kotlin`
  - Repository URL: `https://artifacts.icanbwell.com/repository/bwell-public/`

- **TypeScript SDK**: Available from [NPM](https://www.npmjs.com/package/@icanbwell/bwell-sdk-ts)
  - Package: `@icanbwell/bwell-sdk-ts`
  - Works with React, React Native, Node.js, and other TypeScript/JavaScript environments

## Getting Started

Choose the example that matches your development environment:

1. **For Android development**: Navigate to [`./bwell-kotlin-android/`](./bwell-kotlin-android/) and follow the Android-specific setup instructions.

2. **For web development**: Navigate to [`./bwell-typescript-react/`](./bwell-typescript-react/) and follow the React web app setup instructions.

3. **For mobile app development**: Navigate to [`./bwell-react-native/`](./bwell-react-native/) and follow the React Native/Expo setup instructions.

Each example includes its own detailed README with specific setup instructions, prerequisites, and usage guidelines.

## Common Requirements

All examples require:
- Valid b.well API credentials
- Authentication tokens (JWT)
- Network connectivity for API calls

Refer to each individual example's README for platform-specific setup instructions.
