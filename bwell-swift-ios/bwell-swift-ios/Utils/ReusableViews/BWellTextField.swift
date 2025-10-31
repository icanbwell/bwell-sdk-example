//
//  BWellTextField.swift
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
        VStack(alignment: .leading) {
            HStack {
                Image(systemName: iconName)
                    .font(.system(size: 18))
                    .frame(width: 25, alignment: .leading)
                    .foregroundColor(.black)
                Group {
                    if isSecure {
                        SecureField(placeholder, text: $text)
                    } else {
                        TextField(placeholder, text: $text)
                    }
                }
                .shadow(color: Color.black.opacity(0.1), radius: 5, x: 0, y: 2)
                .autocapitalization(.none)
                .disableAutocorrection(true)
            }
            .padding(.vertical)
            .padding(.leading, 10)
            .background(Color.white)
            .clipShape(RoundedRectangle(cornerRadius: 10))
            
            
            if let errorMessage = errorMessage {
                HStack {
                    Image(systemName: "exclamationmark.triangle.fill")
                        .foregroundStyle(.yellow)
                    Text(errorMessage)
                        .foregroundStyle(.red)
                        .font(.subheadline)
                }.padding(.top, 5)
            }
        }
    }
}

#Preview {
    ZStack {
        Color.bwellPurple
            .ignoresSafeArea()
        BWellTextField(placeholder: "Placeholder",
                       text: .constant(""),
                       iconName: "envelope",
                       errorMessage: "error message")
        .padding(.horizontal)
    }
}
