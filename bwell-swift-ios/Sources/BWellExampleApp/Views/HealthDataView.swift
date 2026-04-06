import SwiftUI

struct HealthDataView: View {
    @ObservedObject var viewModel: SDKViewModel

    var body: some View {
        List {
            NavigationLink("Allergies") {
                AllergiesView(viewModel: viewModel)
            }

            // Add more navigation links for other data types as needed
            // NavigationLink("Medications") { MedicationsView(viewModel: viewModel) }
            // NavigationLink("Labs") { LabsView(viewModel: viewModel) }
        }
        .navigationTitle("Health Data")
    }
}
