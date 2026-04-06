//
//  EnvConfig.swift
//  bwell-swift-ios
//
//  Reads credentials from a .env file bundled with the app.
//  Copy .env.example to .env and fill in your values.
//  The .env file is added to the app bundle but excluded from git via .gitignore.
//

import Foundation

struct EnvConfig {
    let clientKey: String?
    let jwtToken: String?

    /// Load from a .env file in the app bundle
    static func load() -> EnvConfig {
        // Try bundle first
        if let url = Bundle.main.url(forResource: ".env", withExtension: nil),
           let contents = try? String(contentsOf: url, encoding: .utf8) {
            return parse(contents)
        }

        // Try project root (for development — .env next to the .xcodeproj)
        // This won't work in a sandboxed app but is useful during Xcode runs
        if let projectDir = Bundle.main.infoDictionary?["PROJECT_DIR"] as? String {
            let envPath = (projectDir as NSString).appendingPathComponent(".env")
            if let contents = try? String(contentsOfFile: envPath, encoding: .utf8) {
                return parse(contents)
            }
        }

        return EnvConfig(clientKey: nil, jwtToken: nil)
    }

    private static func parse(_ contents: String) -> EnvConfig {
        var values: [String: String] = [:]

        for line in contents.components(separatedBy: .newlines) {
            let trimmed = line.trimmingCharacters(in: .whitespaces)
            // Skip comments and empty lines
            if trimmed.isEmpty || trimmed.hasPrefix("#") { continue }

            let parts = trimmed.split(separator: "=", maxSplits: 1)
            guard parts.count == 2 else { continue }

            let key = String(parts[0]).trimmingCharacters(in: .whitespaces)
            var value = String(parts[1]).trimmingCharacters(in: .whitespaces)

            // Remove surrounding quotes
            if (value.hasPrefix("\"") && value.hasSuffix("\"")) ||
               (value.hasPrefix("'") && value.hasSuffix("'")) {
                value = String(value.dropFirst().dropLast())
            }

            values[key] = value
        }

        return EnvConfig(
            clientKey: values["BWELL_CLIENT_KEY"],
            jwtToken: values["BWELL_JWT_TOKEN"]
        )
    }

    var hasCredentials: Bool {
        guard let key = clientKey, !key.isEmpty,
              let token = jwtToken, !token.isEmpty else { return false }
        return true
    }
}
