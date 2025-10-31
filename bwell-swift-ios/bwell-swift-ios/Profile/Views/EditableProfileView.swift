//
//  EditableProfileView.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 30/10/25.
//

import Foundation
import SwiftUI
import BWellSDK

struct EditableProfileView: View {
    @State private var shouldRotate: Bool = false
    // Properties for the editable fields
    @Binding var givenName: String
    @Binding var familyName: String
    @Binding var gender: BWell.Gender
    @Binding var addressLineOne: String
    @Binding var addressLineTwo: String
    @Binding var city: String
    @Binding var state: String
    @Binding var postalCode: String
    @Binding var language: String
    @Binding var selectedBirthdate: Date
    let action: () -> Void

    var body: some View {
        VStack {
            List {
                Section("Personal Information") {
                    ListItem(text: $givenName, placeholder: "Given Name", icon: "person")
                    ListItem(text: $familyName, placeholder: "Family Name", icon: "person.2")
                    genderPickerView
                }.listRowSeparator(.hidden, edges: .top)

                Section("Birth date") {
                    birthDateView
                }.listRowSeparator(.hidden, edges: .top)

                Section("Address") {
                    ListItem(text: $addressLineOne, placeholder: "Address Line 1", icon: "signpost.right.and.left")
                    ListItem(text: $addressLineTwo, placeholder: "Address Line 2", icon: "house")
                    ListItem(text: $city, placeholder: "City", icon: "mappin.and.ellipse")
                    ListItem(text: $state, placeholder: "State", icon: "map")
                    ListItem(text: $postalCode, placeholder: "Postal Code", icon: "numbers.rectangle")
                }.listRowSeparator(.hidden, edges: .top)

                Section("Language") {
                    ListItem(text: $language, placeholder: "Language", icon: "character.book.closed")
                }.listRowSeparator(.hidden, edges: .top)

            }.listStyle(.plain)

            BWellButton(title: "Save", buttonColor: .green) {
                action()
            }
            .padding(.horizontal)
            .padding(.bottom)
        }
    }

    var birthDateView: some View {
        HStack(spacing: 12) {
            Image(systemName: "calendar")
                .frame(width: 24, alignment: .center)

            Text("\(selectedBirthdate.toString())")

            DatePicker("",
                       selection: $selectedBirthdate,
                       in: ...Date(),
                       displayedComponents: .date
            ).datePickerStyle(.compact)
        }
    }

    var genderPickerView: some View {
        HStack(spacing: 12) {
            Image(systemName: "figure.stand.dress.line.vertical.figure")
                .frame(width: 24, alignment: .center)
            Text("\(gender.description())")

            Spacer()

            Menu {
                GenderMenuButton(title: "Male",
                                 genderOption: .male,
                                 currentGender: gender) { gender = .male }

                GenderMenuButton(title: "Female",
                                 genderOption: .female,
                                 currentGender: gender) { gender = .female }

                GenderMenuButton(title: "Other",
                                 genderOption: .other,
                                 currentGender: gender) { gender = .other }
            } label: {
                Image(systemName: "chevron.right.circle.fill")
                    .foregroundStyle(Color(.systemGray4))
                    .frame(width: 10)
            }
        }
    }

    var separatorView: some View {
        Rectangle()
            .fill(.separator)
            .frame(height: 1)
            .padding(.bottom, 20)
    }
}

private struct ListItem: View {
    @Binding var text: String
    var placeholder: String
    var icon: String

    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: icon)
                .frame(width: 24, alignment: .center)

            TextField(placeholder, text: $text)
                .frame(width: 200)

            Spacer()

            if !text.isEmpty {
                Button {
                    text = ""
                } label: {
                    Image(systemName: "plus.circle.fill")
                        .rotationEffect(.degrees(45))
                        .foregroundStyle(Color(.systemGray4))
                        .frame(width: 10)
                }
            }
        }
    }
}

private struct GenderMenuButton: View {
    let title: String
    let genderOption: BWell.Gender
    let currentGender: BWell.Gender
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack {
                Text(title)
                Spacer()
                if currentGender == genderOption {
                    Image(systemName: "checkmark")
                        .foregroundColor(.blue)
                }
            }
        }
    }
}

#Preview {
    EditableProfileView(givenName: .constant("John"),
                        familyName: .constant("Doe"),
                        gender: .constant(.male),
                        addressLineOne: .constant("145 W Osten St"),
                        addressLineTwo:  .constant("Suite 300"),
                        city: .constant("Baltimore"),
                        state: .constant("Maryland"),
                        postalCode: .constant("21230"),
                        language: .constant("en"),
                        selectedBirthdate: .constant(Date.now), action: {
        print("Action tapped in preview")
    })
}
