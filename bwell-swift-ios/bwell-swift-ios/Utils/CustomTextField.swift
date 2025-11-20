//
//  CustomTextField.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 28/10/25.
//
import Foundation
import SwiftUI

struct BWellTextField: View {
    var placeholder: String
    @Binding var text: String
    var iconName: String
    var isSecure: Bool = false
    var errorMessage: String?

    var body: some View {
        HStack(spacing: 15) {
            Image(systemName: iconName)
                .font(.system(size: 18))
                .frame(width: 25, alignment: .center)
                .foregroundColor(.white)

            VStack(alignment: .leading) {
                Group {
                    if isSecure {
                        SecureField(placeholder, text: $text)
                    } else {
                        TextField(placeholder, text: $text)
                    }
                }
                .padding()
                .background(Color.white)
                .cornerRadius(10)
                .shadow(color: Color.black.opacity(0.1), radius: 5, x: 0, y: 2)
                .autocapitalization(.none)
                .disableAutocorrection(true)

                if let errorMessage = errorMessage {
                    HStack {
                        Image(systemName: "exclamationmark.triangle.fill")
                            .foregroundStyle(.yellow)
                        Text(errorMessage)
                            .foregroundStyle(.red)
                            .font(.subheadline)
                    }
                }
            }
        }
    }
}

#Preview {
    BWellTextField(placeholder: "Placeholder",
                    text: .constant(""),
                    iconName: "envelope",
                    errorMessage: "error message")
}
