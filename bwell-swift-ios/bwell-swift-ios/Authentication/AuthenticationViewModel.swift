//
//  AuthenticationViewModel.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 31/10/25.
//
import Foundation
import BWellSDK

struct Credentials {
    static let clientKey: String = "eyJyIjoidGk3ank1N2J4ODdheGV3MzBzMHciLCJlbnYiOiJjbGllbnQtc2FuZGJveCIsImtpZCI6IndhbGdyZWVucy1jbGllbnQtc2FuZGJveCJ9"
    
    static let katesClientKey: String = "eyJyIjoiNWV4b3d2N2RqZzVtbWpyb2JlaiIsImVudiI6ImNsaWVudC1zYW5kYm94Iiwia2lkIjoic2Ftc3VuZy1jbGllbnQtc2FuZGJveCJ9"

    static let colesClientKey: String = "eyJyIjoiY2Zoa2h3ODZvNHdoNWFiOW9kaHgiLCJlbnYiOiJkZXYiLCJraWQiOiJid2VsbF9kZW1vLWRldiJ9"

    static let token: String = "eyJlbmMiOiJBMTI4Q0JDLUhTMjU2IiwiYWxnIjoiRUNESC1FUyIsImtpZCI6IndhbGdyZWVucy1jbGllbnQtc2FuZGJveCIsImVwayI6eyJrdHkiOiJFQyIsImNydiI6IlAtMjU2IiwieCI6Ilo0ZXNYMnFRSWNVZjlmWG5lSUxnQ1JaZV9HZVFoVC1jd0xDTGZPQWdPZXMiLCJ5IjoiYjY0LU9UN29GUmpxSFBsLU9ULWpIekJrcU43VFp1eFk5R1ZLMDNqS1FOayJ9fQ..fwBk9qqw4ceyFW-1NqGGRQ.qN-uJGoPHcNG2CUwGw1hkvRfBgkqu_ZAh7RY8-gRPr5tHMYHVVR-tZquu9QidqKNqbAwXpP11stL32FjE4kLPSetmFJOH0w92a7fYmBUdG52WN1ObInToXchBLLa9cFLHyCz53g67sgW09-D0EDCjOuskh9d0f8FeAUE1ubtQSYvz9bnDISWtJ4yGHTpc6hPKRJI1aWxm8AQ8jfe_YZ6_eM-lKfjPrs7m-RCrIpENxdvMOMMf_tH16T1lohUoA9HRVwpLr1DdSgbGGyG69UuxYw89yTBBUnMTKltrydH9p-424530Bs6ylWrkJHJ60NHuESmPvEltNUcfW45t-VOR3KViiVnebVu93hC1-W2_Z802H2qlbICNAHF4FRgnhfduz6AeLq5CEVPj-daBsevcQ6dVmHo5Tp-HuzV1qd_3JczE-RYCODnJhZDaO6Lf2OF85hmYLco0i1S5e_CclNHVQX53Ggs_8OgWJWZVeg02aJOK9w52EeHS5M_-Q_bsPUhl92ZQWXRt6WmmMGqoise6d0Eke-GBPdiCF8_r0-2GdnTGhABcrDitx4Dgx_B5oFkRwBtVeDxb0rB7refmAgkBWl9vScHAoOqq_QOLBs8t-K_n4H306HAfz4e056UoVNcRo92ntmsyWNlDQSaeGCxCJ6sfHARWH7RGVvfQ92hJzBjaE_73JNUCL56oZnh5JaluyvVV3XqBq4ruO_a1lKduKc5-2zR0XQDFYrFjVC0TDdeTOmAWGEp1wEQGC1ygpzHGOrDJHC2gya7YRvFGzH7xGRvxdKop-hjD2RJ9ThJEYXA_nD6P3at6OuVrRubSRknkV43NERRIPMM4ckSo6pN-9craCk1s27CW_h_J9FDR_8.5x8h4562PaZ9pUdjeo5lWQ"
}

@MainActor
final class AuthenticationViewModel: ObservableObject {
    private let sdkManager: BWellSDKManager

    // Authentication published properties
    @Published var clientKey: String = Credentials.clientKey
    @Published var oauthToken: String = Credentials.token
    @Published var email: String = "test@test.test"
    @Published var password: String = "Password@1"

    // Published properties
    @Published var isLoading: Bool = false
    @Published var apiKeyValidated: Bool = false

    @Published var emailErrorMessage: String? = nil
    @Published var passwordErrorMessage: String? = nil
    @Published var errorMessage: String? = nil

    init() {
        sdkManager = .shared
    }

    func initializeSDK() {
        isLoading = true

        guard !clientKey.isEmpty else {
            errorMessage = "Client key is required."
            isLoading = false
            return
        }

        Task {
            do {
                try await sdkManager.initilize(clientKey: clientKey)

                self.apiKeyValidated = true
                self.isLoading = false
            } catch {
                errorMessage = "SDK initilization failed"
                isLoading = false
            }
        }
    }

    func loginWithOAuthToken() {
        isLoading = true

        guard !oauthToken.isEmpty else {
            errorMessage = "OAuth token is required."
            isLoading = false
            return
        }

        Task {
            do {
                let credentials = BWell.Credentials.oauth(token: oauthToken)

                try await sdkManager.authenticate(credentials: credentials)
                try await sdkManager.createConsent()

                isLoading = false
            } catch {
                errorMessage = "Login failed"
                isLoading = false
            }
        }
    }

    func loginWithEmailAndPassword() {
        isLoading = true

        if email.isEmpty {
            isLoading = false
            emailErrorMessage = "email is required."
        }

        if password.isEmpty {
            isLoading = false
            passwordErrorMessage = "password is required."
        }

        Task {
            do {
                let credentials = BWell.Credentials.emailPassword(email: email, password: password)

                try await sdkManager.authenticate(credentials: credentials)
                try await sdkManager.createConsent()        

                isLoading = false
            } catch {
                errorMessage = "Login failed"
                isLoading = false
            }
        }
    }
}
