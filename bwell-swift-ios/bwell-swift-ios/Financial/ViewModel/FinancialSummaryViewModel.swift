import Foundation
import BWellSDK

@MainActor
final class FinancialSummaryViewModel: ObservableObject {
    private var sdkManager: BWellSDKManager?
    @Published var errorMessage: String?
    @Published var isLoading: Bool = false

    @Published var coveragesJson: String = ""
    @Published var coverageData: BWell.GetCoverageResponse? = nil

    init() {
        sdkManager = .shared
    }

    func loadCoverages() async {
        isLoading = true
        do {
            guard let sdkManager = sdkManager else {
                errorMessage = "SDK Manager not available"
                isLoading = false
                return
            }
            
            let request = BWell.CoverageRequest(id: ["4b1d7aba-2377-58ce-af45-0d2c105d5f36"],page: 0)

            let response = try await sdkManager.financial().getCoverages(request)
            self.coverageData = response

            let encoder = JSONEncoder()
            encoder.outputFormatting = [.prettyPrinted, .sortedKeys]
            if let data = try? encoder.encode(response) {
                coveragesJson = String(data: data, encoding: .utf8) ?? "{}"
            } else {
                coveragesJson = "{}"
            }

            isLoading = false
        } catch {
            errorMessage = "Failed to fetch coverages: \(error.localizedDescription)"
            isLoading = false
        }
    }
}

extension FinancialSummaryViewModel {
    func refresh() async {
        await loadCoverages()
    }
}
