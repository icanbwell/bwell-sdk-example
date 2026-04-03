//
//  ProviderResourcesView.swift
//  bwell-swift-ios
//
//  Segmented view for Practitioners | Organizations | Locations | Roles
//  using sdk.provider methods.
//

import SwiftUI
import BWell

struct ProviderResourcesView: View {
    @EnvironmentObject private var sdkManager: SDKManager
    @State private var selectedTab: ProviderTab = .practitioners
    @State private var lookupIds = ""
    @State private var showLookupSheet = false

    // Practitioners
    @State private var practitioners: [BWell.Practitioner] = []
    @State private var isPractitionersLoading = false

    // Organizations
    @State private var organizations: [BWell.Organization] = []
    @State private var isOrganizationsLoading = false

    // Locations
    @State private var locations: [BWell.Location] = []
    @State private var isLocationsLoading = false

    // Practitioner Roles
    @State private var practitionerRoles: [BWell.PractitionerRole] = []
    @State private var isRolesLoading = false

    @State private var errorMessage: String?

    enum ProviderTab: String, CaseIterable {
        case practitioners = "Practitioners"
        case organizations = "Organizations"
        case locations = "Locations"
        case roles = "Roles"
    }

    var body: some View {
        VStack(spacing: 0) {
            Picker("Resource Type", selection: $selectedTab) {
                ForEach(ProviderTab.allCases, id: \.self) { tab in
                    Text(tab.rawValue).tag(tab)
                }
            }
            .pickerStyle(.segmented)
            .padding()

            if let error = errorMessage {
                HStack {
                    Image(systemName: "exclamationmark.triangle.fill")
                        .foregroundStyle(.orange)
                    Text(error)
                        .font(.caption)
                        .foregroundStyle(.red)
                }
                .padding(.horizontal)
            }

            switch selectedTab {
            case .practitioners:
                practitionersList
            case .organizations:
                organizationsList
            case .locations:
                locationsList
            case .roles:
                rolesList
            }
        }
        .navigationTitle("Provider Resources")
        .navigationBarTitleDisplayMode(.inline)
        .toolbarColorScheme(.dark, for: .navigationBar)
        .toolbarBackgroundVisibility(.visible, for: .navigationBar)
        .toolbarBackground(.bwellPurple, for: .navigationBar)
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Button {
                    showLookupSheet = true
                } label: {
                    Image(systemName: "magnifyingglass")
                }
            }
        }
        .sheet(isPresented: $showLookupSheet) {
            lookupSheet
        }
    }

    // MARK: - Lookup Sheet

    private var lookupSheet: some View {
        NavigationStack {
            Form {
                Section("Lookup by IDs") {
                    TextField("Enter IDs (comma-separated)", text: $lookupIds)
                        .autocapitalization(.none)
                }

                Section {
                    Button("Look Up") {
                        showLookupSheet = false
                        let ids = lookupIds.split(separator: ",").map { String($0.trimmingCharacters(in: .whitespaces)) }.filter { !$0.isEmpty }
                        guard !ids.isEmpty else { return }
                        Task { await lookupResources(ids: ids) }
                    }
                    .disabled(lookupIds.trimmingCharacters(in: .whitespaces).isEmpty)
                }
            }
            .navigationTitle("Look Up \(selectedTab.rawValue)")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { showLookupSheet = false }
                }
            }
        }
        .presentationDetents([.medium])
    }

    private func lookupResources(ids: [String]) async {
        guard let sdk = sdkManager.sdk else { return }
        errorMessage = nil
        do {
            switch selectedTab {
            case .practitioners:
                isPractitionersLoading = true
                let response = try await sdk.provider.getPractitioners(BWell.PractitionersRequest(ids: ids))
                practitioners = response.resources
                isPractitionersLoading = false
            case .organizations:
                isOrganizationsLoading = true
                let response = try await sdk.provider.getOrganizations(BWell.OrganizationsRequest(ids: ids))
                organizations = response.resources
                isOrganizationsLoading = false
            case .locations:
                isLocationsLoading = true
                let response = try await sdk.provider.getLocations(BWell.LocationsRequest(ids: ids))
                locations = response.resources
                isLocationsLoading = false
            case .roles:
                isRolesLoading = true
                let response = try await sdk.provider.getPractitionerRoles(BWell.PractitionerRolesRequest(ids: ids))
                practitionerRoles = response.resources
                isRolesLoading = false
            }
        } catch {
            errorMessage = "Lookup failed: \(error.localizedDescription)"
            isPractitionersLoading = false
            isOrganizationsLoading = false
            isLocationsLoading = false
            isRolesLoading = false
        }
    }

    // MARK: - Practitioners

    private var practitionersList: some View {
        Group {
            if isPractitionersLoading {
                ProgressView("Loading...")
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if practitioners.isEmpty {
                ContentUnavailableView("No Practitioners",
                    systemImage: "person.text.rectangle",
                    description: Text("Use the search button to look up practitioners by ID."))
            } else {
                List(practitioners, id: \.id) { practitioner in
                    VStack(alignment: .leading, spacing: 6) {
                        Text(practitionerName(practitioner))
                            .font(.body)
                            .fontWeight(.medium)

                        if let id = practitioner.id {
                            Text("ID: \(id)")
                                .font(.caption)
                                .foregroundStyle(.secondary)
                        }

                        if let identifiers = practitioner.identifier, !identifiers.isEmpty {
                            ForEach(identifiers.indices, id: \.self) { i in
                                let ident = identifiers[i]
                                if let value = ident.value {
                                    HStack(spacing: 4) {
                                        Text(ident.type?.text ?? "Identifier:")
                                            .font(.caption)
                                            .foregroundStyle(.secondary)
                                        Text(value)
                                            .font(.caption)
                                    }
                                }
                            }
                        }
                    }
                    .padding(.vertical, 4)
                }
                .listStyle(.plain)
            }
        }
    }

    private func practitionerName(_ p: BWell.Practitioner) -> String {
        guard let name = p.name?.first else { return "Unknown Practitioner" }
        let given = name.given?.joined(separator: " ") ?? ""
        let family = name.family ?? ""
        let prefix = name.prefix?.first ?? ""
        let suffix = name.suffix?.first ?? ""
        let parts = [prefix, given, family, suffix].filter { !$0.isEmpty }
        return parts.isEmpty ? "Unknown Practitioner" : parts.joined(separator: " ")
    }

    // MARK: - Organizations

    private var organizationsList: some View {
        Group {
            if isOrganizationsLoading {
                ProgressView("Loading...")
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if organizations.isEmpty {
                ContentUnavailableView("No Organizations",
                    systemImage: "building.2",
                    description: Text("Use the search button to look up organizations by ID."))
            } else {
                List(organizations, id: \.id) { org in
                    VStack(alignment: .leading, spacing: 6) {
                        Text(org.name ?? "Unknown Organization")
                            .font(.body)
                            .fontWeight(.medium)

                        if let active = org.active {
                            Text(active ? "Active" : "Inactive")
                                .font(.caption)
                                .fontWeight(.medium)
                                .padding(.horizontal, 8)
                                .padding(.vertical, 3)
                                .background((active ? Color.green : Color.gray).opacity(0.15))
                                .foregroundStyle(active ? .green : .gray)
                                .clipShape(Capsule())
                        }

                        if let type = org.type?.first?.text ?? org.type?.first?.coding?.first?.display {
                            HStack(spacing: 4) {
                                Text("Type:")
                                    .font(.caption)
                                    .foregroundStyle(.secondary)
                                Text(type)
                                    .font(.caption)
                            }
                        }

                        if let address = org.address?.first {
                            let parts = [address.line?.first, address.city, address.state, address.postalCode].compactMap { $0 }
                            if !parts.isEmpty {
                                HStack(spacing: 4) {
                                    Image(systemName: "mappin")
                                        .font(.caption)
                                        .foregroundStyle(.secondary)
                                    Text(parts.joined(separator: ", "))
                                        .font(.caption)
                                        .foregroundStyle(.secondary)
                                }
                            }
                        }

                        if let telecoms = org.telecom, !telecoms.isEmpty {
                            ForEach(telecoms.indices, id: \.self) { i in
                                let t = telecoms[i]
                                if let value = t.value {
                                    HStack(spacing: 4) {
                                        Image(systemName: t.system == "phone" ? "phone" : "envelope")
                                            .font(.caption)
                                            .foregroundStyle(.secondary)
                                        Text(value)
                                            .font(.caption)
                                    }
                                }
                            }
                        }
                    }
                    .padding(.vertical, 4)
                }
                .listStyle(.plain)
            }
        }
    }

    // MARK: - Locations

    private var locationsList: some View {
        Group {
            if isLocationsLoading {
                ProgressView("Loading...")
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if locations.isEmpty {
                ContentUnavailableView("No Locations",
                    systemImage: "mappin.and.ellipse",
                    description: Text("Use the search button to look up locations by ID."))
            } else {
                List(locations, id: \.id) { location in
                    VStack(alignment: .leading, spacing: 6) {
                        Text(location.name ?? "Unknown Location")
                            .font(.body)
                            .fontWeight(.medium)

                        if let status = location.status {
                            Text(status.capitalized)
                                .font(.caption)
                                .fontWeight(.medium)
                                .padding(.horizontal, 8)
                                .padding(.vertical, 3)
                                .background(locationStatusColor(status).opacity(0.15))
                                .foregroundStyle(locationStatusColor(status))
                                .clipShape(Capsule())
                        }

                        if let desc = location.description {
                            Text(desc)
                                .font(.caption)
                                .foregroundStyle(.secondary)
                                .lineLimit(2)
                        }

                        if let address = location.address {
                            let parts = [address.line?.first, address.city, address.state, address.postalCode].compactMap { $0 }
                            if !parts.isEmpty {
                                HStack(spacing: 4) {
                                    Image(systemName: "mappin")
                                        .font(.caption)
                                        .foregroundStyle(.secondary)
                                    Text(parts.joined(separator: ", "))
                                        .font(.caption)
                                        .foregroundStyle(.secondary)
                                }
                            }
                        }

                        if let type = location.type?.first?.text ?? location.type?.first?.coding?.first?.display {
                            HStack(spacing: 4) {
                                Text("Type:")
                                    .font(.caption)
                                    .foregroundStyle(.secondary)
                                Text(type)
                                    .font(.caption)
                            }
                        }

                        if let telecoms = location.telecom, !telecoms.isEmpty {
                            ForEach(telecoms.indices, id: \.self) { i in
                                let t = telecoms[i]
                                if let value = t.value {
                                    HStack(spacing: 4) {
                                        Image(systemName: t.system == "phone" ? "phone" : "envelope")
                                            .font(.caption)
                                            .foregroundStyle(.secondary)
                                        Text(value)
                                            .font(.caption)
                                    }
                                }
                            }
                        }
                    }
                    .padding(.vertical, 4)
                }
                .listStyle(.plain)
            }
        }
    }

    private func locationStatusColor(_ status: String) -> Color {
        switch status.lowercased() {
        case "active": return .green
        case "suspended": return .orange
        case "inactive": return .red
        default: return .gray
        }
    }

    // MARK: - Practitioner Roles

    private var rolesList: some View {
        Group {
            if isRolesLoading {
                ProgressView("Loading...")
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if practitionerRoles.isEmpty {
                ContentUnavailableView("No Practitioner Roles",
                    systemImage: "person.badge.key",
                    description: Text("Use the search button to look up practitioner roles by ID."))
            } else {
                List(practitionerRoles, id: \.id) { role in
                    VStack(alignment: .leading, spacing: 6) {
                        // Practitioner name
                        if let practitioner = role.practitioner?.display {
                            Text(practitioner)
                                .font(.body)
                                .fontWeight(.medium)
                        } else {
                            Text("Practitioner Role")
                                .font(.body)
                                .fontWeight(.medium)
                        }

                        // Organization
                        if let org = role.organization?.display {
                            HStack(spacing: 4) {
                                Image(systemName: "building.2")
                                    .font(.caption)
                                    .foregroundStyle(.secondary)
                                Text(org)
                                    .font(.caption)
                            }
                        }

                        // Active status
                        if let active = role.active {
                            Text(active ? "Active" : "Inactive")
                                .font(.caption)
                                .fontWeight(.medium)
                                .padding(.horizontal, 8)
                                .padding(.vertical, 3)
                                .background((active ? Color.green : Color.gray).opacity(0.15))
                                .foregroundStyle(active ? .green : .gray)
                                .clipShape(Capsule())
                        }

                        // Specialties
                        if let specialties = role.specialty, !specialties.isEmpty {
                            let tags = specialties.compactMap { $0.text ?? $0.coding?.first?.display }
                            if !tags.isEmpty {
                                FlowLayout(spacing: 4) {
                                    ForEach(tags, id: \.self) { tag in
                                        Text(tag)
                                            .font(.caption2)
                                            .padding(.horizontal, 8)
                                            .padding(.vertical, 3)
                                            .background(Color.bwellPurple.opacity(0.1))
                                            .foregroundStyle(.bwellPurple)
                                            .clipShape(Capsule())
                                    }
                                }
                            }
                        }

                        // Codes / Roles
                        if let codes = role.code, !codes.isEmpty {
                            let roleNames = codes.compactMap { $0.text ?? $0.coding?.first?.display }
                            if !roleNames.isEmpty {
                                HStack(spacing: 4) {
                                    Text("Role:")
                                        .font(.caption)
                                        .foregroundStyle(.secondary)
                                    Text(roleNames.joined(separator: ", "))
                                        .font(.caption)
                                }
                            }
                        }

                        // Period
                        if let period = role.period {
                            HStack(spacing: 4) {
                                Text("Period:")
                                    .font(.caption)
                                    .foregroundStyle(.secondary)
                                if let start = period.start {
                                    Text(start.dateFormatter())
                                        .font(.caption)
                                }
                                if let end = period.end {
                                    Text("- \(end.dateFormatter())")
                                        .font(.caption)
                                }
                            }
                        }

                        // Locations
                        if let locs = role.location, !locs.isEmpty {
                            let locNames = locs.compactMap { $0.display }
                            if !locNames.isEmpty {
                                HStack(spacing: 4) {
                                    Image(systemName: "mappin")
                                        .font(.caption)
                                        .foregroundStyle(.secondary)
                                    Text(locNames.joined(separator: ", "))
                                        .font(.caption)
                                        .foregroundStyle(.secondary)
                                }
                            }
                        }

                        // Telecom
                        if let telecoms = role.telecom, !telecoms.isEmpty {
                            ForEach(telecoms.indices, id: \.self) { i in
                                let t = telecoms[i]
                                if let value = t.value {
                                    HStack(spacing: 4) {
                                        Image(systemName: t.system == "phone" ? "phone" : "envelope")
                                            .font(.caption)
                                            .foregroundStyle(.secondary)
                                        Text(value)
                                            .font(.caption)
                                    }
                                }
                            }
                        }
                    }
                    .padding(.vertical, 4)
                }
                .listStyle(.plain)
            }
        }
    }
}

// MARK: - Flow Layout for tags

private struct FlowLayout: Layout {
    var spacing: CGFloat = 4

    func sizeThatFits(proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) -> CGSize {
        let result = layout(proposal: proposal, subviews: subviews)
        return result.size
    }

    func placeSubviews(in bounds: CGRect, proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) {
        let result = layout(proposal: proposal, subviews: subviews)
        for (index, position) in result.positions.enumerated() {
            subviews[index].place(at: CGPoint(x: bounds.minX + position.x, y: bounds.minY + position.y), proposal: .unspecified)
        }
    }

    private func layout(proposal: ProposedViewSize, subviews: Subviews) -> (size: CGSize, positions: [CGPoint]) {
        let maxWidth = proposal.width ?? .infinity
        var positions: [CGPoint] = []
        var x: CGFloat = 0
        var y: CGFloat = 0
        var lineHeight: CGFloat = 0
        var totalHeight: CGFloat = 0

        for subview in subviews {
            let size = subview.sizeThatFits(.unspecified)
            if x + size.width > maxWidth && x > 0 {
                x = 0
                y += lineHeight + spacing
                lineHeight = 0
            }
            positions.append(CGPoint(x: x, y: y))
            x += size.width + spacing
            lineHeight = max(lineHeight, size.height)
            totalHeight = y + lineHeight
        }

        return (CGSize(width: maxWidth, height: totalHeight), positions)
    }
}
