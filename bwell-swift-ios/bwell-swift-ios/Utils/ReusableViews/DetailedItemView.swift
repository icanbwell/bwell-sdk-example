//
//  DetailedItemView.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 31/10/25.
//
import Foundation
import SwiftUI

enum Direction {
    case vertically, horizontally
}

struct DetailedItemView: View {

    var display: Direction = .horizontally
    var title: String
    var content: String?

    var body: some View {
        if display == .vertically {
            VStack(alignment: .leading, spacing: 5) {
                Text(title)
                    .frame(alignment: .leading)
                    .fontWeight(.semibold)

                Text(content ?? "")
            }
        } else {
            HStack(alignment: .top, spacing: 0) {
                Text(title)
                    .frame(alignment: .leading)
                    .fontWeight(.semibold)
                    .fixedSize()

                Text(content ?? "")
                Spacer()
            }
        }
    }
}
