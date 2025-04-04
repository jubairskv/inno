import Foundation
import React

@objc(LivelinessDetectionBridge)
class LivelinessDetectionBridge: NSObject {

  // ✅ Expose a function to React Native to send data
  @objc func sendSessionTimeout(_ sessionTimeout: String) {
      DispatchQueue.main.async {
          if let bridge = RCTBridge.current() {
              bridge.eventDispatcher().sendAppEvent(withName: "sessionTimeoutStatus", body: ["sessionTimeout": sessionTimeout])
          }
      }
  }

  // ✅ Required to expose this module to React Native
  @objc static func requiresMainQueueSetup() -> Bool {
      return true
  }
}
