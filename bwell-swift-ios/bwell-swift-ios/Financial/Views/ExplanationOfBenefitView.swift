import SwiftUI
#if os(iOS)
import UIKit
#endif
import BWellSDK

struct ExplanationOfBenefitView: View {
    @StateObject private var viewModel = ExplanationOfBenefitViewModel()
    @State private var showEOB: Bool = false

    var body: some View {
        List {
            Section(header: Text("Financial")) {
                Button {
                    showEOB = true
                } label: {
                    HStack {
                        VStack(alignment: .leading) {
                            Text("Explanation Of Benefits")
                                .fontWeight(.semibold)
                            Text("View explanation of benefit details")
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                        Spacer()
                        Image(systemName: "chevron.right")
                            .foregroundColor(.gray)
                    }
                    .padding(.vertical, 8)
                }
            }
        }
        .navigationTitle("Financial")
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Button(action: {
                    Task { await viewModel.refresh() }
                }) {
                    Image(systemName: "arrow.clockwise")
                }
            }
        }
        .task { await viewModel.loadExplanationOfBenefits() }
        .sheet(isPresented: $showEOB) {
            NavigationStack {
                EOBDetailView(jsonText: viewModel.eobJson, response: viewModel.eobData)
                    .toolbar {
                        ToolbarItem(placement: .navigationBarLeading) {
                            Button("Close") { showEOB = false }
                        }
                    }
            }
        }
    }
}

struct EOBDetailView: View {
    let jsonText: String
    let response: BWell.GetExplanationOfBenefitsResponse?

    @State private var expandAll: Bool = false
    @State private var searchQuery: String = ""

    var body: some View {
        VStack(alignment: .leading) {
            if jsonText.isEmpty {
                Spacer()
                Text("No EOB data yet")
                    .foregroundStyle(.secondary)
                    .padding()
                Spacer()
            } else {
                VStack(spacing: 8) {
                    HStack {
                        TextField("Search JSON...", text: $searchQuery)
                            .textFieldStyle(.roundedBorder)
                        Button(action: { copyToClipboard() }) {
                            Image(systemName: "doc.on.doc")
                        }
                        Button(action: { expandAll.toggle() }) {
                            Image(systemName: expandAll ? "arrow.up.left.and.arrow.down.right" : "arrow.down.right.and.arrow.up.left")
                        }
                    }
                    .padding([.leading, .trailing])

                    ScrollView {
                        JSONViewer(jsonString: jsonText, expandAll: expandAll, searchQuery: searchQuery)
                            .padding()
                    }
                }
            }
        }
        .navigationTitle("Explanation Of Benefits")
    }

    private func copyToClipboard() {
        #if os(iOS)
        UIPasteboard.general.string = jsonText
        #elseif os(macOS)
        let pasteboard = NSPasteboard.general
        pasteboard.clearContents()
        pasteboard.setString(jsonText, forType: .string)
        #endif
    }
}

struct ExplanationOfBenefitView_Previews: PreviewProvider {
    static var previews: some View {
        NavigationView {
            ExplanationOfBenefitView()
        }
    }
}
