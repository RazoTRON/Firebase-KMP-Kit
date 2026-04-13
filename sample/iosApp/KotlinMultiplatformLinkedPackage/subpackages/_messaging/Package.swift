// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "_messaging",
  platforms: [
    .iOS("15.0")
  ],
  products: [
    .library(
      name: "_messaging",
      type: .none,
      targets: ["_messaging"]
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
      name: "_messaging",
      dependencies: [
        .product(
          name: "FirebaseMessaging",
          package: "firebase-ios-sdk"
        )
      ]
    )
  ]
)
