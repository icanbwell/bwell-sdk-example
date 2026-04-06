//
//  LabTrendChartView.swift
//  bwell-swift-ios
//
//  Line chart for lab results with reference range shading and trend visualization.
//

import SwiftUI
import Charts
import BWellSDK

struct LabTrendChartView: View {
    let observations: [BWell.Observation]
    let title: String

    @State private var selectedRange: ChartTimeRange = .all

    private var allPoints: [ChartDataPoint] {
        observations.compactMap { $0.toChartDataPoint() }
            .sorted { $0.date < $1.date }
    }

    private var dataPoints: [ChartDataPoint] {
        guard let startDate = selectedRange.startDate else { return allPoints }
        return allPoints.filter { $0.date >= startDate }
    }

    private var referenceRange: (low: Double?, high: Double?)? {
        findReferenceRange(in: observations)
    }

    @ViewBuilder
    var body: some View {
        if allPoints.isEmpty {
            EmptyView()
        } else {
            VStack(alignment: .leading, spacing: 12) {
                HStack {
                    Text(title)
                        .font(.headline)
                    Spacer()
                    Picker("Range", selection: $selectedRange) {
                        ForEach(ChartTimeRange.allCases, id: \.self) { range in
                            Text(range.rawValue).tag(range)
                        }
                    }
                    .pickerStyle(.segmented)
                    .frame(width: 200)
                }

                if dataPoints.isEmpty {
                    Text("No results in this time range")
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                        .frame(maxWidth: .infinity, minHeight: 120)
                        .multilineTextAlignment(.center)
                } else {
                    Chart {
                        if let ref = referenceRange, let low = ref.low, let high = ref.high {
                            RectangleMark(
                                yStart: .value("Low", low),
                                yEnd: .value("High", high)
                            )
                            .foregroundStyle(.green.opacity(0.08))

                            RuleMark(y: .value("Low", low))
                                .lineStyle(StrokeStyle(lineWidth: 1, dash: [4]))
                                .foregroundStyle(.green.opacity(0.4))

                            RuleMark(y: .value("High", high))
                                .lineStyle(StrokeStyle(lineWidth: 1, dash: [4]))
                                .foregroundStyle(.green.opacity(0.4))
                        }

                        ForEach(dataPoints) { point in
                            LineMark(
                                x: .value("Date", point.date),
                                y: .value("Value", point.value)
                            )
                            .foregroundStyle(.bwellPurple)
                            .interpolationMethod(.catmullRom)

                            PointMark(
                                x: .value("Date", point.date),
                                y: .value("Value", point.value)
                            )
                            .foregroundStyle(point.inRange ? .bwellPurple : .red)
                            .symbolSize(point.inRange ? 30 : 50)
                        }
                    }
                    .chartYAxisLabel(dataPoints.first?.unit ?? "")
                    .frame(height: 200)
                    .padding(.horizontal, 4)

                    if dataPoints.count <= 10 {
                        VStack(alignment: .leading, spacing: 4) {
                            ForEach(dataPoints.reversed()) { point in
                                HStack {
                                    Circle()
                                        .fill(point.inRange ? Color.bwellPurple : Color.red)
                                        .frame(width: 8, height: 8)
                                    Text(point.date, style: .date)
                                        .font(.caption)
                                        .foregroundStyle(.secondary)
                                    Spacer()
                                    Text(formatValue(point.value, unit: point.unit))
                                        .font(.caption)
                                        .fontWeight(.medium)
                                }
                            }
                        }
                    }
                }
            }
            .padding()
            .background(Color(.systemBackground))
            .clipShape(RoundedRectangle(cornerRadius: 12))
            .shadow(color: .black.opacity(0.05), radius: 4, y: 2)
        }
    }

    private func formatValue(_ value: Double, unit: String) -> String {
        let formatted = value.truncatingRemainder(dividingBy: 1) == 0
            ? String(format: "%.0f", value)
            : String(format: "%.1f", value)
        return "\(formatted) \(unit)".trimmingCharacters(in: .whitespaces)
    }
}
