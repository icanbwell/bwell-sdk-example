//
//  HealthDataListView.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 14/11/25.
//

import Foundation
import SwiftUI

struct HealthDataRowContent {
    let title: String?
    let date: String?
    let value: String?

    init(title: String?, date: String? = nil, value: String? = nil) {
        self.title = title
        self.date = date
        self.value = value
    }
}

struct HealthDataGroupListView<Group, ID: Hashable>: View {
    @EnvironmentObject private var viewModel: HealthSummaryViewModel
    @EnvironmentObject private var router: NavigationRouter

    let groups: [Group]
    let id: KeyPath<Group, ID>
    let fetch: () async -> Void
    let rowContent: (Group) -> HealthDataRowContent
    let onSelect: (Group) -> Void

    var body: some View {
        ZStack {
            if viewModel.isLoading && groups.isEmpty {
                ProgressView("Loading data...")
            } else {
                List(groups, id: id) { group in
                    Button {
                        onSelect(group)
                    } label: {
                        let content = rowContent(group)
                        RowView(content: content)
                    }
                }
                .listStyle(.plain)
                .onAppear {
                    clearHealthDataDetails()
                }
            }
        }
        .toolbarColorScheme(.dark, for: .navigationBar)
        .toolbarBackgroundVisibility(.visible, for: .navigationBar)
        .toolbarBackground(.bwellPurple, for: .navigationBar)
        .task {
            if groups.isEmpty {
                await fetch()
            }
        }
    }

    private func clearHealthDataDetails() {
        viewModel.allergyIntolerances = []
        viewModel.carePlans = []
        viewModel.conditions = []
        viewModel.immunizations = []
        viewModel.encounters = []
        viewModel.labs = []
        viewModel.medications = []
        viewModel.procedures = []
        viewModel.vitalSigns = []
    }
}

// MARK: - Group Items View
struct HealthDataGroupItemsView<Entry, ID: Hashable, Detail: View>: View {
    @EnvironmentObject private var viewModel: HealthSummaryViewModel
    @State private var selectedEntry: Entry?
    @State private var showSheet = false

    var entry: [Entry]
    let id: KeyPath<Entry, ID>
    let rowContent: (Entry) -> HealthDataRowContent
    let fetch: () async -> Void
    let detailView: (Entry) -> Detail

    var body: some View {
        ZStack {
            if viewModel.isLoading && entry.isEmpty {
                ProgressView("Loading data...")
            } else {
                List(entry, id: id) { entry in
                    Button {
                        selectedEntry = entry
                        showSheet = true
                    } label: {
                        HStack {
                            let content = rowContent(entry)
                            RowView(content: content)
                        }
                    }
                }.listStyle(.plain)
            }
        }
        .toolbarColorScheme(.dark, for: .navigationBar)
        .toolbarBackgroundVisibility(.visible, for: .navigationBar)
        .toolbarBackground(.bwellPurple, for: .navigationBar)
        .task {
            if self.entry.isEmpty {
                await fetch()
            }
        }
        .sheet(isPresented: $showSheet) {
            if let selectedEntry = selectedEntry {
                detailView(selectedEntry)
            } else {
                VStack(spacing: 16) {
                    Image(systemName: "doc.text")
                        .font(.largeTitle)
                        .foregroundColor(.secondary)
                    Text("No Details Available")
                        .font(.headline)
                    Text("This record doesn't have additional information to display")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                        .multilineTextAlignment(.center)
                }
                .padding()
                .presentationDetents([.medium])
            }
        }
    }
}

// MARK: - Row View
private struct RowView: View {
    let content: HealthDataRowContent
    var body: some View {
        HStack(alignment: .center, spacing: 12) {
            Text(content.title?.capitalizingFirstLetter() ?? "Unknown title")

            Spacer()

            if let date = content.date {
                Text(date.dateFormatter())
                    .font(.subheadline)
                    .foregroundStyle(.gray)
            }

            if let value = content.value {
                Text(value)
                    .font(.subheadline)
                    .foregroundStyle(.primary)
            }

            Image(systemName: "chevron.right")
                .font(.caption)
                .frame(width: 24)
                .foregroundStyle(.gray)
        }
    }
}
