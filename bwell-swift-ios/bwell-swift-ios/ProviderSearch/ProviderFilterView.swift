//
//  ProviderFilterView.swift
//  bwell-swift-ios
//

import SwiftUI

struct ProviderFilterView: View {
    @ObservedObject var viewModel: ProviderSearchViewModel
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationView {
            Form {
                Section(header: Text("Organization Type")) {
                    Picker("Type", selection: $viewModel.organizationType) {
                        ForEach(ProviderSearchViewModel.OrganizationType.allCases, id: \.self) { type in
                            Text(type.rawValue).tag(type)
                        }
                    }
                    .pickerStyle(.segmented)
                }

                Section(header: Text("Provider Options")) {
                    Toggle("PROA Only", isOn: $viewModel.includeProaOnly)
                        .tint(.bwellPurple)
                }

                Section(header: Text("Location")) {
                    Toggle("Use Location", isOn: $viewModel.useLocation)
                        .tint(.bwellPurple)

                    if viewModel.useLocation {
                        VStack(alignment: .leading, spacing: 8) {
                            HStack {
                                Text("Latitude:")
                                    .font(.subheadline)
                                    .foregroundColor(.secondary)
                                Spacer()
                                Text(String(format: "%.6f", viewModel.latitude))
                                    .font(.subheadline)
                            }

                            HStack {
                                Text("Longitude:")
                                    .font(.subheadline)
                                    .foregroundColor(.secondary)
                                Spacer()
                                Text(String(format: "%.6f", viewModel.longitude))
                                    .font(.subheadline)
                            }
                        }
                    }
                }

                Section {
                    Button(action: {
                        dismiss()
                    }) {
                        HStack {
                            Spacer()
                            Text("Apply")
                                .fontWeight(.semibold)
                            Spacer()
                        }
                    }
                }
            }
            .navigationTitle("Filters")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Done") {
                        dismiss()
                    }
                }
            }
        }
    }
}
