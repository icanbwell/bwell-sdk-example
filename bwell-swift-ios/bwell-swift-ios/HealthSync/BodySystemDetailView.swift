//
//  BodySystemDetailView.swift
//  bwell-swift-ios
//
//  One body system's full detail - pushed from a card in the Body Score
//  grid. Ported in spirit (not literally) from ui-platform's mfe-devices
//  BodySystemDetailPage: score + grade + trend, bio-age, recommendations,
//  and contributing components. Deliberately omits that page's "Ask Bailey"
//  AI chat FAB and day-by-day trend chart - both depend on infra this demo
//  doesn't have (an AI chat service, a charting library); everything shown
//  here comes directly from BWell.BodySystemScore's own fields.
//

import BWellSDK
import SwiftUI

/// Navigation value for pushing to a body system's detail.
struct BodySystemDetailRoute: Hashable {
    let bodySystemId: String
    let title: String
}

enum BodySystemDetailState {
    case loading
    case loaded(BWell.BodySystemScore)
    case empty(errorMessage: String?)
}

@MainActor
final class BodySystemDetailViewModel: ObservableObject {
    @Published var state: BodySystemDetailState = .loading

    func load(client: BWellClient, bodySystemId: String) async {
        state = .loading
        do {
            let result = try await client.health.getBodySystemScore(.init(bodySystemId: bodySystemId))
            if let resource = result.resource {
                state = .loaded(resource)
            } else {
                state = .empty(errorMessage: nil)
            }
        } catch {
            state = .empty(errorMessage: "Couldn't load body system: \(error.localizedDescription)")
        }
    }
}

struct BodySystemDetailView: View {
    let route: BodySystemDetailRoute

    @EnvironmentObject private var sdkManager: SDKManager
    @StateObject private var viewModel = BodySystemDetailViewModel()

    var body: some View {
        Group {
            switch viewModel.state {
            case .loading:
                ProgressView().frame(maxWidth: .infinity, maxHeight: .infinity)
            case .empty(let errorMessage):
                Text(errorMessage ?? "No data yet for this body system.")
                    .foregroundStyle(.secondary)
                    .multilineTextAlignment(.center)
                    .padding()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            case .loaded(let bodySystem):
                ScrollView {
                    VStack(alignment: .leading, spacing: 20) {
                        scoreCard(bodySystem)
                        if bodySystem.trendDirection != nil {
                            HealthTrendAlertCard(
                                label: route.title.lowercased(),
                                trendDirection: bodySystem.trendDirection,
                                trendRate: bodySystem.trendRate,
                                periodStart: bodySystem.periodStart
                            )
                        }
                        if let recommendations = bodySystem.recommendations, !recommendations.isEmpty {
                            recommendationsSection(recommendations)
                        }
                        if let components = bodySystem.components, !components.isEmpty {
                            componentsSection(components)
                        }
                        PeriodMetadataView(
                            calculationDate: bodySystem.calculationDate,
                            periodStart: bodySystem.periodStart,
                            periodEnd: bodySystem.periodEnd
                        )
                    }
                    .padding()
                }
            }
        }
        .background(Color(.systemGroupedBackground))
        .navigationTitle(route.title)
        .navigationBarTitleDisplayMode(.inline)
        .task {
            guard let client = sdkManager.sdk else { return }
            await viewModel.load(client: client, bodySystemId: route.bodySystemId)
        }
    }

    // MARK: - Score card

    private func scoreCard(_ bodySystem: BWell.BodySystemScore) -> some View {
        let grade = bodySystem.score.map(HealthGrade.from)
        return VStack(spacing: 12) {
            Text(bodySystem.score.map { String(format: "%.0f", $0) } ?? "—")
                .font(.system(size: 44, weight: .bold))
            if let grade {
                HealthGradeBadge(grade: grade)
            }
            if let score = bodySystem.score {
                HealthBarView(score: score)
                    .padding(.top, 4)
            }
            if let bioAge = bodySystem.bioAge {
                Divider()
                VStack(spacing: 4) {
                    Text("Biological Age").font(.caption).foregroundStyle(.secondary)
                    Text(String(format: "%.0f", bioAge)).font(.title2).fontWeight(.semibold)
                    if let actualAge = bodySystem.actualAge {
                        Text(bioAge <= actualAge
                             ? "Younger than actual age (\(Int(actualAge)))"
                             : "Older than actual age (\(Int(actualAge)))")
                            .font(.caption2).foregroundStyle(.secondary)
                    }
                }
            }
        }
        .frame(maxWidth: .infinity)
        .padding()
        .background(Color(.secondarySystemGroupedBackground))
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }


    // MARK: - Recommendations

    private func recommendationsSection(_ recommendations: [String]) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Recommendations").font(.headline)
            ForEach(recommendations, id: \.self) { recommendation in
                HStack(alignment: .top, spacing: 8) {
                    Circle().frame(width: 5, height: 5).padding(.top, 6)
                        .foregroundStyle(.secondary)
                    Text(recommendation).font(.subheadline)
                }
            }
        }
    }

    // MARK: - Contributing metrics

    private func componentsSection(_ components: [BWell.BodySystemComponent]) -> some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Contributing Metrics").font(.headline)
            ForEach(Array(components.enumerated()), id: \.offset) { _, component in
                VStack(alignment: .leading, spacing: 4) {
                    Text(component.title ?? component.category ?? "Metric")
                        .font(.subheadline).fontWeight(.semibold)
                    if let value = component.value {
                        Text(value).font(.subheadline)
                    } else if let score = component.score {
                        Text(String(format: "%.0f", score)).font(.subheadline)
                    }
                    if let text = component.text {
                        Text(text).font(.caption).foregroundStyle(.secondary)
                    }
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding()
                .background(Color(.secondarySystemGroupedBackground))
                .clipShape(RoundedRectangle(cornerRadius: 12))
            }
        }
    }
}
