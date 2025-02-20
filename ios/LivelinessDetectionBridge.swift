import Foundation
import React

@objc(LivelinessDetectionBridge)
class LivelinessDetectionBridge: NSObject {

  // ✅ Expose a function to React Native to send data
  @objc func sendReferenceID(_ referenceID: String) {
      DispatchQueue.main.async {
          if let bridge = RCTBridge.current() {
              bridge.eventDispatcher().sendAppEvent(withName: "onReferenceIDReceived", body: ["referenceID": referenceID])
          }
      }
  }

  // ✅ Required to expose this module to React Native
  @objc static func requiresMainQueueSetup() -> Bool {
      return true
  }
}
