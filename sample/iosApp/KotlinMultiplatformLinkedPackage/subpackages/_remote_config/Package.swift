// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "_remote_config",
  platforms: [
    .iOS("15.0")
  ],
  products: [
    .library(
      name: "_remote_config",
      type: .none,
      targets: ["_remote_config"]
    )
  ],
  dependencies: [
    .package(
      url: "https://github.com/firebase/firebase-ios-sdk.git",
      from: "12.9.0",
    )
  ],
  targets: [
    .target(
      name: "_remote_config",
      dependencies: [
        .product(
          name: "FirebaseCore",
          package: "firebase-ios-sdk",
        ),
        .product(
          name: "FirebaseRemoteConfig",
          package: "firebase-ios-sdk",
        )
      ]
    )
  ]
)
