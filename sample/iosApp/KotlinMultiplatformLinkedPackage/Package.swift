// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "KotlinMultiplatformLinkedPackage",
  platforms: [
    .iOS("15.0")
  ],
  products: [
    .library(
      name: "KotlinMultiplatformLinkedPackage",
      type: .none,
      targets: ["KotlinMultiplatformLinkedPackage"]
    )
  ],
  dependencies: [
    .package(path: "subpackages/_performance"),
    .package(path: "subpackages/_analytics"),
    .package(path: "subpackages/_messaging"),
    .package(path: "subpackages/_remote_config"),
    .package(path: "subpackages/_core")
  ],
  targets: [
    .target(
      name: "KotlinMultiplatformLinkedPackage",
      dependencies: [
        .product(name: "_performance", package: "_performance"),
        .product(name: "_analytics", package: "_analytics"),
        .product(name: "_messaging", package: "_messaging"),
        .product(name: "_remote_config", package: "_remote_config"),
        .product(name: "_core", package: "_core")
      ]
    )
  ]
)
