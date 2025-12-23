//
//  APIKeyService.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 12/11/25.
//
import Foundation

final class APIKeyService {
    static let shared = APIKeyService()
    private let apiKeyUD: String = "bwell_api_key"

    private init() {}

    func save(_ apiKey: String) {
        UserDefaults.standard.set(apiKey, forKey: apiKeyUD)
    }

    func getAPIKey() -> String? {
        UserDefaults.standard.string(forKey: apiKeyUD)
    }

    func clear() {
        UserDefaults.standard.removeObject(forKey: apiKeyUD)
    }
}
