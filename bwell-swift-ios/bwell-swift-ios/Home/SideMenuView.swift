//
//  SideMenuView.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 27/10/25.
//

import SwiftUI

struct SideMenuView: View {
    @Binding var isShowing: Bool
    @ObservedObject var viewModel: SideMenuOptionViewModel
    @EnvironmentObject var router: NavigationRouter
    @EnvironmentObject var sdkManager: BWellSDKManager
    
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
                        VStack {
                            ForEach(SideMenuOptionModel.allCases.dropLast()) { option in
                                Button(action: {
                                    viewModel.navigate(to: option)
                                    isShowing = false
                                }, label: {
                                    RowView(option: option,
                                            selectedOption: $viewModel.optionSelected)
                                })
                            }
                        }
                        Spacer()

                        Button(action: {
                            viewModel.navigate(to: .logout)
                            isShowing = false
                        }, label: {
                            RowView(option: .logout,
                                    selectedOption: .constant(.logout))
                        })
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
        .onAppear {
            viewModel.setup(router: router, sdkManager: sdkManager)
            if viewModel.optionSelected == nil {
                viewModel.optionSelected = .home
            }
        }
    }
}

private struct HeaderView: View {
    var body: some View {
        HStack {
            Image("user-profile-picture")
                .resizable()
                .scaledToFill()
                .frame(width: 48, height: 48)
                .clipShape(Circle())
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
                .foregroundStyle(selectedOption == .logout ? .red : .bwellPurple)

            Text(option.title)
                .font(.subheadline)
                .foregroundStyle(selectedOption == .logout ? .red : .black)
            Spacer()
        }
        .padding(.leading)
        .frame(height: 44)
        .background((isSelected && selectedOption != .logout) ? .bwellPurple.opacity(0.25) : .clear)
        .clipShape(RoundedRectangle(cornerRadius: 10))
    }
}


#Preview {
    SideMenuView(isShowing: .constant(true), viewModel: SideMenuOptionViewModel())
        .environmentObject(NavigationRouter())
        .environmentObject(BWellSDKManager.shared)
        .environmentObject(SideMenuOptionViewModel())
}
