import SwiftUI

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
                    ForEach(dict.keys.sorted(), id: \.self) { key in
                        if let val = dict[key] {
                            CollapsibleKeyView(key: key, value: val, expandAll: expandAll, searchQuery: searchQuery)
                        }
                    }
                }
            } else if let array = value as? [Any] {
                VStack(alignment: .leading, spacing: 8) {
                    ForEach(Array(array.enumerated()), id: \.0) { index, item in
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

// Small preview helper
struct CollapsibleJSONView_Previews: PreviewProvider {
    static var sample = "{ \"items\": [ { \"id\": \"1\", \"name\": \"Plan A\", \"details\": { \"status\": \"active\", \"coverage\": 80 } }, { \"id\": \"2\", \"name\": \"Plan B\", \"details\": { \"status\": \"inactive\", \"coverage\": 50 } } ] }"
    static var previews: some View {
        ScrollView {
            CollapsibleJSONView(jsonString: sample)
                .padding()
        }
    }
}
