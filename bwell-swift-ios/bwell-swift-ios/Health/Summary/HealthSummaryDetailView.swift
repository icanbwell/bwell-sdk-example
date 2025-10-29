//
//  HealthSummaryDetailView.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 28/10/25.
//

import Foundation
import SwiftUI

struct HealthSummaryDetailView<Content: View>: View {
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

struct AllergyIntolerancesDetailView: View {
    var entries:  [AllergyIntoleranceEntry]

    var body: some View {
        VStack(alignment: .leading) {
            Text("Total: \(entries.count)")
                .font(.title2)
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(.bottom, 20)

            HStack {
                Text("Allergy")
                    .font(.system(size: 18, weight: .semibold))
                Spacer()
                Text("Criticality")
                    .font(.system(size: 18, weight: .semibold))
            }.padding(.bottom, 10)

            ForEach(entries) { entry in
                HStack {
                    if let allergy = entry.allergy, let criticality = entry.criticality {
                        Text(allergy)
                        Spacer()
                        Text(criticality)
                    }
                }
            }
            Spacer()
        }
        .padding(.horizontal)
        .padding(.top, 20)
    }
}

struct GenericView: View {
    var body: some View {
        Text("Hello, world!")
    }
}
