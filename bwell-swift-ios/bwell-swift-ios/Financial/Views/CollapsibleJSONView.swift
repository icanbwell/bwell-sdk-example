import SwiftUI

struct CollapsibleJSONView: View {
    let jsonString: String

    var body: some View {
        if let data = jsonString.data(using: .utf8) {
            if let jsonObj = try? JSONSerialization.jsonObject(with: data, options: []) {
                JSONValueView(value: jsonObj)
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
    var body: some View {
        Group {
            if let dict = value as? [String: Any] {
                VStack(alignment: .leading, spacing: 8) {
                    ForEach(dict.keys.sorted(), id: \ .self) { key in
                        if let val = dict[key] {
                            DisclosureGroup(key) {
                                JSONValueView(value: val)
                                    .padding(.leading, 8)
                            }
                        }
                    }
                }
            } else if let array = value as? [Any] {
                VStack(alignment: .leading, spacing: 8) {
                    ForEach(Array(array.enumerated()), id: \ .0) { index, item in
                        DisclosureGroup("[\(index)]") {
                            JSONValueView(value: item)
                                .padding(.leading, 8)
                        }
                    }
                }
            } else if value is NSNull {
                Text("null")
                    .font(.system(.body, design: .monospaced))
                    .foregroundColor(.secondary)
            } else if let str = value as? String {
                Text("\"\(str)\"")
                    .font(.system(.body, design: .monospaced))
            } else if let num = value as? NSNumber {
                Text(num.stringValue)
                    .font(.system(.body, design: .monospaced))
            } else {
                Text(String(describing: value))
                    .font(.system(.body, design: .monospaced))
            }
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
