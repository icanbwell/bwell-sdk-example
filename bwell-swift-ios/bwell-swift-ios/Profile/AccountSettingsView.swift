//
//  AccountSettingsView.swift
//  bwell-swift-ios
//
//  Account settings: data export (createDataExportDirectDownloadURL) and
//  account deletion (sdk.user.delete).
//

import SwiftUI
import BWell

struct AccountSettingsView: View {
    @EnvironmentObject private var sdkManager: SDKManager
    @State private var isExporting = false
    @State private var isDeleting = false
    @State private var exportURL: String?
    @State private var showDeleteConfirmation = false
    @State private var showExportSheet = false
    @State private var exportId = ""
    @State private var exportPassword = ""
    @State private var errorMessage: String?

    var body: some View {
        List {
            // MARK: - Data Export
            Section {
                Button {
                    showExportSheet = true
                } label: {
                    HStack(spacing: 12) {
                        Image(systemName: "arrow.down.doc")
                            .font(.title3)
                            .foregroundStyle(.bwellBlue)
                            .frame(width: 32)
                        VStack(alignment: .leading, spacing: 2) {
                            Text("Export My Data")
                                .font(.body)
                                .foregroundStyle(.primary)
                            Text("Download a copy of your health data")
                                .font(.caption)
                                .foregroundStyle(.secondary)
                        }
                    }
                }
            } header: {
                Text("Data Management")
            }

            // MARK: - Export Result
            if let url = exportURL {
                Section("Export Ready") {
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Your data export is ready.")
                            .font(.subheadline)

                        Button {
                            if let downloadURL = URL(string: url) {
                                UIApplication.shared.open(downloadURL)
                            }
                        } label: {
                            HStack {
                                Image(systemName: "safari")
                                Text("Open Download Link")
                            }
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 8)
                        }
                        .buttonStyle(.borderedProminent)
                        .tint(.bwellBlue)
                    }
                }
            }

            // MARK: - Account Deletion
            Section {
                Button(role: .destructive) {
                    showDeleteConfirmation = true
                } label: {
                    HStack(spacing: 12) {
                        Image(systemName: "trash")
                            .font(.title3)
                            .frame(width: 32)
                        VStack(alignment: .leading, spacing: 2) {
                            Text("Delete Account")
                                .font(.body)
                            Text("Permanently remove your account and data")
                                .font(.caption)
                                .foregroundStyle(.secondary)
                        }
                    }
                }
            } header: {
                Text("Danger Zone")
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
        }
        .listStyle(.insetGrouped)
        .navigationTitle("Account Settings")
        .navigationBarTitleDisplayMode(.inline)
        .toolbarColorScheme(.dark, for: .navigationBar)
        .toolbarBackgroundVisibility(.visible, for: .navigationBar)
        .toolbarBackground(.bwellPurple, for: .navigationBar)
        .alert("Delete Account", isPresented: $showDeleteConfirmation) {
            Button("Cancel", role: .cancel) { }
            Button("Delete", role: .destructive) {
                Task { await deleteAccount() }
            }
        } message: {
            Text("This will permanently delete your account and all associated data. This action cannot be undone.")
        }
        .sheet(isPresented: $showExportSheet) {
            NavigationStack {
                Form {
                    Section("Export Details") {
                        TextField("Export ID", text: $exportId)
                            .autocapitalization(.none)
                        SecureField("Password", text: $exportPassword)
                    }

                    Section {
                        Button {
                            showExportSheet = false
                            Task { await exportData() }
                        } label: {
                            if isExporting {
                                ProgressView()
                                    .frame(maxWidth: .infinity)
                            } else {
                                Text("Export")
                                    .frame(maxWidth: .infinity)
                            }
                        }
                        .disabled(exportId.isEmpty || exportPassword.isEmpty || isExporting)
                    }
                }
                .navigationTitle("Export Data")
                .navigationBarTitleDisplayMode(.inline)
                .toolbar {
                    ToolbarItem(placement: .cancellationAction) {
                        Button("Cancel") { showExportSheet = false }
                    }
                }
            }
            .presentationDetents([.medium])
        }
    }

    private func exportData() async {
        guard let sdk = sdkManager.sdk else { return }
        isExporting = true
        errorMessage = nil
        do {
            // CreateDataExportDirectDownloadURLRequest lacks a public init — use Codable workaround
            let jsonStr = """
            {"exportId":"\(exportId)","password":"\(exportPassword)"}
            """
            let request = try JSONDecoder().decode(
                BWell.CreateDataExportDirectDownloadURLRequest.self,
                from: jsonStr.data(using: .utf8)!
            )
            let url = try await sdk.user.createDataExportDirectDownloadURL(request)
            exportURL = url
        } catch {
            errorMessage = "Export failed: \(error.localizedDescription)"
        }
        isExporting = false
    }

    private func deleteAccount() async {
        guard let sdk = sdkManager.sdk else { return }
        isDeleting = true
        errorMessage = nil
        do {
            _ = try await sdk.user.delete()
            sdkManager.logout()
        } catch {
            errorMessage = "Delete failed: \(error.localizedDescription)"
        }
        isDeleting = false
    }
}
