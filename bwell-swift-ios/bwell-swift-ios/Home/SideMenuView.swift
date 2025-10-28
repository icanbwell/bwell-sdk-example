//
//  SideMenuView.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 27/10/25.
//

import SwiftUI

struct SideMenuView: View {
    @Binding var isShowing: Bool
    @State private var selectedOpton: SideMenuOptionModel? = .home

    var body: some View {
        ZStack {
            if isShowing {
                Rectangle()
                    .opacity(0.3)
                    .ignoresSafeArea()
                    .onTapGesture { isShowing.toggle() }

                HStack {
                    VStack(alignment: .leading, spacing: 32) {
                        HeaderView()
                        // ScrollView {
                            VStack {
                                ForEach(SideMenuOptionModel.allCases.dropLast()) { option in
                                    Button(action: {
                                        selectedOpton = option
                                        print("\(option.title) pressed")
                                    }, label: {
                                        RowView(option: option, selectedOption: $selectedOpton)
                                    })
                                }
                            }
                        // }.scrollIndicators(.hidden)
                        Spacer()

                        RowView(option: .logout, selectedOption: .constant(.logout))
                    }
                    .padding()
                    .frame(width: 270, alignment: .leading)
                    .background(.white)

                    Spacer()
                }
            }
        }
        .transition(.move(edge: .leading))
        .animation(.easeInOut, value: isShowing)
    }
}

private struct HeaderView: View {
    var body: some View {
        HStack {
            Image(systemName: "person.circle.fill")
                .imageScale(.large)
                .foregroundStyle(.white)
                .frame(width: 48, height: 48)
                .background(.bwellPurple)
                .clipShape(RoundedRectangle(cornerRadius: 10))
                .padding(.vertical)

            VStack(alignment: .leading, spacing: 4) {
                Text("Sample App User")
                    .fontWeight(.semibold)
                    .font(.subheadline)

                Text("test@test.test")
                    .font(.footnote)
                    .tint(.gray)
            }
        }
    }
}

private struct RowView: View {
    var option: SideMenuOptionModel
    @Binding var selectedOption: SideMenuOptionModel?

    private var isSelected: Bool {
        return selectedOption == option
    }

    var body: some View {
        HStack {
            Image(systemName: option.iconName)
                .imageScale(.small)

            Text(option.title)
                .font(.subheadline)

            Spacer()
        }
        .padding(.leading)
        .frame(height: 44)
        .foregroundStyle((isSelected && selectedOption != .logout) ? .bwellPurple : .black)
        .background((isSelected && selectedOption != .logout) ? .bwellPurple.opacity(0.25) : .clear)
        .clipShape(RoundedRectangle(cornerRadius: 10))
    }
}


#Preview {
    SideMenuView(isShowing: .constant(true))
}
