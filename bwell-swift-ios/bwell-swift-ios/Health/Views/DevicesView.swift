//
//  DevicesView.swift
//  bwell-swift-ios
//
//  Displays health devices with expandable detail rows.
//

import SwiftUI
import BWellSDK

struct DevicesView: View {
    @EnvironmentObject private var sdkManager: SDKManager
    @State private var devices: [BWell.Device] = []
    @State private var isLoading = false
    @State private var errorMessage: String?
    @State private var expandedIds: Set<String> = []

    var body: some View {
        ZStack {
            if isLoading && devices.isEmpty {
                ContentUnavailableView("Error", systemImage: "exclamationmark.triangle", description: Text(""))
                    .hidden()
                ProgressView("Loading devices...")
            } else if let error = errorMessage {
                ContentUnavailableView("Error", systemImage: "exclamationmark.triangle", description: Text(error))
            } else if devices.isEmpty {
                ContentUnavailableView("No Devices", systemImage: "sensor", description: Text("No medical devices found."))
            } else {
                List(devices, id: \.id) { device in
                    DeviceRow(
                        device: device,
                        isExpanded: expandedIds.contains(device.id ?? ""),
                        onToggle: {
                            withAnimation(.easeInOut(duration: 0.25)) {
                                let id = device.id ?? ""
                                if expandedIds.contains(id) {
                                    expandedIds.remove(id)
                                } else {
                                    expandedIds.insert(id)
                                }
                            }
                        }
                    )
                }
                .listStyle(.plain)
            }
        }
        .navigationTitle("Devices")
        .toolbarColorScheme(.dark, for: .navigationBar)

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
}

// MARK: - Device Row

private struct DeviceRow: View {
    let device: BWell.Device
    let isExpanded: Bool
    let onToggle: () -> Void

    private var displayName: String {
        if let names = device.deviceName, let first = names.first {
            return first.name ?? "Device"
        }
        return device.type?.text ?? device.type?.coding?.first?.display ?? "Device"
    }

    private var typeText: String? {
        // If name came from deviceName, show type separately
        if let names = device.deviceName, names.first?.name != nil {
            return device.type?.text ?? device.type?.coding?.first?.display
        }
        return nil
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Button(action: onToggle) {
                HStack(alignment: .center, spacing: 10) {
                    VStack(alignment: .leading, spacing: 3) {
                        Text(displayName)
                            .font(.body)
                            .fontWeight(.medium)
                            .foregroundStyle(.primary)
                            .lineLimit(2)
                            .multilineTextAlignment(.leading)

                        if let type = typeText {
                            Text(type)
                                .font(.caption2)
                                .foregroundStyle(.tertiary)
                        }
                    }

                    Spacer()

                    if let status = device.status {
                        Text(status)
                            .font(.caption2)
                            .fontWeight(.medium)
                            .padding(.horizontal, 8)
                            .padding(.vertical, 3)
                            .background((FHIRDeviceStatus(rawStatus: status)?.color ?? .gray).opacity(0.15))
                            .foregroundStyle((FHIRDeviceStatus(rawStatus: status)?.color ?? .gray))
                            .clipShape(Capsule())
                    }

                    Image(systemName: isExpanded ? "chevron.down" : "chevron.right")
                        .font(.caption2)
                        .fontWeight(.semibold)
                        .foregroundStyle(.tertiary)
                }
            }
            .buttonStyle(.plain)

            if isExpanded {
                Divider().padding(.top, 8)
                DeviceDetail(device: device)
                    .padding(.top, 6)
                    .padding(.leading, 20)
                    .transition(.opacity.combined(with: .move(edge: .top)))
            }
        }
        .padding(.vertical, 6)
    }

}

// MARK: - Device Detail (Expanded)

private struct DeviceDetail: View {
    let device: BWell.Device

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            if let type = device.type?.text ?? device.type?.coding?.first?.display {
                detailRow("Type", type)
            }

            if let manufacturer = device.manufacturer {
                detailRow("Manufacturer", manufacturer)
            }

            if let model = device.modelNumber {
                detailRow("Model", model)
            }

            if let serial = device.serialNumber {
                detailRow("Serial", serial)
            }

            if let lotNumber = device.lotNumber {
                detailRow("Lot Number", lotNumber)
            }

            if let expirationDate = device.expirationDate {
                detailRow("Expires", expirationDate.dateFormatter())
            }

            if let manufactureDate = device.manufactureDate {
                detailRow("Manufactured", manufactureDate.dateFormatter())
            }

            // Device names (additional names beyond the primary)
            if let names = device.deviceName, names.count > 1 {
                Divider().padding(.vertical, 4)
                Text("Names")
                    .font(.caption)
                    .fontWeight(.semibold)
                    .foregroundStyle(.secondary)
                ForEach(Array(names.dropFirst()).indices, id: \.self) { i in
                    if let nameText = Array(names.dropFirst())[i].name {
                        detailRow("Name", nameText)
                    }
                }
            }

        }
    }

    @ViewBuilder
    private func detailRow(_ label: String, _ value: String?) -> some View {
        if let value, !value.isEmpty {
            HStack(alignment: .top, spacing: 6) {
                Text(label)
                    .font(.caption)
                    .foregroundStyle(.secondary)
                    .frame(width: 80, alignment: .trailing)
                Text(value)
                    .font(.caption)
                    .foregroundStyle(.primary)
                    .fixedSize(horizontal: false, vertical: true)
            }
        }
    }
}
