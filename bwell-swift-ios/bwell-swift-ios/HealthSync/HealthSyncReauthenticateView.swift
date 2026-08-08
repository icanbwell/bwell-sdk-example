//
//  HealthSyncReauthenticateView.swift
//  bwell-swift-ios
//
//  Ported from bwell-sdk-swift/Examples/HealthSyncSampleApp/Sources/ReauthenticateView.swift.
//  Account-switch screen for this demo.
//

import SwiftUI

struct HealthSyncReauthenticateView: View {
    @ObservedObject var viewModel: HealthSyncPlaygroundViewModel
    let error: String?

    var body: some View {
        PlaygroundCenteredForm {
            Text("Switch account")
                .font(.title2.bold())
                .foregroundStyle(Color.playgroundHeading)
                .multilineTextAlignment(.center)

            TextField("New OAuth token", text: $viewModel.reauthTokenInput)
                .textFieldStyle(.plain)
                .playgroundFieldStyle()
                .autocorrectionDisabled().textInputAutocapitalization(.never)

            if let error {
                Text(error).foregroundStyle(.red).font(.footnote)
                    .multilineTextAlignment(.center)
            }

            Button("Authenticate") { viewModel.reauthenticate() }
                .buttonStyle(.playgroundPrimary)
        }
    }
}
