//
//  AuthenticationTextField.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 28/10/25.
//
import Foundation
import SwiftUI
enum TextFieldType {
    case login
    case standard
}

struct AuthenticationTextField: View {
    var placeholder: String
    @Binding var text: String
    var iconName: String
    var isSecure: Bool = false
    var errorMessage: String?
    
    var body: some View {
        VStack(alignment: .leading) {
            HStack {
                Image(systemName: iconName)
                    .font(.system(size: 18))
                    .frame(width: 25, alignment: .leading)
                    .foregroundColor(.white)

                credentialsTextField
                    .foregroundStyle(.white)
                    .textFieldStyle(.plain)
                    .autocapitalization(.none)
                    .disableAutocorrection(true)

                if !text.isEmpty {
                    Button {
                        text = ""
                    } label: {
                        Image(systemName: "plus.circle.fill")
                            .rotationEffect(.degrees(45))
                            .foregroundStyle(Color(.systemGray4))
                            .frame(width: 10)
                    }
                }
            }
            .padding(.bottom, 5)
            .padding(.horizontal, 10)
            .clipShape(RoundedRectangle(cornerRadius: 10))
            
            Rectangle()
                .frame(height: 1)
                .foregroundColor(.gray)

            if let errorMessage = errorMessage {
                HStack {
                    Image(systemName: "exclamationmark.triangle.fill")
                        .foregroundStyle(.yellow)
                    Text(errorMessage)
                        .foregroundStyle(.bwellRed)
                        .font(.subheadline)
                }.padding(.top, 2)
            }
        }
    }

    var credentialsTextField: some View {
        Group {
            if !isSecure {
                TextField("",
                          text: $text,
                          prompt: Text(placeholder)
                    .foregroundStyle(.gray)
                )
            } else {
                SecureField("",
                            text: $text,
                            prompt: Text(placeholder)
                    .foregroundStyle(.gray)

                )
            }
        }
    }
}

#Preview {
    ZStack {
        Color.bwellPurple
            .ignoresSafeArea()
        AuthenticationTextField(placeholder: "Placeholder",
                       text: .constant(""),
                       iconName: "envelope",
                       errorMessage: "error message")
        .padding(.horizontal)
    }
}
