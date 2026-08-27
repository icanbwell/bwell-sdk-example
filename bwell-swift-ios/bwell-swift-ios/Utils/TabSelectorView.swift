//
//  TabSelectorView.swift
//  bwell-swift-ios
//
//  Reusable segmented tab selector for detail views.
//

import SwiftUI

struct TabSelectorView: View {
    let tabs: [String]
    @Binding var selectedIndex: Int

    var body: some View {
        HStack(spacing: 0) {
            ForEach(Array(tabs.enumerated()), id: \.offset) { index, title in
                Button {
                    withAnimation(.easeInOut(duration: 0.2)) {
                        selectedIndex = index
                    }
                } label: {
                    VStack(spacing: 8) {
                        Text(title)
                            .font(.subheadline)
                            .fontWeight(selectedIndex == index ? .semibold : .regular)
                            .foregroundStyle(selectedIndex == index ? .primary : .secondary)

                        Rectangle()
                            .fill(selectedIndex == index ? Color.bwellPurple : Color.clear)
                            .frame(height: 2)
                    }
                }
                .frame(maxWidth: .infinity)
            }
        }
        .padding(.horizontal)
    }
}
