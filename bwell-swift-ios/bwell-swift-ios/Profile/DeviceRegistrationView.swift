//
//  DeviceRegistrationView.swift
//  bwell-swift-ios
//
//  Push notification device registration using sdk.device.registerDevice()
//  and sdk.device.deregisterDevice().
//

import SwiftUI
import BWellSDK
import UserNotifications

// Workaround: DeregisterDeviceRequest has no public init in the SDK.
// The struct contains a single public var (deviceToken: String), so we can
// safely create it by interpreting a String as the struct's memory layout.
private func makeDeregisterRequest(deviceToken: String) -> BWell.DeregisterDeviceRequest {
    withUnsafePointer(to: deviceToken) { ptr in
        ptr.withMemoryRebound(to: BWell.DeregisterDeviceRequest.self, capacity: 1) { $0.pointee }
    }
}

struct DeviceRegistrationView: View {
    @EnvironmentObject private var sdkManager: SDKManager
    @State private var isRegistered = false
    @State private var isProcessing = false
    @State private var deviceToken: String?
    @State private var permissionStatus: UNAuthorizationStatus = .notDetermined
    @State private var statusMessage: String?
    @State private var errorMessage: String?

    var body: some View {
        List {
            Section {
                VStack(alignment: .leading, spacing: 12) {
                    HStack(spacing: 12) {
                        Image(systemName: isRegistered ? "bell.badge.fill" : "bell.slash")
                            .font(.title)
                            .foregroundStyle(isRegistered ? .bwellPurple : .secondary)

                        VStack(alignment: .leading, spacing: 2) {
                            Text(isRegistered ? "Notifications Enabled" : "Notifications Disabled")
                                .font(.headline)
                            Text(isRegistered
                                 ? "You'll receive health updates and reminders."
                                 : "Enable to receive health updates and reminders.")
                                .font(.caption)
                                .foregroundStyle(.secondary)
                        }
                    }

                    Toggle("Push Notifications", isOn: Binding(
                        get: { isRegistered },
                        set: { newValue in
                            Task {
                                if newValue {
                                    await registerDevice()
                                } else {
                                    await deregisterDevice()
                                }
                            }
                        }
                    ))
                    .disabled(isProcessing)
                    .tint(.bwellPurple)
                }
            } header: {
                Text("Notification Settings")
            } footer: {
                if permissionStatus == .denied {
                    Text("Notifications are blocked in system settings. Open Settings > Notifications to enable them.")
                        .foregroundStyle(.orange)
                }
            }

            if let status = statusMessage {
                Section("Status") {
                    HStack {
                        Image(systemName: "checkmark.circle.fill")
                            .foregroundStyle(.green)
                        Text(status)
                            .font(.subheadline)
                    }
                }
            }

            if let error = errorMessage {
                Section {
                    HStack {
                        Image(systemName: "exclamationmark.triangle.fill")
                            .foregroundStyle(.orange)
                        Text(error)
                            .font(.subheadline)
                            .foregroundStyle(.red)
                    }
                }
            }

            Section {
                VStack(alignment: .leading, spacing: 8) {
                    Text("About Push Notifications")
                        .font(.subheadline)
                        .fontWeight(.semibold)
                    Text("Push notifications allow the app to alert you about important health updates, appointment reminders, and new health insights. Your device token is securely registered with our servers.")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }
            }
        }
        .listStyle(.insetGrouped)
        .navigationTitle("Push Notifications")
        .navigationBarTitleDisplayMode(.inline)
        .toolbarColorScheme(.dark, for: .navigationBar)
        .toolbarBackgroundVisibility(.visible, for: .navigationBar)
        .toolbarBackground(.bwellPurple, for: .navigationBar)
        .task {
            await checkNotificationStatus()
        }
    }

    private func checkNotificationStatus() async {
        let settings = await UNUserNotificationCenter.current().notificationSettings()
        permissionStatus = settings.authorizationStatus
    }

    private func registerDevice() async {
        guard let sdk = sdkManager.sdk else { return }
        isProcessing = true
        errorMessage = nil
        statusMessage = nil

        // Request notification permission
        do {
            let granted = try await UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .badge, .sound])
            guard granted else {
                errorMessage = "Notification permission was denied."
                isProcessing = false
                return
            }
        } catch {
            errorMessage = "Failed to request notification permission."
            isProcessing = false
            return
        }

        // For demo purposes, use a placeholder token.
        // In production, obtain the real device token from UIApplication.shared.registerForRemoteNotifications()
        // and the AppDelegate didRegisterForRemoteNotificationsWithDeviceToken callback.
        let token = deviceToken ?? "demo-device-token-\(UUID().uuidString)"

        do {
            let request = BWell.RegisterDeviceRequest(
                deviceToken: token,
                platformName: .ios,
                applicationName: "bwell-swift-ios"
            )
            _ = try await sdk.device.registerDevice(request)
            isRegistered = true
            deviceToken = token
            statusMessage = "Device registered successfully."
        } catch {
            errorMessage = "Registration failed: \(error.localizedDescription)"
        }
        isProcessing = false
    }

    private func deregisterDevice() async {
        guard let sdk = sdkManager.sdk else { return }
        guard let token = deviceToken else {
            isRegistered = false
            return
        }
        isProcessing = true
        errorMessage = nil
        statusMessage = nil

        do {
            let request = makeDeregisterRequest(deviceToken: token)
            _ = try await sdk.device.deregisterDevice(request)
            isRegistered = false
            statusMessage = "Device deregistered successfully."
        } catch {
            errorMessage = "Deregistration failed: \(error.localizedDescription)"
        }
        isProcessing = false
    }
}
