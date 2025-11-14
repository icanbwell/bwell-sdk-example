//
//  ConnectionsView.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 06/11/25.
//
import Foundation
import SwiftUI

struct ConnectionsView: View {
    var body: some View {
        ZStack {
            List {
                CardView(connection: .insurance)
                CardView(connection: .providers)
                CardView(connection: .clinics)
                CardView(connection: .labs)
            }
            .padding(.top, 10)
            .listStyle(.plain)
            .scrollDisabled(true)
        }
        .toolbarColorScheme(.dark, for: .navigationBar)
        .toolbarBackgroundVisibility(.visible, for: .navigationBar)
        .toolbarBackground(.bwellPurple, for: .navigationBar)
        .toolbar {
            ToolbarItem(placement: .principal) {
                Text("Search connections by")
                    .fontWeight(.medium)
            }
        }
    }
}

private struct CardView: View {
    @EnvironmentObject private var router: NavigationRouter
    var connection: ConnectionsModel

    var body: some View {
        Button {
            router.navigate(to: .searchConnections(connection: connection))
        } label: {
            HStack(alignment: .center) {
                VStack(alignment: .leading, spacing: 10) {
                    HStack {
                        Text(connection.title)
                            .font(.headline)
                            .fontWeight(.medium)
                            .multilineTextAlignment(.leading)

                        Spacer()

                        Image(systemName: connection.icon)
                            .font(.headline)
                            .foregroundStyle(connection.iconColor)

                    }
                    
                    Text(connection.description, tableName: "Localizable")
                        .multilineTextAlignment(.leading)
                        .foregroundStyle(.black)
                }
                Spacer()

                Image(systemName: "chevron.right")
                    .font(.subheadline)
                    .foregroundStyle(.gray)
            }
        }
        .listRowSeparator(.hidden, edges: .all)
    }
}

#Preview {
    ConnectionsView()
}
