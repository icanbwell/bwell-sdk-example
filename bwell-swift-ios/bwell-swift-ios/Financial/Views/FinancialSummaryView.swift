import SwiftUI
#if os(iOS)
import UIKit
#endif
import BWellSDK

struct FinancialSummaryView: View {
    @StateObject private var viewModel = FinancialSummaryViewModel()
    @State private var showCoverages: Bool = false

    var body: some View {
        List {
            Section(header: Text("Financial")) {
                Button {
                    showCoverages = true
                } label: {
                    HStack {
                        VStack(alignment: .leading) {
                            Text("Coverages")
                                .fontWeight(.semibold)
                            Text("View insurance coverages and details")
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
        .task { await viewModel.loadCoverages() }
        .sheet(isPresented: $showCoverages) {
            NavigationStack {
                CoveragesDetailView(jsonText: viewModel.coveragesJson, response: viewModel.coverageData)
                    .toolbar {
                        ToolbarItem(placement: .navigationBarLeading) {
                            Button("Close") { showCoverages = false }
                        }
                    }
            }
        }
    }
}

struct CoveragesDetailView: View {
    let jsonText: String
    let response: BWell.GetCoverageResponse?

    @State private var expandAll: Bool = false
    @State private var searchQuery: String = ""

    var body: some View {
        VStack(alignment: .leading) {
            if jsonText.isEmpty {
                Spacer()
                Text("No coverage data yet")
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
        .navigationTitle("Coverages")
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

struct FinancialSummaryView_Previews: PreviewProvider {
    static var previews: some View {
        NavigationView {
            FinancialSummaryView()
        }
    }
}

// MARK: - Collapsible JSON Viewer (inlined to avoid target scope issues)
struct CollapsibleJSONView: View {
    let jsonString: String
    var expandAll: Bool = false
    var searchQuery: String = ""

    var body: some View {
        if let data = jsonString.data(using: .utf8) {
            if let jsonObj = try? JSONSerialization.jsonObject(with: data, options: []) {
                JSONValueView(value: jsonObj, expandAll: expandAll, searchQuery: searchQuery)
            } else {
                Text("Invalid JSON")
                    .font(.caption)
                    .foregroundColor(.red)
            }
        } else {
            Text("Empty JSON")
                .font(.caption)
                .foregroundColor(.secondary)
        }
    }
}

fileprivate struct JSONValueView: View {
    let value: Any
    var expandAll: Bool = false
    var searchQuery: String = ""

    var body: some View {
        Group {
            if let dict = value as? [String: Any] {
                VStack(alignment: .leading, spacing: 8) {
                    ForEach(dict.keys.sorted(), id: \ .self) { key in
                        if let val = dict[key] {
                            CollapsibleKeyView(key: key, value: val, expandAll: expandAll, searchQuery: searchQuery)
                        }
                    }
                }
            } else if let array = value as? [Any] {
                VStack(alignment: .leading, spacing: 8) {
                    ForEach(Array(array.enumerated()), id: \ .0) { index, item in
                        CollapsibleKeyView(key: "[\(index)]", value: item, expandAll: expandAll, searchQuery: searchQuery)
                    }
                }
            } else {
                ValueTextView(value: value, searchQuery: searchQuery)
            }
        }
    }
}

fileprivate struct CollapsibleKeyView: View {
    let key: String
    let value: Any
    var expandAll: Bool
    var searchQuery: String

    @State private var isExpanded: Bool = false

    var body: some View {
        DisclosureGroup(isExpanded: Binding(get: { expandAll ? true : isExpanded }, set: { isExpanded = $0 })) {
            JSONValueView(value: value, expandAll: expandAll, searchQuery: searchQuery)
                .padding(.leading, 8)
        } label: {
            HStack {
                Text(key)
                    .font(.headline)
                Spacer()
                if matchesSearch(text: key) {
                    Image(systemName: "magnifyingglass")
                        .foregroundColor(.accentColor)
                }
            }
        }
    }

    private func matchesSearch(text: String) -> Bool {
        guard !searchQuery.isEmpty else { return false }
        return text.localizedCaseInsensitiveContains(searchQuery)
    }
}

fileprivate struct ValueTextView: View {
    let value: Any
    var searchQuery: String

    var body: some View {
        if value is NSNull {
            Text("null")
                .font(.system(.body, design: .monospaced))
                .foregroundColor(.secondary)
        } else if let str = value as? String {
            highlightedText("\"\(str)\"")
        } else if let num = value as? NSNumber {
            highlightedText(num.stringValue)
        } else {
            highlightedText(String(describing: value))
        }
    }

    @ViewBuilder
    private func highlightedText(_ text: String) -> some View {
        if !searchQuery.isEmpty && text.localizedCaseInsensitiveContains(searchQuery) {
            let parts = text.components(separatedBy: searchQuery)
            HStack(spacing: 0) {
                ForEach(0..<parts.count, id: \.self) { i in
                    Text(parts[i])
                        .font(.system(.body, design: .monospaced))
                    if i < parts.count - 1 {
                        Text(searchQuery)
                            .font(.system(.body, design: .monospaced))
                            .background(Color.yellow)
                    }
                }
            }
        } else {
            Text(text)
                .font(.system(.body, design: .monospaced))
        }
    }
}

// JSONViewer: uses RakuyoKit/JSONPreview when available, otherwise falls back to CollapsibleJSONView
#if canImport(JSONPreview)
import JSONPreview

struct JSONViewer: View {
    let jsonString: String
    var expandAll: Bool = false
    var searchQuery: String = ""

    var body: some View {
        if let data = jsonString.data(using: .utf8) {
            // NOTE: adjust initializer if JSONPreview's API differs.
            // Common API might be `JSONPreview(data:)` or `JSONPreviewView(json:)`.
            // Replace the line below with the correct initializer when adding the package.
            JSONPreview(data: data)
        } else {
            Text("Empty JSON")
                .font(.caption)
                .foregroundColor(.secondary)
        }
    }
}
#else
struct JSONViewer: View {
    let jsonString: String
    var expandAll: Bool = false
    var searchQuery: String = ""
    var body: some View { CollapsibleJSONView(jsonString: jsonString, expandAll: expandAll, searchQuery: searchQuery) }
}
#endif

