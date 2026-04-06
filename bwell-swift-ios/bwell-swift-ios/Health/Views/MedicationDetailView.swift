//
//  MedicationDetailView.swift
//  bwell-swift-ios
//
//  Three-tab detail view: Overview, Knowledge, Pricing.
//

import SwiftUI
import BWellSDK

struct MedicationDetailView: View {
    let medication: BWell.MedicationStatement
    @EnvironmentObject private var sdkManager: SDKManager
    @State private var selectedTab = 0

    // Knowledge
    @State private var knowledgeHTML: String?
    @State private var isLoadingKnowledge = false

    // Pricing
    @State private var pricingHTML: String?
    @State private var isLoadingPricing = false

    // Related data
    @State private var medicationRequests: [BWell.MedicationRequest] = []
    @State private var medicationDispenses: [BWell.MedicationDispense] = []

    var body: some View {
        VStack(spacing: 0) {
            TabSelectorView(
                tabs: ["Overview", "What is it?", "Pricing"],
                selectedIndex: $selectedTab
            )
            .padding(.top, 8)

            TabView(selection: $selectedTab) {
                overviewTab.tag(0)
                knowledgeTab.tag(1)
                pricingTab.tag(2)
            }
            .tabViewStyle(.page(indexDisplayMode: .never))
        }
        .navigationTitle(medication.medicationCodeableConcept?.coding?.first?.display ?? medication.medicationCodeableConcept?.text ?? "Medication Detail")
        .toolbarColorScheme(.dark, for: .navigationBar)
        .toolbarBackgroundVisibility(.visible, for: .navigationBar)
        .toolbarBackground(.bwellPurple, for: .navigationBar)
    }

    // MARK: - Overview Tab

    @ViewBuilder
    private var overviewTab: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 12) {
                Text(medication.medicationCodeableConcept?.coding?.first?.display ?? medication.medicationCodeableConcept?.text ?? "Title unavailable")
                    .font(.headline)
                    .fontWeight(.medium)
                    .padding(.top, 16)

                DetailedItemView(title: "Status: ", content: medication.status)
                DetailedItemView(title: "Period start: ",
                                 content: medication.effectivePeriod?.start?.dateFormatter())
                DetailedItemView(title: "Period end: ",
                                 content: medication.effectivePeriod?.end?.dateFormatter())

                if let reasonCode = medication.reasonCode, !reasonCode.isEmpty {
                    ForEach(reasonCode, id: \.id) { reason in
                        DetailedItemView(title: "Reason: ", content: reason.text ?? reason.coding?.first?.display)
                    }
                }

                if let dosage = medication.dosage, !dosage.isEmpty {
                    ForEach(dosage, id: \.id) { dose in
                        DetailedItemView(title: "Dosage: ", content: dose.text)
                    }
                }

                // Medication Requests section
                if !medicationRequests.isEmpty {
                    sectionHeader("Prescriptions")
                    ForEach(medicationRequests, id: \.id) { request in
                        prescriptionCard(request)
                    }
                }

                // Medication Dispenses section
                if !medicationDispenses.isEmpty {
                    sectionHeader("Dispenses")
                    ForEach(medicationDispenses, id: \.id) { dispense in
                        dispenseCard(dispense)
                    }
                }
            }
            .padding(.horizontal)
        }
        .task {
            await loadRelatedData()
        }
    }

    // MARK: - Knowledge Tab

    @ViewBuilder
    private var knowledgeTab: some View {
        Group {
            if isLoadingKnowledge {
                ProgressView("Loading knowledge...")
            } else if let html = knowledgeHTML {
                HTMLContentView(htmlContent: html)
            } else {
                emptyState(icon: "doc.text.magnifyingglass",
                           message: "No knowledge content available for this medication.")
            }
        }
        .task {
            guard knowledgeHTML == nil, !isLoadingKnowledge else { return }
            await loadKnowledge()
        }
    }

    // MARK: - Pricing Tab

    @ViewBuilder
    private var pricingTab: some View {
        Group {
            if isLoadingPricing {
                ProgressView("Loading pricing...")
            } else if let html = pricingHTML {
                HTMLContentView(htmlContent: html)
            } else {
                emptyState(icon: "dollarsign.circle",
                           message: "No pricing information available for this medication.")
            }
        }
        .task {
            guard pricingHTML == nil, !isLoadingPricing else { return }
            await loadPricing()
        }
    }

    // MARK: - Helpers

    @ViewBuilder
    private func sectionHeader(_ title: String) -> some View {
        Text(title)
            .font(.title3)
            .fontWeight(.semibold)
            .padding(.top, 12)
    }

    @ViewBuilder
    private func prescriptionCard(_ request: BWell.MedicationRequest) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            DetailedItemView(title: "Status: ", content: request.status)
            DetailedItemView(title: "Authored: ", content: request.authoredOn?.dateFormatter())
            if let dosageInstructions = request.dosageInstruction {
                ForEach(dosageInstructions, id: \.id) { instruction in
                    DetailedItemView(title: "Instructions: ", content: instruction.text)
                }
            }
        }
        .padding()
        .background(Color(.systemGray6))
        .clipShape(RoundedRectangle(cornerRadius: 10))
    }

    @ViewBuilder
    private func dispenseCard(_ dispense: BWell.MedicationDispense) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            DetailedItemView(title: "Status: ", content: dispense.status)
            if let quantity = dispense.quantity {
                DetailedItemView(title: "Quantity: ",
                                 content: "\(quantity.value ?? 0) \(quantity.unit ?? "")")
            }
            if let daysSupply = dispense.daysSupply {
                DetailedItemView(title: "Days supply: ",
                                 content: "\(daysSupply.value ?? 0)")
            }
            DetailedItemView(title: "When prepared: ",
                             content: dispense.whenPrepared?.dateFormatter())
        }
        .padding()
        .background(Color(.systemGray6))
        .clipShape(RoundedRectangle(cornerRadius: 10))
    }

    @ViewBuilder
    private func emptyState(icon: String, message: String) -> some View {
        VStack(spacing: 12) {
            Image(systemName: icon)
                .font(.largeTitle)
                .foregroundStyle(.secondary)
            Text(message)
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.center)
        }
        .padding()
    }

    // MARK: - Data Loading

    private func loadRelatedData() async {
        guard let sdk = sdkManager.sdk else { return }

        // Load medication requests
        if let code = medication.medicationCodeableConcept?.coding?.first {
            let searchToken = BWell.SearchToken.Value(system: code.system, code: code.code)
            let request = BWell.MedicationRequestRequest(medicationCode: .init(value: searchToken))
            if let response = try? await sdk.health.getMedicationRequest(request) {
                medicationRequests = response.entry?.compactMap { $0.resource } ?? []
            }
        }

        // Load medication dispenses
        if let medId = medication.id {
            let request = BWell.MedicationDispenseRequest(prescription: medId)
            if let response = try? await sdk.health.getMedicationDispense(request) {
                medicationDispenses = response.entry?.compactMap { $0.resource } ?? []
            }
        }
    }

    private func loadKnowledge() async {
        guard let sdk = sdkManager.sdk, let medId = medication.id else { return }
        isLoadingKnowledge = true
        let request = BWell.MedicationKnowledgeRequest(medicationStatementId: medId)
        if let response = try? await sdk.health.getMedicationKnowledge(request) {
            knowledgeHTML = response.content
        }
        isLoadingKnowledge = false
    }

    private func loadPricing() async {
        guard let sdk = sdkManager.sdk, let medId = medication.id else { return }
        isLoadingPricing = true
        let request = BWell.MedicationPricingRequest(medicationStatementId: medId)
        if let response = try? await sdk.health.getMedicationPricing(request) {
            // Build pricing HTML from resources
            if let resources = response.resources, let first = resources.first {
                pricingHTML = buildPricingHTML(first)
            }
        }
        isLoadingPricing = false
    }

    private func buildPricingHTML(_ pricing: BWell.MedicationPricing) -> String {
        var html = "<h3>Medication Pricing</h3>"
        if let pharmacy = pricing.pharmacy {
            html += "<p><strong>Pharmacy:</strong> \(pharmacy)</p>"
        }
        if let price = pricing.price, let value = price.value {
            let currency = price.currency ?? "USD"
            html += "<p><strong>Price:</strong> \(currency) \(String(format: "%.2f", value))</p>"
        }
        if let couponUrl = pricing.couponUrl {
            html += "<p><a href=\"\(couponUrl)\">View Coupon</a></p>"
        }
        return html
    }
}
