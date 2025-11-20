//
//  BWellButton.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 28/10/25.
//
import Foundation
import SwiftUI

struct BWellButton: View {
    let title: String
    var buttonColor: Color = .bwellBlue
    var isLoading: Bool = false
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            if isLoading {
                ProgressView()
                    .progressViewStyle(CircularProgressViewStyle(tint: .white))
                    .frame(maxWidth: .infinity, minHeight: 30)
            } else {
                Text(title)
                    .fontWeight(.semibold)
                    .frame(maxWidth: .infinity, minHeight: 30)
            }
        }
        .padding()
        .background(buttonColor)
        .foregroundColor(.white)
        .cornerRadius(10)
        .disabled(isLoading)
    }
}
