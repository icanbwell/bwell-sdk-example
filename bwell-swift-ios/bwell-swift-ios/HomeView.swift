//
//  HomeView.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 10/11/25.
//
import Foundation
import SwiftUI

struct HomeView: View {
    var body: some View {
        ScrollView {
            VStack(alignment: .leading) {
                Text("Welcome, !")
                    .font(.title)
                    .fontWeight(.semibold)
                    .padding(.bottom, 15)
                
                BannerView(title: "First Banner Title",
                           description: "First Banner Description",
                           buttonTitle: "Get Started")
                
                BannerView(title: "Second Banner Title",
                           description: "Second Banner Description",
                           buttonTitle: "Learn More")
                .padding(.bottom, 20)

                Text("Suggested activites")
                    .font(.headline)
                    .padding(.bottom, 10)

                ActivityListItemView(title: "Activity One Title",
                                     description: "Activity One Description",
                                     icon: "personalhotspot",
                                     iconColor: .bwellBlue)

                ActivityListItemView(title: "Activity Two Title",
                                     description: "Activity Two Description",
                                     icon: "cross.fill",
                                     iconColor: .bwellRed)

                ActivityListItemView(title: "Activity Three Title",
                                     description: "Activity Three Description",
                                     icon: "person.fill",
                                     iconColor: .bwellPurple)

                Spacer()
            }.padding(.horizontal)
        }
    }
}

// MARK: Banner View
private struct BannerView: View {
    @State private var shouldClose: Bool = false

    var title: LocalizedStringKey
    var description: LocalizedStringKey
    var buttonTitle: LocalizedStringKey

    var body: some View {
        if !shouldClose {
            VStack(alignment: .leading, spacing: 10) {
                Text(title, tableName: "Home")
                    .font(.title2)
                    .fontWeight(.bold)
                    .foregroundStyle(.white)
                
                Text(description, tableName: "Home")
                    .font(.headline)
                    .foregroundStyle(.white)
                    .padding(.bottom, 10)
                
                Button {
                    print("Banner tapped!")
                } label: {
                    Text(buttonTitle, tableName: "Home")
                        .padding(10)
                        .foregroundStyle(.white)
                        .fontWeight(.medium)
                        .background(.bwellPurple)
                        .clipShape(Capsule())
                }
            }
            .padding()
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(.bwellBlue.opacity(0.7))
            .clipShape(RoundedRectangle(cornerRadius: 15))
            .transition(.asymmetric(insertion: .scale, removal: .opacity))
            .overlay(alignment: .topTrailing) {
                Button {
                    shouldClose = true
                } label: {
                    Image(systemName: "plus.circle.fill")
                        .padding(.top, 10)
                        .padding(.trailing, 5)
                        .rotationEffect(.degrees(45))
                        .foregroundStyle(Color(.systemGray5))
                }
            }
        }
    }
}

// MARK: Suggested Activity List Item
private struct ActivityListItemView: View {
    var title: LocalizedStringKey
    var description: LocalizedStringKey
    var icon: String
    var iconColor: Color

    var body: some View {
        HStack(alignment: .center) {
            HStack(alignment: .firstTextBaseline) {
                Image(systemName: icon)
                    .font(.headline)
                    .foregroundStyle(iconColor)

                VStack(alignment: .leading, spacing: 5) {
                    Text(title, tableName: "Home")
                        .font(.title3)
                        .fontWeight(.medium)

                    Text(description, tableName: "Home")
                }
            }

            Spacer()

            Image(systemName: "chevron.right")
                .foregroundStyle(.gray)
        }.padding()
    }
}

#Preview {
    HomeView()
}
