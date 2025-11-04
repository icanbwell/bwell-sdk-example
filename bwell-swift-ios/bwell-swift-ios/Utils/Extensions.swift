//
//  Extensions.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 30/10/25.
//

import Foundation
import BWellSDK

extension Date {
    func toString() -> String {
        let dateFormatter = DateFormatter()
        dateFormatter.dateStyle = .long
        dateFormatter.timeStyle = .none
        dateFormatter.locale = Locale(identifier: "en_US")

        return dateFormatter.string(from: self)
    }
}

extension String {
    func dateFormatter() -> String {
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "en_US_POSIX")
        
        let formats = [
            "yyyy-MM-dd/HH:mm:ssZZZZZ",
            "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
            "yyyy-MM-dd'T'HH:mm:ssZ",
            "yyyy-MM-dd"
        ]
        
        var date: Date?
        for format in formats {
            formatter.dateFormat = format
            if let parsedDate = formatter.date(from: self) {
                date = parsedDate
                break
            }
        }
        
        guard let validDate = date else { return self }

        let outputFormatter = DateFormatter()
        outputFormatter.dateFormat = "MMMM dd, yyyy"
        outputFormatter.locale = Locale(identifier: "en_US_POSIX")

        return outputFormatter.string(from: validDate)
    }

    func toDate() -> Date {
        let formatter = DateFormatter()
        formatter.dateFormat = "MMMM dd, yyyy"
        formatter.locale = Locale(identifier: "en_US_POSIX")

        return formatter.date(from: self) ?? .now
    }

    func stripHTML() -> String {
        guard let data = self.data(using: .utf8) else { return self }

        let options: [NSAttributedString.DocumentReadingOptionKey: Any] = [
            .documentType: NSAttributedString.DocumentType.html,
            .characterEncoding: String.Encoding.utf8.rawValue
        ]

        guard let attributedString = try? NSAttributedString(data: data,
                                                             options: options,
                                                             documentAttributes: nil) else {
            return self
        }

        return attributedString.string
    }
}

extension BWell.Gender {
    func description() -> String {
        switch self {
            case .female:
                return "Female"
            case .male:
                return "Male"
            case .other:
                return "Other"
            default:
                return "Unknown"
        }
    }
}
