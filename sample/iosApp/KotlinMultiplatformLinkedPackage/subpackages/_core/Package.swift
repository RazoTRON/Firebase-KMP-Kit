// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "_core",
  platforms: [
    .iOS("15.0")
  ],
  products: [
    .library(
      name: "_core",
      type: .none,
      targets: ["_core"]
    )
  ],
  dependencies: [
    .package(
      url: "https://github.com/firebase/firebase-ios-sdk.git",
      from: "12.12.0"
    )
  ],
  targets: [
    .target(
      name: "_core",
      dependencies: [
        .product(
          name: "FirebaseCore",
          package: "firebase-ios-sdk"
        )
      ]
    )
  ]
)
