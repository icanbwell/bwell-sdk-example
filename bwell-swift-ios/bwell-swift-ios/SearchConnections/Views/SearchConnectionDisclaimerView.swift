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

    var body: some View {
        VStack(alignment: .leading, spacing: 15) {
            HStack {
                Spacer()
                Text("Discalimer")
                    .font(.headline)
                    .fontWeight(.medium)
                    .padding(.vertical, 10)
                Spacer()
            }

            Text("\(resourceTitle) \(String(localized: "Connection Disclaimer"))")
                .multilineTextAlignment(.leading)

            VStack(alignment: .leading, spacing: 3) {
                Text("Note: ")
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
        }.padding()
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
