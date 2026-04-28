//
//  ProviderSearchView.swift
//  bwell-swift-ios
//

import SwiftUI
import BWellSDK

struct ProviderSearchView: View {
    @EnvironmentObject private var sdkManager: SDKManager
    @StateObject private var viewModel = ProviderSearchViewModel()
    @State private var selectedResult: BWell.SearchHealthResourcesResults.Result?

    var body: some View {
        Group {
            if !viewModel.hasInitiallyLoaded {
                ProgressView("Loading providers...")
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if viewModel.results.isEmpty && !viewModel.isLoading {
                emptyStateView
            } else {
                VStack(spacing: 0) {
                    if viewModel.isSearching {
                        HStack(spacing: 6) {
                            ProgressView()
                                .scaleEffect(0.7)
                            Text("Searching...")
                                .font(.caption)
                                .foregroundStyle(.secondary)
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 6)
                        .background(Color(.systemGray6))
                    }
                    resultsList
                }
            }
        }
        .navigationTitle("Find Providers")
        .navigationBarTitleDisplayMode(.large)
        .toolbarColorScheme(.dark, for: .navigationBar)
        .toolbarBackgroundVisibility(.visible, for: .navigationBar)
        .toolbarBackground(.bwellPurple, for: .navigationBar)
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Button { viewModel.showFilters = true } label: {
                    Image(systemName: "line.3.horizontal.decrease.circle")
                }
            }
        }
        .searchable(text: $viewModel.searchText, prompt: "Search providers...")
        .task {
            guard let sdk = sdkManager.sdk else { return }
            viewModel.configure(sdk: sdk)
            await viewModel.loadInitialResults()
        }
        .onSubmit(of: .search) {
            Task { await viewModel.submitSearch() }
        }
        .sheet(isPresented: $viewModel.showFilters) {
            ProviderFilterView(viewModel: viewModel)
                .presentationDetents([.medium])
                .presentationDragIndicator(.visible)
        }
        .alert("Error", isPresented: .constant(viewModel.errorMessage != nil)) {
            Button("OK") { viewModel.errorMessage = nil }
        } message: {
            if let errorMessage = viewModel.errorMessage {
                Text(errorMessage)
            }
        }
    }

    private var emptyStateView: some View {
        VStack(spacing: 16) {
            Image(systemName: "magnifyingglass")
                .font(.system(size: 48))
                .foregroundStyle(.tertiary)

            if viewModel.searchText.isEmpty {
                Text("No Providers Nearby")
                    .font(.title3)
                    .fontWeight(.semibold)
                Text("Try adjusting your filters or location")
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
            } else {
                Text("No Results for \"\(viewModel.searchText)\"")
                    .font(.title3)
                    .fontWeight(.semibold)
                Text("Tap Search to find more results from the server")
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 40)
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }

    private var resultsList: some View {
        List {
            Section {
                ForEach(viewModel.results, id: \.id) { result in
                    Button {
                        selectedResult = result
                    } label: {
                        ProviderResultRow(result: result)
                    }
                }
            } header: {
                HStack {
                    Text("\(viewModel.results.count) Provider\(viewModel.results.count == 1 ? "" : "s")")
                        .textCase(nil)

                    if viewModel.isSearching {
                        ProgressView()
                            .scaleEffect(0.7)
                    }
                }
            }
        }
        .listStyle(.plain)
        .sheet(item: $selectedResult) { result in
            NavigationStack {
                ProviderDetailView(result: result)
                    .environmentObject(sdkManager)
            }
        }
    }
}

struct ProviderResultRow: View {
    let result: BWell.SearchHealthResourcesResults.Result

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(result.content ?? "Unknown Provider")
                .font(.body)
                .fontWeight(.medium)
                .foregroundStyle(.primary)

            if let specialties = result.specialty, !specialties.isEmpty {
                Text(specialties.compactMap { $0.display }.joined(separator: ", "))
                    .font(.caption)
                    .foregroundStyle(.secondary)
                    .lineLimit(1)
            }

            if let location = result.location?.first, let address = location.address {
                HStack(spacing: 4) {
                    if let city = address.city {
                        Text(city)
                    }
                    if let state = address.state {
                        Text(", \(state)")
                    }
                    if let postalCode = address.postalCode {
                        Text(postalCode)
                    }
                }
                .font(.subheadline)
                .foregroundStyle(.secondary)

                if let distance = location.distanceInMiles {
                    HStack(spacing: 4) {
                        Image(systemName: "location.fill")
                            .font(.caption2)
                        Text(String(format: "%.1f mi", distance))
                            .font(.caption)
                    }
                    .foregroundStyle(.bwellBlue)
                }
            }

            if let type = result.type {
                Text(providerTypeLabel(type))
                    .font(.caption)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 3)
                    .background(Color.bwellPurple.opacity(0.1))
                    .foregroundStyle(.bwellPurple)
                    .clipShape(Capsule())
            }
        }
        .padding(.vertical, 4)
    }

    private func providerTypeLabel(_ type: BWell.ProviderType) -> String {
        switch type {
        case .practice: return "Practice"
        case .practitioner: return "Practitioner"
        case .insurance: return "Insurance"
        case .laboratory: return "Laboratory"
        case .pharmacy: return "Pharmacy"
        case .unknown(let value): return value.capitalized
        }
    }
}
