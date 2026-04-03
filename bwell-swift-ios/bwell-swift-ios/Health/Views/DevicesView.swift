//
//  DevicesView.swift
//  bwell-swift-ios
//
//  Displays health devices using sdk.health.getDevices().
//

import SwiftUI
import BWell

struct DevicesView: View {
    @EnvironmentObject private var sdkManager: SDKManager
    @State private var devices: [BWell.Device] = []
    @State private var isLoading = false
    @State private var errorMessage: String?

    var body: some View {
        ZStack {
            if isLoading && devices.isEmpty {
                ProgressView("Loading devices...")
            } else if let error = errorMessage {
                ContentUnavailableView("Error", systemImage: "exclamationmark.triangle", description: Text(error))
            } else if devices.isEmpty {
                ContentUnavailableView("No Devices", systemImage: "sensor", description: Text("No medical devices found."))
            } else {
                List(devices, id: \.id) { device in
                    VStack(alignment: .leading, spacing: 8) {
                        Text(deviceDisplayName(device))
                            .font(.headline)

                        if let status = device.status {
                            Text(status)
                                .font(.caption)
                                .fontWeight(.medium)
                                .padding(.horizontal, 8)
                                .padding(.vertical, 3)
                                .background(deviceStatusColor(status).opacity(0.15))
                                .foregroundStyle(deviceStatusColor(status))
                                .clipShape(Capsule())
                        }

                        if let type = device.type?.text ?? device.type?.coding?.first?.display {
                            HStack(spacing: 4) {
                                Text("Type:")
                                    .font(.caption)
                                    .foregroundStyle(.secondary)
                                Text(type)
                                    .font(.caption)
                            }
                        }

                        if let manufacturer = device.manufacturer {
                            HStack(spacing: 4) {
                                Text("Manufacturer:")
                                    .font(.caption)
                                    .foregroundStyle(.secondary)
                                Text(manufacturer)
                                    .font(.caption)
                            }
                        }

                        if let model = device.modelNumber {
                            HStack(spacing: 4) {
                                Text("Model:")
                                    .font(.caption)
                                    .foregroundStyle(.secondary)
                                Text(model)
                                    .font(.caption)
                            }
                        }

                        if let serial = device.serialNumber {
                            HStack(spacing: 4) {
                                Text("Serial:")
                                    .font(.caption)
                                    .foregroundStyle(.secondary)
                                Text(serial)
                                    .font(.caption)
                            }
                        }

                        if let lotNumber = device.lotNumber {
                            HStack(spacing: 4) {
                                Text("Lot:")
                                    .font(.caption)
                                    .foregroundStyle(.secondary)
                                Text(lotNumber)
                                    .font(.caption)
                            }
                        }
                    }
                    .padding(.vertical, 4)
                }
                .listStyle(.plain)
            }
        }
        .navigationTitle("Devices")
        .toolbarColorScheme(.dark, for: .navigationBar)
        .toolbarBackgroundVisibility(.visible, for: .navigationBar)
        .toolbarBackground(.bwellPurple, for: .navigationBar)
        .refreshable {
            await loadDevices()
        }
        .task {
            guard devices.isEmpty else { return }
            await loadDevices()
        }
    }

    private func loadDevices() async {
        guard let sdk = sdkManager.sdk else { return }
        isLoading = true
        errorMessage = nil
        do {
            let request = BWell.DeviceRequest(page: 0)
            let response = try await sdk.health.getDevices(request)
            devices = response.entry?.compactMap { $0.resource } ?? []
        } catch {
            errorMessage = "Failed to load devices."
        }
        isLoading = false
    }

    private func deviceDisplayName(_ device: BWell.Device) -> String {
        if let names = device.deviceName, let first = names.first {
            return first.name ?? "Device"
        }
        return device.type?.text ?? device.type?.coding?.first?.display ?? "Device"
    }

    private func deviceStatusColor(_ status: String) -> Color {
        switch status.lowercased() {
        case "active": return .green
        case "inactive": return .orange
        case "entered-in-error": return .red
        default: return .gray
        }
    }
}
