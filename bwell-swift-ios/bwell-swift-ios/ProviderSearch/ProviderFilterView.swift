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

                    Toggle("Include Inactive", isOn: $viewModel.includeInactive)
                        .tint(.bwellPurple)
                }

                Section(header: Text("Gender")) {
                    Picker("Gender", selection: $viewModel.genderFilter) {
                        Text("Any").tag(ProviderSearchViewModel.GenderFilter.any)
                        Text("Male").tag(ProviderSearchViewModel.GenderFilter.male)
                        Text("Female").tag(ProviderSearchViewModel.GenderFilter.female)
                    }
                    .pickerStyle(.segmented)
                }

                Section(header: Text("Patient Acceptance")) {
                    Picker("Accepting", selection: $viewModel.patientAcceptance) {
                        Text("Any").tag(ProviderSearchViewModel.PatientAcceptanceFilter.any)
                        Text("New Patients").tag(ProviderSearchViewModel.PatientAcceptanceFilter.newPatients)
                        Text("Existing").tag(ProviderSearchViewModel.PatientAcceptanceFilter.existingPatients)
                    }
                    .pickerStyle(.segmented)
                }

                Section(header: Text("Specialty (comma separated)")) {
                    TextField("e.g. 261QP2300X", text: $viewModel.specialtyFilter)
                        .textInputAutocapitalization(.never)
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

                            HStack {
                                Text("Radius:")
                                    .font(.subheadline)
                                    .foregroundColor(.secondary)
                                Spacer()
                                TextField("miles", text: $viewModel.radiusText)
                                    .keyboardType(.decimalPad)
                                    .multilineTextAlignment(.trailing)
                                    .frame(width: 80)
                                Text("mi")
                                    .font(.subheadline)
                                    .foregroundColor(.secondary)
                            }
                        }
                    }
                }

                Section(header: Text("Filter Values")) {
                    Toggle("Request Specialty Values", isOn: $viewModel.requestSpecialtyFilterValues)
                        .tint(.bwellPurple)
                    Toggle("Request Communication Values", isOn: $viewModel.requestCommunicationFilterValues)
                        .tint(.bwellPurple)
                    Toggle("Request Insurance Plan Values", isOn: $viewModel.requestInsurancePlanFilterValues)
                        .tint(.bwellPurple)
                }

                Section {
                    Button(action: {
                        dismiss()
                        Task { await viewModel.loadInitialResults() }
                    }) {
                        HStack {
                            Spacer()
                            Text("Apply & Search")
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
