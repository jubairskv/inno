import UIKit

class BaseTimerController: UIViewController {
    // Timer Properties
    var inactivityTimer: Timer?
    var inactivityTimeout: TimeInterval = 120  // Default 10 seconds (for testing)

    // MARK: - Lifecycle Methods
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        resetInactivityTimer()
    }

    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        stopInactivityTimer()
    }

    // MARK: - Timer Control Methods
    final func resetInactivityTimer() {
        inactivityTimer?.invalidate()
        inactivityTimer = Timer.scheduledTimer(
            timeInterval: inactivityTimeout,
            target: self,
            selector: #selector(handleInactivityTimeout),
            userInfo: nil,
            repeats: false
        )
        print("Timer reset")
    }

    final func stopInactivityTimer() {
        inactivityTimer?.invalidate()
        inactivityTimer = nil
        print("Timer stopped")
    }

    // MARK: - Timeout Handler (Override in child classes)
    @objc func handleInactivityTimeout() {
        print("Base timeout handler - closing screens")

        DispatchQueue.main.async { [weak self] in
            // Dismiss modal presentations
            self?.dismiss(animated: true)

            // Pop navigation stack
            self?.navigationController?.popToRootViewController(animated: true)
        }
    }

    // MARK: - User Interaction
    override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
        super.touchesBegan(touches, with: event)
        resetInactivityTimer()
    }
}
