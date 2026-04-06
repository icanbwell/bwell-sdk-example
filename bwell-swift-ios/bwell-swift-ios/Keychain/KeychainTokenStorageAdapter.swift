//
//  KeychainTokenStorageAdapter.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 11/11/25.
//
import Foundation
import BWellSDK

/**
 * Keychain-backed implementation of BWell.TokenStorage.
 * Stores all token key/values as a single JSON object using your existing KeychainService.
 */
final class KeychainTokenStorageAdapter: BWell.TokenStorage {
    private let keychain: KeychainService

    init(keychain: KeychainService = .shared) {
        self.keychain = keychain
    }

    // MARK: - Internal JSON blob helpers

    private func readStore() -> [String: String] {
        do {
            guard let blob = try keychain.loadToken(), !blob.isEmpty else {
                print("KeychainTokenStorageAdapter: No token blob found in keychain.")
                return [:]
            }
            print("KeychainTokenStorageAdapter: Read token blob from keychain.")
            guard let data = blob.data(using: .utf8) else { return [:] }

            let obj = try JSONSerialization.jsonObject(with: data, options: [])
            guard let dict = obj as? [String: Any] else { return [:] }

            var out: [String: String] = [:]
            out.reserveCapacity(dict.count)

            for (k, v) in dict {
                if let s = v as? String {
                    out[k] = s
                } else if let n = v as? NSNumber {
                    out[k] = n.stringValue
                }
            }
            return out
        } catch {
            print("KeychainTokenStorageAdapter.readStore error: \(error)")
            return [:]
        }
    }

    private func writeStore(_ dict: [String: String]) {
        do {
            print("KeychainTokenStorageAdapter: Writing token blob to keychain...")
            let data = try JSONSerialization.data(withJSONObject: dict, options: [])
            guard let s = String(data: data, encoding: .utf8) else { return }
            try keychain.save(token: s)
            print("KeychainTokenStorageAdapter: Successfully wrote tokens.")
        } catch {
            print("KeychainTokenStorageAdapter.writeStore error: \(error)")
        }
    }

    // MARK: - BWell.TokenStorage conformance

    func get(key: String) -> String? {
        readStore()[key]
    }

    func get(key: String, completion: @escaping (String?) -> Void) {
        completion(get(key: key))
    }

    func getMany(keys: [String]) -> [String : String?] {
        let store = readStore()
        var result: [String: String?] = [:]

        result.reserveCapacity(keys.count)
        for k in keys { result[k] = store[k] }

        return result
    }

    func set(key: String, value: String) {
        var store = readStore()
        store[key] = value
        writeStore(store)
    }

    func setMany(keyValues: [String : String]) {
        var store = readStore()
        store.merge(keyValues) { _, new in new }
        writeStore(store)
    }

    func delete(key: String) -> Bool {
        var store = readStore()
        let removed = store.removeValue(forKey: key) != nil

        if store.isEmpty {
            do {
                try keychain.deleteToken()
            } catch {
                // Fallback: clear the blob if full delete fails
                writeStore([:])
            }
        } else {
            writeStore(store)
        }

        return removed
    }
}
