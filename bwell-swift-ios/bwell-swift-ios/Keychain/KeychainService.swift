//
//  KeychainService.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 11/11/25.
//
import Foundation
import Security

enum KeychainError: Error {
    case stringToData_ConversionError
    case dataToString_ConversionError
    case unhandledError(status: OSStatus)
}

final class KeychainService {
    static let shared = KeychainService()

    private init() {}

    private let service = "com.bwell.sdkSampleApp"
    private let account = "bwell-user-token"

    /**
     * Saves the authentication token to the Keychain
     *
     * - Parameters:
     *    - token: The JWT Token to save

     * - Throws:
     *    - Error if the save operation fails.
     */
    public func save(token: String) throws {
        guard let data = token.data(using: .utf8) else {
            throw KeychainError.stringToData_ConversionError
        }

        // 1. Build the query to search for an existing item
        let query: [CFString:Any] = [
            kSecClass: kSecClassGenericPassword,
            kSecAttrService: service,
            kSecAttrAccount: account
        ]

        // 2. Define the attributes for the new or updated item.
        let attributes = [kSecValueData:data] as [String:Any]

        // 3. Try to update an existing item first
        let status = SecItemUpdate(query as CFDictionary, attributes as CFDictionary)

        // 4. If no item exists to update, add a new one.
        if status == errSecItemNotFound {
            let newQuery = query.merging([kSecValueData:data]) {(_, new) in new }
            let addStatus = SecItemAdd(newQuery as CFDictionary, nil)

            guard addStatus == errSecSuccess else {
                throw KeychainError.unhandledError(status: addStatus)
            }
        } else if status != errSecSuccess {
            // If the error failed for any other reason, throw and error.
            throw KeychainError.unhandledError(status: status)
        }
    }

    /**
     * Loads the authentication token from the keychain.
     *
     * - Returns:
     *    - The saved token string, or nil if it doesn't exist.
     *
     * - Throws:
     *    - An error if the load operation fails.
     */
    func loadToken() throws -> String? {
        let query = [
            kSecClass: kSecClassGenericPassword,
            kSecAttrService: service,
            kSecAttrAccount: account,
            kSecReturnData: true,
            kSecMatchLimit: kSecMatchLimitOne
        ] as [String:Any]

        var item: CFTypeRef?
        let status = SecItemCopyMatching(query as CFDictionary, &item)

        if status == errSecItemNotFound {
            return nil
        }

        guard let data = item as? Data,
              let token = String(data: data, encoding: .utf8) else {
            throw KeychainError.dataToString_ConversionError
        }

        return token
    }

    /**
     * Deletes the authentication token from the keychain.
     *
     * - Throws:
     *    - An error if the delete operation fails.
     */
    func deleteToken() throws {
        let query = [
            kSecClass: kSecClassGenericPassword,
            kSecAttrService: service,
            kSecAttrAccount: account
        ] as [String: Any]

        let status = SecItemDelete(query as CFDictionary)

        guard status == errSecSuccess || status == errSecItemNotFound else {
            throw KeychainError.unhandledError(status: status)
        }
    }

    /**
     * Deletes all tokens managed by the service.
     *
     * - Throws:
     *    - An error if the delete operation fails.
     */
    func clear() throws {
        let query = [
            kSecClass: kSecClassGenericPassword,
            kSecAttrService: service
        ] as [String: Any]

        let status = SecItemDelete(query as CFDictionary)

        guard status == errSecSuccess || status == errSecItemNotFound else {
            throw KeychainError.unhandledError(status: status)
        }
    }
}


