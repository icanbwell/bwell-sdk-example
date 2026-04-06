import SwiftUI
import BWellSDK

struct AllergiesView: View {
    @ObservedObject var viewModel: SDKViewModel

    var body: some View {
        List {
            ForEach(viewModel.allergies, id: \.id) { allergy in
                VStack(alignment: .leading, spacing: 4) {
                    Text(allergy.code?.text ?? "Unknown Allergy")
                        .font(.headline)

                    if let category = allergy.category?.first {
                        Text("Category: \(category)")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }

                    if let reaction = allergy.reaction?.first {
                        if let manifestation = reaction.manifestation?.first?.text {
                            Text("Reaction: \(manifestation)")
                                .font(.caption)
                                .foregroundColor(.orange)
                        }
                    }

                    if let clinicalStatus = allergy.clinicalStatus?.text {
                        Text("Status: \(clinicalStatus)")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                }
                .padding(.vertical, 4)
            }
        }
        .navigationTitle("Allergies")
        .task {
            if viewModel.allergies.isEmpty {
                await viewModel.fetchAllergies()
            }
        }
        .refreshable {
            await viewModel.fetchAllergies()
        }
        .overlay {
            if viewModel.allergies.isEmpty {
                VStack(spacing: 16) {
                    Image(systemName: "cross.vial")
                        .font(.system(size: 48))
                        .foregroundColor(.secondary)
                    Text("No Allergies")
                        .font(.title2)
                        .fontWeight(.semibold)
                    Text("No allergy records found")
                        .font(.body)
                        .foregroundColor(.secondary)
                }
            }
        }
    }
}
