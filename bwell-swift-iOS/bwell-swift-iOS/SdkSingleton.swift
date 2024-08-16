//
//  SdkSingleton.swift
//  bwell-swift-iOS
//
//  Created by Kyle Wade on 8/13/24.
//

import Foundation
import bwell_sdk_swift

enum SdkSingletonError: Error {
    case sdkNotInitialized(String)
    case invalidCredentials(String)
}

class SdkSingleton: ObservableObject {
    private var sdkInstance: BWellSdk? = nil;
    
    init() {
        
    }
    
    public func configure(clientKey: String) async throws {
        // TODO: make these things configurable
        do {
            let config = BWellConfig(clientKey: clientKey, logLevel: .debug, timeout: 30, retryPolicy: nil, telemetryEnabled: false);
            self.sdkInstance = try BWellSdk(config: config);
            try await self.sdkInstance!.initialize();
        } catch {
            throw error;
        }
    }
    
    public func getInstance() throws -> BWellSdk {
        if (self.sdkInstance == nil) {
            throw SdkSingletonError.sdkNotInitialized("SDK not initialized. Please call configure() before calling getInstance()")
        }
        
        return self.sdkInstance!;
    }
}
