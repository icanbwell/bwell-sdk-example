import Foundation
import BWellSDK

@MainActor
final class ExplanationOfBenefitViewModel: ObservableObject {
    private var sdkManager: BWellSDKManager?
    @Published var errorMessage: String?
    @Published var isLoading: Bool = false

    @Published var eobJson: String = ""
    @Published var eobData: BWell.GetExplanationOfBenefitsResponse? = nil

    init() {
        sdkManager = .shared
    }

    func loadExplanationOfBenefits() async {
        isLoading = true
        do {
            guard let sdkManager = sdkManager else {
                errorMessage = "SDK Manager not available"
                isLoading = false
                return
            }

            let request = BWell.ExplanationOfBenefitRequest(page: 0)

            let response = try await sdkManager.financial().getExplanationOfBenefits(request)
            self.eobData = response

            let encoder = JSONEncoder()
            encoder.outputFormatting = [.prettyPrinted, .sortedKeys]
            if let data = try? encoder.encode(response) {
                eobJson = String(data: data, encoding: .utf8) ?? "{}"
            } else {
                eobJson = "{}"
            }

            isLoading = false
        } catch {
            errorMessage = "Failed to fetch explanation of benefits: \(error.localizedDescription)"
            isLoading = false
        }
    }
}

extension ExplanationOfBenefitViewModel {
    func refresh() async {
        await loadExplanationOfBenefits()
    }
}
