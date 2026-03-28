// swift-tools-version:5.9
import PackageDescription

let package = Package(
   name: "FirebaseKitRemoteConfig",
   platforms: [
     .iOS(.v16),
   ],
   products: [
      .library(name: "FirebaseKitRemoteConfig", targets: ["Shared"])
   ],
   targets: [
      .binaryTarget(
          name: "Shared",
          path: "FirebaseKitRemoteConfig.xcframework.zip"
      )
   ]
)