import Foundation
import UIKit
import React


@objc(Inno)
class Inno: RCTEventEmitter {
    @objc(multiply:withB:withResolver:withRejecter:)
    func multiply(a: Float, b: Float, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) -> Void {
        resolve(a * b)
    }

    @objc(getHelloWorld:withRejecter:)
    func getHelloWorld(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) -> Void {
        resolve("referenceId")
    }

    @objc(showEkycUI:withRejecter:)
    func showEkycUI(resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
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
      let referenceID = SharedViewModel.shared.referenceNumber ?? "Unknown"
      print("📡 Sending Reference ID to React Native:", referenceID)
      self.sendEvent(withName: "onReferenceIDReceived", body: referenceID)
    }
  }

  override func supportedEvents() -> [String] {
    return ["onReferenceIDReceived"]
  }
}
