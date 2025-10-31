//
//  HealthDataDetailView.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 28/10/25.
//

import Foundation
import SwiftUI

struct HealthDataDetailView<Content: View>: View {
    var view: Content

    var body: some View {
        ZStack(alignment: .topLeading) {
            view
        }
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Image("bwell-logo-header")
                    .resizable()
                    .scaledToFit()
                    .frame(width: 60)
            }
        }
    }
}

// TODO: Remove Generic view after all views for Health Data have been implemented.
struct GenericView: View {
    var body: some View {
        Text("Hello, world!")
    }
}
