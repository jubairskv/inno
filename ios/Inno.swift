import Foundation
import React
import UIKit

@objc(Inno)
class Inno: RCTEventEmitter {
  // Store a reference to the shared instance so that other classes can send events
  static var sharedInstance: Inno?

  override init() {
    super.init()
    Inno.sharedInstance = self
  }

  @objc(multiply:withB:withResolver:withRejecter:)
  func multiply(a: Float, b: Float, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock)
  {
    resolve(a * b)
  }

  @objc(getHelloWorld:withRejecter:)
  func getHelloWorld(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
    resolve("referenceId")
  }

  // Updated method signature for proper bridging
  @objc(showEkycUI:resolver:rejecter:)
  func showEkycUI(
    _ referenceId: NSString, resolver resolve: @escaping RCTPromiseResolveBlock,
    rejecter reject: @escaping RCTPromiseRejectBlock
  ) {
    // Save the reference number in your shared view model
    SharedViewModel.shared.referenceNumber = referenceId as String
    print("🚀 Launching EkycViewController with referenceId:", referenceId)

    DispatchQueue.main.async {
      let ekycViewController = EkycViewController()
      ekycViewController.resolve = resolve
      ekycViewController.reject = reject
      ekycViewController.modalPresentationStyle = .fullScreen

      if let rootViewController = UIApplication.shared.keyWindow?.rootViewController {
        rootViewController.present(ekycViewController, animated: true, completion: nil)
      } else {
        reject("NO_ROOT_VIEW_CONTROLLER", "Could not find root view controller", nil)
      }
    }
  }

  override static func requiresMainQueueSetup() -> Bool {
    return true
  }

  @objc func startLivelinessDetection() {
    DispatchQueue.main.async {
      // let referenceID = SharedViewModel.shared.referenceNumber ?? "Unknown"
      // print("📡 Sending Reference ID to React Native:", referenceID)
      let sessionTimeout = "0"
      print("Sending 0 to React native ",sessionTimeout)
      self.sendEvent(withName: "sessionTimeoutStatus", body: sessionTimeout)
    }
  }

  override func supportedEvents() -> [String] {
        SharedViewModel.shared.capturedImageData = nil
        SharedViewModel.shared.ocrResponse = nil
        SharedViewModel.shared.ocrResponseBack = nil
        SharedViewModel.shared.croppedFaceImageData = nil
        SharedViewModel.shared.faceCropped = nil
        SharedViewModel.shared.referenceNumber = nil
        SharedViewModel.shared.frontImage = nil
        SharedViewModel.shared.backImage = nil
        SharedViewModel.shared.selfieImage = nil
        SharedViewModel.shared.verificationResult = nil
        SharedViewModel.shared.isDigitalID = false
        SharedViewModel.shared.digitalFrontImage = nil
        SharedViewModel.shared.digitalBackImage = nil
        SharedViewModel.shared.frontImageUrl = nil
        SharedViewModel.shared.backImageUrl = nil
    // Add the new event 'onScreenTimeout' along with your existing event
    return ["onReferenceIDReceived", "onScreenTimeout"]
  }
}