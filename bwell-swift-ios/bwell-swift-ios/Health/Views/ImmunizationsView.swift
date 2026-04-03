//
//  ImmunizationsView.swift
//  bwell-swift-ios
//
//  Immunizations organized chronologically with expandable detail.
//  Shows vaccine name, site, status badge, date, and administration details.
//

import SwiftUI
import BWell

struct ImmunizationsView: View {
    @EnvironmentObject private var viewModel: HealthSummaryViewModel
    @EnvironmentObject private var sdkManager: SDKManager
    @State private var immunizations: [BWell.Immunization] = []
    @State private var isLoading = false
    @State private var hasFetched = false
    @State private var expandedIds: Set<String> = []

    private var groupedByDate: [(String, [BWell.Immunization])] {
        let grouped = Dictionary(grouping: immunizations) { imm -> String in
            if let dt = imm.occurrenceDateTime, let date = parseFHIRDate(dt) {
                let formatter = DateFormatter()
                formatter.dateFormat = "MMMM yyyy"
                return formatter.string(from: date)
            }
            return "Unknown Date"
        }
        return grouped.sorted { pair1, pair2 in
            let date1 = pair1.1.first?.occurrenceDateTime ?? ""
            let date2 = pair2.1.first?.occurrenceDateTime ?? ""
            return date1 > date2
        }
    }

    var body: some View {
        List {
            if isLoading && immunizations.isEmpty {
                ForEach(0..<5, id: \.self) { _ in
                    SkeletonRow()
                }
                .listRowSeparator(.hidden)
            } else if !isLoading && immunizations.isEmpty && hasFetched {
                ContentUnavailableView("No Immunizations",
                    systemImage: "syringe",
                    description: Text("No immunization records found."))
            } else {
                ForEach(groupedByDate, id: \.0) { month, imms in
                    Section(header: Text(month)
                        .font(.subheadline)
                        .fontWeight(.semibold)
                    ) {
                        ForEach(imms, id: \.id) { imm in
                            ImmunizationRow(
                                immunization: imm,
                                isExpanded: expandedIds.contains(imm.id),
                                onToggle: { toggleExpanded(imm.id) }
                            )
                        }
                    }
                }
            }
        }
        .listStyle(.plain)
        .navigationTitle("Immunizations")
        .toolbarColorScheme(.dark, for: .navigationBar)
        .toolbarBackgroundVisibility(.visible, for: .navigationBar)
        .toolbarBackground(.bwellPurple, for: .navigationBar)
        .refreshable { await fetchAll() }
        .task {
            if !hasFetched { await fetchAll() }
        }
    }

    private func toggleExpanded(_ id: String) {
        withAnimation(.easeInOut(duration: 0.25)) {
            if expandedIds.contains(id) {
                expandedIds.remove(id)
            } else {
                expandedIds.insert(id)
            }
        }
    }

    private func fetchAll() async {
        guard let sdk = sdkManager.sdk else { return }
        isLoading = true
        do {
            let request = BWell.HealthDataRequest(page: 0, pageSize: 100)
            let response = try await sdk.health.getImmunizations(request)
            let all = response.entry?.compactMap { $0.resource } ?? []
            immunizations = all.sorted {
                ($0.occurrenceDateTime ?? "") > ($1.occurrenceDateTime ?? "")
            }
        } catch {
            NSLog("[Immunizations] Error: %@", error.localizedDescription)
            immunizations = []
        }
        isLoading = false
        hasFetched = true
    }

    private func parseFHIRDate(_ string: String) -> Date? {
        let formats = ["yyyy-MM-dd'T'HH:mm:ssZ", "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
                       "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd"]
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "en_US_POSIX")
        for format in formats {
            formatter.dateFormat = format
            if let date = formatter.date(from: string) { return date }
        }
        return nil
    }
}

// MARK: - Immunization Row

private struct ImmunizationRow: View {
    let immunization: BWell.Immunization
    let isExpanded: Bool
    let onToggle: () -> Void

    private var vaccineName: String {
        immunization.vaccineCode?.coding?.first?.display
            ?? immunization.vaccineCode?.text
            ?? "Vaccine"
    }

    private var siteText: String? {
        immunization.site?.text ?? immunization.site?.coding?.first?.display
    }

    private var statusText: String {
        immunization.status?.capitalizingFirstLetter() ?? "Unknown"
    }

    private var dateText: String? {
        immunization.occurrenceDateTime?.dateFormatter()
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Button(action: onToggle) {
                HStack(alignment: .center, spacing: 10) {
                    Image(systemName: "syringe.fill")
                        .font(.title3)
                        .foregroundStyle(.bwellPurple)
                        .frame(width: 28)

                    VStack(alignment: .leading, spacing: 3) {
                        Text(vaccineName)
                            .font(.body)
                            .fontWeight(.medium)
                            .foregroundStyle(.primary)
                            .lineLimit(2)
                            .multilineTextAlignment(.leading)

                        if let site = siteText {
                            Text(site)
                                .font(.caption)
                                .foregroundStyle(.secondary)
                                .lineLimit(1)
                        }
                    }

                    Spacer()

                    VStack(alignment: .trailing, spacing: 3) {
                        Text(statusText)
                            .font(.caption2)
                            .fontWeight(.medium)
                            .padding(.horizontal, 8)
                            .padding(.vertical, 3)
                            .background(statusColor.opacity(0.12))
                            .foregroundStyle(statusColor)
                            .clipShape(Capsule())

                        if let date = dateText {
                            Text(date)
                                .font(.caption2)
                                .foregroundStyle(.tertiary)
                        }
                    }

                    Image(systemName: isExpanded ? "chevron.down" : "chevron.right")
                        .font(.caption2)
                        .fontWeight(.semibold)
                        .foregroundStyle(.tertiary)
                }
            }
            .buttonStyle(.plain)

            if isExpanded {
                Divider().padding(.top, 8)
                ImmunizationDetailContent(immunization: immunization)
                    .padding(.top, 6)
                    .padding(.leading, 38)
                    .transition(.opacity.combined(with: .move(edge: .top)))
            }
        }
        .padding(.vertical, 6)
    }

    private var statusColor: Color {
        switch immunization.status?.lowercased() {
        case "completed": return .green
        case "entered-in-error": return .red
        case "not-done": return .orange
        default: return .gray
        }
    }
}

// MARK: - Immunization Detail (Expanded)

private struct ImmunizationDetailContent: View {
    let immunization: BWell.Immunization

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            detailRow("Status", immunization.status?.capitalizingFirstLetter())
            detailRow("Date", immunization.occurrenceDateTime?.dateFormatter())
            detailRow("Site", immunization.site?.text ?? immunization.site?.coding?.first?.display)
            detailRow("Route", immunization.route?.text ?? immunization.route?.coding?.first?.display)
            detailRow("Lot #", immunization.lotNumber)
            detailRow("Expiration", immunization.expirationDate?.dateFormatter())

            if let dose = immunization.doseQuantity {
                let value = dose.value.map { String(format: "%.1f", $0) } ?? ""
                let unit = dose.unit ?? ""
                detailRow("Dose", "\(value) \(unit)".trimmingCharacters(in: .whitespaces))
            }

            if let manufacturer = immunization.manufacturer {
                detailRow("Manufacturer", manufacturer.display)
            }

            if let performers = immunization.performer, !performers.isEmpty {
                let performerNames = performers.compactMap { $0.actor?.display }.filter { !$0.isEmpty }
                if !performerNames.isEmpty {
                    detailRow("Performer", performerNames.joined(separator: "; "))
                }
            }

            if let reasons = immunization.reasonCode, !reasons.isEmpty {
                let texts = reasons.compactMap { $0.text ?? $0.coding?.first?.display }
                if !texts.isEmpty {
                    detailRow("Reason", texts.joined(separator: "; "))
                }
            }

            if let statusReason = immunization.statusReason {
                detailRow("Status Reason", statusReason.text ?? statusReason.coding?.first?.display)
            }

            if let protoList = immunization.protocolApplied, !protoList.isEmpty {
                let doseStr = protoList.compactMap { $0.doseNumberString }.joined(separator: ", ")
                if !doseStr.isEmpty { detailRow("Dose", doseStr) }
                if let doseNum = protoList.first?.doseNumberPositiveInt {
                    detailRow("Dose #", "\(doseNum)")
                }
            }

            if let reactions = immunization.reaction, !reactions.isEmpty {
                let reactionDates = reactions.compactMap { $0.date?.dateFormatter() }
                if !reactionDates.isEmpty {
                    detailRow("Reaction", reactionDates.joined(separator: "; "))
                }
            }

            if let code = immunization.vaccineCode?.coding?.first {
                if let system = code.system, let codeVal = code.code {
                    let shortSystem = system.components(separatedBy: "/").last ?? system
                    detailRow("Code", "\(shortSystem): \(codeVal)")
                }
            }
        }
    }

    @ViewBuilder
    private func detailRow(_ label: String, _ value: String?) -> some View {
        if let value, !value.isEmpty {
            HStack(alignment: .top, spacing: 6) {
                Text(label)
                    .font(.caption)
                    .foregroundStyle(.secondary)
                    .frame(width: 85, alignment: .trailing)
                Text(value)
                    .font(.caption)
                    .foregroundStyle(.primary)
                    .fixedSize(horizontal: false, vertical: true)
            }
        }
    }
}

struct ImmunizationSheetView: View {
    var immunization: BWellWrapper.immunization

    var body: some View {
        VStack(alignment: .leading, spacing: 5) {
            Text(immunization.vaccineCode?.coding?.first?.display ?? immunization.vaccineCode?.text ?? "Immunization")
                .font(.headline)
                .fontWeight(.medium)
                .padding(.vertical, 20)

            VStack(alignment: .leading, spacing: 5) {
                DetailedItemView(title: "Occurrence date: ", content: immunization.occurrenceDateTime?.dateFormatter())
                DetailedItemView(title: "Expiration date: ", content: immunization.expirationDate?.dateFormatter())
            }.padding(.bottom, 10)

            DetailedItemView(title: "Status: ", content: immunization.status)
            DetailedItemView(title: "Site: ", content: immunization.site?.text ?? immunization.site?.coding?.first?.display)
            DetailedItemView(title: "Administration route: ", content: immunization.route?.text)
            DetailedItemView(title: "Lot number: ", content: immunization.lotNumber)
            Spacer()
        }
        .padding()
        .presentationDragIndicator(.visible)
        .presentationDetents([.medium])
    }
}
