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

struct HealthDataGroupListView<Group>: View {
    @EnvironmentObject private var viewModel: HealthSummaryViewModel
    @EnvironmentObject private var router: NavigationRouter

    let groups: [Group]
    let fetch: () async -> Void
    let rowContent: (Group) -> HealthDataRowContent
    let onSelect: (Group) -> Void

    var body: some View {
        ZStack {
            if viewModel.isLoading && groups.isEmpty {
                ProgressView("Loading data...")
            } else {
                List(groups.indices, id: \.self) { index in
                    let group = groups[index]
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
struct HealthDataGroupItemsView<Entry, Detail: View>: View {
    @EnvironmentObject private var viewModel: HealthSummaryViewModel
    @State private var selectedEntryIndex: Int?

    var entry: [Entry]
    let rowContent: (Entry) -> HealthDataRowContent
    let fetch: () async -> Void
    let detailView: (Entry) -> Detail

    var body: some View {
        ZStack {
            if viewModel.isLoading && entry.isEmpty {
                ProgressView("Loading data...")
            } else {
                List(entry.indices, id: \.self) { idx in
                    let item = entry[idx]
                    Button {
                        selectedEntryIndex = idx
                    } label: {
                        HStack {
                            let content = rowContent(item)
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
        .sheet(isPresented: Binding(get: { selectedEntryIndex != nil }, set: { if !$0 { selectedEntryIndex = nil } })) {
            if let idx = selectedEntryIndex {
                detailView(entry[idx])
            } else {
                // fallback empty view
                EmptyView()
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
