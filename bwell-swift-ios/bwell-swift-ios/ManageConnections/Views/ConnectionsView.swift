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
    var connection: ConnectionsModel

    var body: some View {
        NavigationLink(value: AppView.searchConnections(connection: connection)) {
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
            }
        }
        .listRowSeparator(.hidden, edges: .all)
    }
}

#Preview {
    ConnectionsView()
}
