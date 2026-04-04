// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "BWellExampleApp",
    platforms: [
        .iOS(.v15),
        .macOS(.v12)
    ],
    dependencies: [
        // ┌──────────────────────────────────────────────────────────────────┐
        // │  Choose ONE of the three options below.                         │
        // │  Comment out the others.                                        │
        // └──────────────────────────────────────────────────────────────────┘

        // OPTION 1: Local SDK source (for SDK contributors)
        // Requires: clone bwell-sdk repo adjacent to this repo
        //   git clone git@github.com:icanbwell/bwell-sdk.git ../../bwell-sdk
        .package(path: "../../bwell-sdk/bwell-sdk-swift")

        // OPTION 2: Snapshot from Artifactory (latest main build)
        // Requires: Artifactory credentials in ~/.netrc
        //   machine icanbwell.jfrog.io
        //     login <your-email>
        //     password <your-artifactory-token>
        // NOTE: XCFramework distribution requires static linking of dependencies (Apollo, OTel)
        // to work across Swift versions. See EA-2147 for progress.
        // .package(url: "https://github.com/icanbwell/bwell-sdk-swift-package", branch: "main")

        // OPTION 3: Released version (production)
        // .package(url: "https://github.com/icanbwell/bwell-sdk-swift-package", from: "1.0.0")
    ],
    targets: [
        .executableTarget(
            name: "BWellExampleApp",
            dependencies: [
                // When using OPTION 1 (local):
                .product(name: "BWellSDK", package: "bwell-sdk-swift")
                // When using OPTION 2 or 3 (package wrapper):
                // .product(name: "BWellSDK", package: "bwell-sdk-swift-package")
            ],
            path: "Sources/BWellExampleApp"
        ),
        .testTarget(
            name: "BWellExampleAppTests",
            dependencies: ["BWellExampleApp"]
        )
    ]
)
