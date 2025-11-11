//
//  SearchConnectionDisclaimerView.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 07/11/25.
//
import Foundation
import SwiftUI

enum SheetContent: Identifiable {
    case terms(resource: BWellWrapper.healthResource.Result)
    case webView(url: URL)

    var id: String {
        switch self {
            case .terms(let resource):
                if let endpoint = resource.endpoint?.first?.name {
                    return endpoint
                }
                return "proa_demo"
            case .webView(let url):
                return url.absoluteString
        }
    }
}

struct SearchConnectionDisclaimerView: View {
    @State private var termsAccepted = false
    
    var resourceTitle: String
    var action: () -> Void

    // TODO: FIND A BETTER WAY TO ADD A NAVIGATION BAR TO THE SHEET WITHOUT USING .bwellNavigationBar() ViewModifier
    var body: some View {
        VStack(alignment: .leading, spacing: 15) {
            Text("\(resourceTitle) \(String(localized: "Connection Disclaimer"))")
                .multilineTextAlignment(.leading)

            VStack(spacing: 3) {
                Text("NOTE: ")
                    .fontWeight(.semibold)

                Text("Connection Note", tableName: "Localizable")
                    .multilineTextAlignment(.leading)
            }

            Spacer()

            HStack(alignment: .firstTextBaseline, spacing: 10) {
                Button {
                    termsAccepted.toggle()
                } label: {
                    Image(systemName: !termsAccepted ? "square" : "checkmark.square.fill")
                        .foregroundStyle(termsAccepted ? .bwellGreen : .black)
                }

                Text("Connection Credentials Confirmation", tableName: "Localizable")
                    .multilineTextAlignment(.leading)
            }.padding(.bottom, 10)

            proceedButton
        }
        .padding()
        .bwellNavigationBar(showMenu: .constant(false), navigationTitle: "Terms & Conditions") // TODO: Adjust correctly this navigation bar
    }

    // MARK: Proceed Button
    @ViewBuilder
    var proceedButton: some View {
        HStack {
            Spacer()
            Button {
                action()
            } label: {
                Text("Proceed")
                    .padding(5)
                    .background(termsAccepted ? .bwellGreen : .gray)
                    .foregroundStyle(.white)
                    .fontWeight(.semibold)
                    .clipShape(RoundedRectangle(cornerRadius: 5))
                    .disabled(!termsAccepted)
            }
            Spacer()
        }
    }
}
