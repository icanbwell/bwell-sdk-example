//
//  SearchConnectionsView.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 07/11/25.
//
import Foundation
import SwiftUI

struct SearchConnectionsView: View {
    @Environment(\.dismissSearch) private var dismissSearch
    @Environment(\.dismiss) private var dismiss
    @EnvironmentObject private var sdkManager: SDKManager

    @ObservedObject private var viewModel = SearchConnectionsViewModel()
    @ObservedObject private var manageConnectionsViewModel = ManageConnectionsViewModel()

    @State private var searchedText: String = ""
    @State private var activeSheet: SheetContent?
    @State private var showWebView: Bool = false
    @State private var showOnSuccessMessage: Bool = false

    var connection: ConnectionsModel

    var body: some View {
        ZStack {
            if !viewModel.healthResources.isEmpty {
                resultsView
                    .padding(.top, 10)
            } else {
                VStack {
                    Image(systemName: "magnifyingglass")
                        .padding(.bottom, 10)

                    Text("Search by clinic/medical office name, hospital name. health system name, or city/state.")
                }.padding(.horizontal)
            }
        }
        .toolbarColorScheme(.dark, for: .navigationBar)
        .toolbarBackgroundVisibility(.visible, for: .navigationBar)
        .toolbarBackground(.bwellPurple, for: .navigationBar)
        .navigationBarTitleDisplayMode(.inline)
        .navigationTitle(Text(connection.title))
        .searchable(text: $searchedText, placement: .toolbar, prompt: Text("Search"))
        .submitLabel(.search)
        .onSubmit(of: .search) {
            Task {
                guard let sdk = sdkManager.sdk else { return }
                await viewModel.searchHealthResources(searchedText: searchedText, sdk: sdk)
            }
            dismissSearch()
        }
        .sheet(item: $activeSheet) { sheetContent in
            switch sheetContent {
                case .terms(let resource):
                    if let title = resource.content {
                        SearchConnectionDisclaimerView(resourceTitle: title) {
                            Task {
                                guard let sdk = sdkManager.sdk else { return }
                                await manageConnectionsViewModel.getOAuthURL(search: resource.endpoint?.first?.name ?? "proa_demo", sdk: sdk)
                            }
                        }
                        .presentationDetents([.medium])
                        .presentationDragIndicator(.visible)
                    }
                case .webView(let url):
                    WebViewWrapper(url: url) {
                        activeSheet = nil
                        Task {
                            try? await Task.sleep(nanoseconds: 100_000_000) // 0.1 seconds
                            showOnSuccessMessage = true
                        }
                    }
            }
        }
        .onChange(of: manageConnectionsViewModel.url) { _, url in
            if let url = url {
                activeSheet = .webView(url: url)
            }
        }
        .alert("Success!", isPresented: $showOnSuccessMessage) {
            Button("Done") {}
        } message: {
            Text("Authentication successful!")
        }
        .onChange(of: showOnSuccessMessage) { _, newValue in
            if !newValue {
                dismiss()
            }
        }
    }

    // MARK: Results view
    @ViewBuilder
    var resultsView: some View {
        VStack(alignment: .leading, spacing: 10) {
            Text("Results (\(viewModel.healthResources.count))")
                .font(.headline)
                .fontWeight(.semibold)
                .padding(.bottom, 5)

            List {
                ForEach(viewModel.healthResources, id: \.id) { result in
                    if let content = result.content, !content.isEmpty {
                        Button {
                            activeSheet = .terms(resource: result)
                        } label: {
                            ListItem(title: content)
                        }
                    }
                }
            }.listStyle(.plain)
        }.padding(.horizontal)
    }
}

private struct ListItem: View {
    var title: String

    var body: some View {
        HStack(alignment: .firstTextBaseline) {
            Image(systemName: "person.circle")
                .frame(width: 25, alignment: .leading)

            Text(title)
                .font(.headline)
                .fontWeight(.medium)

            Spacer()

            Image(systemName: "chevron.right")
                .foregroundStyle(.gray)
        }.listRowSeparator(.hidden, edges: .all)
    }
}

#Preview {
    SearchConnectionsView(connection: .clinics)
        .environmentObject(NavigationRouter())
}
