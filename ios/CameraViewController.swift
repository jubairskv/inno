import AVFoundation
import UIKit

class SharedViewModel {
    static let shared = SharedViewModel()
    var capturedImageData: Data?
    var ocrResponse: OcrResponseFront?
    var ocrResponseBack: OcrResponseBack?
    var croppedFaceImageData: Data?
    var faceCropped: UIImage?
    var referenceNumber: String?
    var frontImage: UIImage?
    var frontImageUrl: String?
    var backImage: UIImage?
    var backImageUrl: String?
    var selfieImage: Data?
    var referenceId: String = "Reference Id From the native"
    var verificationResult: [String: Any]?
    var isDigitalID: Bool = false
    var digitalFrontImage: UIImage?
    var digitalBackImage: UIImage?
    var hasEmittedTimeout: Bool = false
}

struct OcrResponseFront {
    var fullName: String
    var dob: String
    var sex: String
    var nationality: String
    var fcn: String
    var dateOfExpiry: String
    var imageUrl: String
}

struct OcrResponseBack: Codable {
    let dateOfExpiry: String
    let dateOfIssue: String
    let phoneNumber: String
    let region: String
    let zone: String
    let woreda: String
    let fin: String
}
class CameraViewController: UIViewController, AVCapturePhotoCaptureDelegate {
    var resolve: RCTPromiseResolveBlock?
    var reject: RCTPromiseRejectBlock?
    var captureSession: AVCaptureSession!
    var previewLayer: AVCaptureVideoPreviewLayer!
    var photoOutput: AVCapturePhotoOutput!
    var captureButton: UIButton!
    var capturedImageView: UIImageView!
    var loadingIndicator: UIActivityIndicatorView!
    var inactivityTimer: Timer?

    let translucentBox = UIView()
    let borderBox = UIView()
    let snapLabel = UILabel()

    private func showLoadingIndicator() {
        DispatchQueue.main.async {
            self.loadingIndicator.startAnimating()
        }
    }

    private func hideLoadingIndicator() {
        DispatchQueue.main.async {
            self.loadingIndicator.stopAnimating()
        }
    }

    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        resetInactivityTimer()  // Start the initial timer
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        setupCamera()
        addCameraOverlay()
        setupCapturedImageView()
        setupLoadingIndicator()
        startInactivityTimer()
    }

    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        inactivityTimer?.invalidate()
        inactivityTimer = nil
    }
    override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
        super.touchesBegan(touches, with: event)
        startInactivityTimer()  // Reset the timer on interaction
    }

    private func setupCamera() {
        captureSession = AVCaptureSession()
        captureSession.sessionPreset = .photo

        guard
            let backCamera = AVCaptureDevice.default(
                .builtInWideAngleCamera, for: .video, position: .back)
        else {
            print("No back camera available")
            return
        }

        do {
            let input = try AVCaptureDeviceInput(device: backCamera)
            captureSession.addInput(input)
        } catch {
            print("Error accessing camera: \(error)")
            return
        }

        photoOutput = AVCapturePhotoOutput()
        if captureSession.canAddOutput(photoOutput) {
            captureSession.addOutput(photoOutput)
        }

        previewLayer = AVCaptureVideoPreviewLayer(session: captureSession)
        previewLayer.videoGravity = .resizeAspectFill
        previewLayer.frame = view.layer.bounds
        view.layer.addSublayer(previewLayer)

        captureSession.startRunning()
    }

    private func addCameraOverlay() {
        let overlayView = UIView(frame: view.bounds)
        overlayView.backgroundColor = UIColor.black.withAlphaComponent(0.1)
        view.addSubview(overlayView)

        translucentBox.translatesAutoresizingMaskIntoConstraints = false
        translucentBox.backgroundColor = UIColor.black.withAlphaComponent(0.4)
        translucentBox.layer.cornerRadius = 10
        view.addSubview(translucentBox)

        snapLabel.text = "Snap the front of your ID"
        snapLabel.textColor = .white
        snapLabel.font = UIFont.boldSystemFont(ofSize: 20)
        snapLabel.textAlignment = .center
        snapLabel.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(snapLabel)  // ✅ Add directly to `view`, not `overlayView`

        borderBox.layer.borderColor = UIColor.white.cgColor
        borderBox.layer.borderWidth = 2.0
        borderBox.layer.cornerRadius = 10
        borderBox.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(borderBox)  // ✅ Add directly to `view`

        captureButton = UIButton(type: .system)
        captureButton.setTitle("Capture Front Side", for: .normal)
        captureButton.titleLabel?.font = UIFont.boldSystemFont(ofSize: 18)
        captureButton.backgroundColor = UIColor(
            red: 0x59 / 255.0, green: 0xD5 / 255.0, blue: 0xFF / 255.0, alpha: 1.0)
        captureButton.setTitleColor(.white, for: .normal)
        captureButton.layer.cornerRadius = 10
        captureButton.translatesAutoresizingMaskIntoConstraints = false
        captureButton.addTarget(self, action: #selector(capturePhoto), for: .touchUpInside)
        view.addSubview(captureButton)  // ✅ Add directly to `view`

        // Constraints
        NSLayoutConstraint.activate([
            translucentBox.centerXAnchor.constraint(equalTo: view.centerXAnchor),
            translucentBox.widthAnchor.constraint(equalTo: view.widthAnchor, multiplier: 0.8),
            translucentBox.heightAnchor.constraint(equalToConstant: 60),
            translucentBox.topAnchor.constraint(equalTo: view.topAnchor, constant: 82),

            snapLabel.centerXAnchor.constraint(equalTo: view.centerXAnchor),
            snapLabel.topAnchor.constraint(equalTo: view.topAnchor, constant: 100),

            borderBox.centerXAnchor.constraint(equalTo: view.centerXAnchor),
            borderBox.centerYAnchor.constraint(equalTo: view.centerYAnchor),
            borderBox.widthAnchor.constraint(equalToConstant: 300),
            borderBox.heightAnchor.constraint(equalToConstant: 200),

            captureButton.centerXAnchor.constraint(equalTo: view.centerXAnchor),
            captureButton.bottomAnchor.constraint(equalTo: view.bottomAnchor, constant: -50),
            captureButton.widthAnchor.constraint(equalToConstant: 280),
            captureButton.heightAnchor.constraint(equalToConstant: 55),
        ])

        // ✅ Ensure UI elements stay on top
        view.bringSubviewToFront(translucentBox)
        view.bringSubviewToFront(snapLabel)
        view.bringSubviewToFront(borderBox)
    }
    private func setupCapturedImageView() {
        capturedImageView = UIImageView(frame: view.bounds)
        capturedImageView.contentMode = .scaleAspectFill
        capturedImageView.isHidden = true
        capturedImageView.backgroundColor = .black  // Optional: Prevents black flickering
        view.addSubview(capturedImageView)

        // ✅ Ensure captured image is brought to the front when it’s made visible
        view.bringSubviewToFront(capturedImageView)
    }
    private func setupLoadingIndicator() {
        loadingIndicator = UIActivityIndicatorView(style: .large)
        loadingIndicator.center = view.center
        loadingIndicator.hidesWhenStopped = true
        loadingIndicator.transform = CGAffineTransform(scaleX: 1.5, y: 1.5)
        view.addSubview(loadingIndicator)
    }

    @objc private func capturePhoto() {
        let photoSettings = AVCapturePhotoSettings()
        photoOutput.capturePhoto(with: photoSettings, delegate: self)

    }

    // ✅ Reset the timer for any other UI interactions (e.g., buttons)
    @objc func someButtonTapped() {
        resetInactivityTimer()
        print("Button tapped")
    }

    func photoOutput(
        _ output: AVCapturePhotoOutput, didFinishProcessingPhoto photo: AVCapturePhoto,
        error: Error?
    ) {
        if let error = error {
            showAlert("Capture Error", error.localizedDescription)
            return
        }
        //    super.touchesBegan(touches, with: event)
        //        resetInactivityTimer()

        guard let imageData = photo.fileDataRepresentation() else {
            showAlert("Error", "Failed to process image")
            return
        }

        print("Image captured:------------------ \(imageData)")

        guard let originalImage = UIImage(data: imageData),
            let compressedData = originalImage.jpegData(compressionQuality: 0.15)
        else {
            print("Error: Could not compress image")
            return
        }

        print(compressedData, "----------------------------------------------------------")

        DispatchQueue.main.async {
            if self.capturedImageView == nil {
                self.setupCapturedImageView()
            }

            // ✅ Show captured image
            self.capturedImageView.image = UIImage(data: imageData)
            self.capturedImageView.isHidden = false  // Ensure it is visible

            // ✅ Bring UI elements to front
            self.view.bringSubviewToFront(self.borderBox)
            self.view.bringSubviewToFront(self.captureButton)
            self.view.bringSubviewToFront(self.snapLabel)
            self.view.bringSubviewToFront(self.translucentBox)

            // ✅ Hide camera preview
            self.previewLayer.isHidden = true

            // ✅ Process image
            self.processImage(compressedData)
            self.captureButton.isEnabled = false

            // ✅ Now present the IdCardFrontCapturedViewController
            let capturedViewController = IdCardFrontCapturedViewController()
            capturedViewController.modalPresentationStyle = .fullScreen

            if let rootVC = UIApplication.shared.keyWindow?.rootViewController {
                rootVC.present(capturedViewController, animated: true, completion: nil)
            }
        }
    }
    private func processImage(_ imageData: Data) {
        loadingIndicator.startAnimating()
        view.bringSubviewToFront(loadingIndicator)

        // let referenceID =
        //     "INNOVERIFYIOS" + String(Int(Date().timeIntervalSince1970))
        //     + String(format: "%08d", Int.random(in: 1_000_000...9_999_999))
        let referenceID = SharedViewModel.shared.referenceNumber ?? ""
        print("📡 Sending Reference ID to React Native:", referenceID)

        uploadImageToAPI(data: imageData, referenceID: referenceID)
    }

    private func uploadImageToAPI(data: Data, referenceID: String) {
        showLoadingIndicator()
        let client = URLSession.shared
        let username = "test"
        let password = "test"
        let credentials = "\(username):\(password)"
        guard let credentialsData = credentials.data(using: .utf8) else {
            fatalError("Unable to encode credentials")
        }
        let base64Credentials = credentialsData.base64EncodedString()

        // let referenceID = "INNOVERIFYMAN" + String(Int(Date().timeIntervalSince1970))
        // SharedViewModel.shared.referenceNumber = referenceID

        resetInactivityTimer()
        var ocrRequest = URLRequest(
            url: URL(string: "https://api.innovitegrasuite.online/process-id")!)
        ocrRequest.httpMethod = "POST"
        ocrRequest.setValue("testapikey", forHTTPHeaderField: "api-key")
        ocrRequest.setValue("Basic \(base64Credentials)", forHTTPHeaderField: "Authorization")

        let ocrBoundary = UUID().uuidString
        var ocrRequestBody = Data()

        // Add file field
        ocrRequestBody.append("--\(ocrBoundary)\r\n".data(using: .utf8)!)
        ocrRequestBody.append(
            "Content-Disposition: form-data; name=\"file\"; filename=\"image.jpg\"\r\n".data(
                using: .utf8)!)
        ocrRequestBody.append("Content-Type: image/jpeg\r\n\r\n".data(using: .utf8)!)
        ocrRequestBody.append(data)  // Ensure valid image data is passed
        ocrRequestBody.append("\r\n".data(using: .utf8)!)

        // Add reference_id field
        ocrRequestBody.append("--\(ocrBoundary)\r\n".data(using: .utf8)!)
        ocrRequestBody.append(
            "Content-Disposition: form-data; name=\"reference_id\"\r\n\r\n".data(using: .utf8)!)
        ocrRequestBody.append("\(referenceID)\r\n".data(using: .utf8)!)

        // Add side field
        ocrRequestBody.append("--\(ocrBoundary)\r\n".data(using: .utf8)!)
        ocrRequestBody.append(
            "Content-Disposition: form-data; name=\"side\"\r\n\r\n".data(using: .utf8)!)
        ocrRequestBody.append("front\r\n".data(using: .utf8)!)

        ocrRequestBody.append("--\(ocrBoundary)--\r\n".data(using: .utf8)!)
        ocrRequest.setValue(
            "multipart/form-data; boundary=\(ocrBoundary)", forHTTPHeaderField: "Content-Type")
        ocrRequest.httpBody = ocrRequestBody

        let ocrTask = client.dataTask(with: ocrRequest) { ocrData, ocrResponse, error in
            DispatchQueue.main.async { self.hideLoadingIndicator() }

            guard let httpResponse = ocrResponse as? HTTPURLResponse else {
                DispatchQueue.main.async {
                    self.showAlert("OCR API error", "No response received from server.")
                }
                return
            }

            if let responseData = ocrData,
                let responseString = String(data: responseData, encoding: .utf8)
            {
                print("OCR API Response Body:", responseString)
            }

            guard let ocrData = ocrData, httpResponse.statusCode == 200 else {
                DispatchQueue.main.async {
                    self.showAlert(
                        "OCR API error",
                        "Failed to analyze image. Status Code: \(httpResponse.statusCode)")
                }
                return
            }
            do {
                let jsonResponse =
                    try JSONSerialization.jsonObject(with: ocrData, options: []) as! [String: Any]
                print("OCR JSON Response:", jsonResponse)

                guard let dataObject = jsonResponse["id_analysis"] as? [String: Any],
                    let frontObject = dataObject["front"] as? [String: Any],
                    let croppedFace = jsonResponse["cropped_face"] as? String,
                    let croppedIdUrlString = jsonResponse["cropped_id"] as? String,
                    let croppedIdUrl = URL(string: croppedIdUrlString)
                else {
                    DispatchQueue.main.async {
                        self.showAlert("Error", "Incomplete or missing OCR analysis data.")
                    }
                    return
                }

                let ocrResponse = OcrResponseFront(
                    fullName: frontObject["Full_name"] as? String ?? "N/A",
                    dob: frontObject["Date_of_birth"] as? String ?? "N/A",
                    sex: frontObject["Sex"] as? String ?? "N/A",
                    nationality: frontObject["Nationality"] as? String ?? "N/A",
                    fcn: frontObject["FCN"] as? String ?? "N/A",
                    dateOfExpiry: frontObject["Date_of_expiry"] as? String ?? "N/A",
                    imageUrl: croppedFace
                )

                let bitmap = UIImage(data: data)
                SharedViewModel.shared.ocrResponse = ocrResponse
                SharedViewModel.shared.frontImageUrl = croppedIdUrlString

                // // ✅ Download Cropped Face Image
                // print("🔄 Downloading Cropped Id Image...")
                // let downloadTask = URLSession.shared.dataTask(with: croppedIdUrl) { (data, response, error) in
                //     if let error = error {
                //         print("❌ Error downloading cropped face: \(error)")
                //         return
                //     }

                //     guard let croppedIdData = data else {
                //         print("❌ Error: No data received for cropped face")
                //         return
                //     }

                //     // Convert the downloaded data to UIImage
                //     if let croppedIdImage = UIImage(data: croppedIdData) {
                //         print("✅ Cropped Face Image downloaded successfully!")
                //         SharedViewModel.shared.frontImage = croppedIdImage
                //         print(SharedViewModel.shared.frontImage,"URL-------------URL")
                //         print(croppedIdImage)

                //         // // ✅ Call API after downloading image
                //         // DispatchQueue.main.asyncAfter(deadline: .now() + 3) {
                //         //     self.callVerificationAPI()
                //         // }
                //     } else {
                //         print("❌ Error: Unable to convert downloaded data to UIImage")
                //     }
                // }
                // downloadTask.resume()

                func getTopViewController(
                    _ rootViewController: UIViewController? = UIApplication.shared.windows.first?
                        .rootViewController
                ) -> UIViewController? {
                    if let presentedViewController = rootViewController?.presentedViewController {
                        return getTopViewController(presentedViewController)
                    }
                    if let navigationController = rootViewController as? UINavigationController {
                        return getTopViewController(navigationController.visibleViewController)
                    }
                    if let tabBarController = rootViewController as? UITabBarController {
                        return getTopViewController(tabBarController.selectedViewController)
                    }
                    return rootViewController
                }

                DispatchQueue.main.asyncAfter(deadline: .now() + 0.3) {
                    self.closeFrontCapturedScreen()

                    let capturedViewController = IdCardFrontCapturedViewController()
                    capturedViewController.modalPresentationStyle = .fullScreen

                    if let topVC = getTopViewController(), topVC.view.window != nil {
                        topVC.present(
                            capturedViewController, animated: true,
                            completion: {
                                print("✅ Successfully presented IdCardFrontCapturedViewController")
                            })
                    } else {
                        print("❌ Unable to find the visible view controller to present")
                    }
                }
            } catch {
                DispatchQueue.main.async {
                    self.showAlert("Parsing Error", "Failed to parse OCR response.")
                }
            }
        }
        ocrTask.resume()
        // }
        // croppingTask.resume()
    }

    func showAlert(_ title: String, _ message: String) {
        DispatchQueue.main.async {
            let alert = UIAlertController(title: title, message: message, preferredStyle: .alert)
            alert.addAction(
                UIAlertAction(title: "Retry", style: .default) { _ in
                    self.restartCameraPreview()

                })
            alert.addAction(UIAlertAction(title: "Cancel", style: .cancel, handler: nil))
            self.present(alert, animated: true, completion: nil)
        }
    }
    private func restartCameraPreview() {
        if !captureSession.isRunning {
            captureSession.startRunning()
        }
        capturedImageView.isHidden = true  // Hide captured image
        previewLayer.isHidden = false  // Show camera preview
        captureButton.isEnabled = true  // Re-enable capture button
    }
    private func closeFrontCapturedScreen() {
        self.capturedImageView.isHidden = true
        self.borderBox.isHidden = true
        self.captureButton.isHidden = true
        self.snapLabel.isHidden = true
        self.translucentBox.isHidden = true
        self.previewLayer.removeFromSuperlayer()
        self.captureSession.stopRunning()
        print("✅ Successfully closed previous screen before transition.")
    }
    private func showCapturedImageScreen() {
        DispatchQueue.main.async {
            let capturedViewController = IdCardFrontCapturedViewController()
            capturedViewController.modalPresentationStyle = .fullScreen

            if let rootVC = UIApplication.shared.keyWindow?.rootViewController {
                rootVC.present(capturedViewController, animated: true, completion: nil)
            } else {
                print("❌ Error: Root ViewController is nil. Cannot present captured screen.")
            }
        }
    }

    private func startInactivityTimer() {
        // Invalidate the existing timer if any
        inactivityTimer?.invalidate()

        // Start a new timer for 3 minutes (180 seconds)
        inactivityTimer = Timer.scheduledTimer(
            timeInterval: 180,
            // timeInterval: 180,
            target: self,
            selector: #selector(closeCameraAfterTimeout),
            userInfo: nil,
            repeats: false
        )
    }

    // ✅ Stop the inactivity timer
    private func stopInactivityTimer() {
        inactivityTimer?.invalidate()
        inactivityTimer = nil
    }

    // ✅ Close the camera after 3 minutes
    @objc private func closeCameraAfterTimeout() {
        print("⚠️ Camera closed due to inactivity")

        // Stop the camera session
        captureSession.stopRunning()
        let hasEmitted = SharedViewModel.shared.hasEmittedTimeout;
        if(!hasEmitted) {
            Inno.sharedInstance?.sendEvent(withName: "onScreenTimeout", body: 1)
        }
        
        SharedViewModel.shared.hasEmittedTimeout = true

        // Dismiss the current view controller
        DispatchQueue.main.async {
            self.dismiss(animated: true) {
                // Optionally, close any other native screens
                self.closeAllNativeScreens()
            }
        }
    }

    // ✅ Close all native screens
    private func closeAllNativeScreens() {
        if let rootViewController = UIApplication.shared.windows.first?.rootViewController {
            rootViewController.dismiss(animated: true, completion: nil)
        }
    }

    private func resetInactivityTimer() {
        // Invalidate the existing timer
        inactivityTimer?.invalidate()
        // Start a new timer
        inactivityTimer = Timer.scheduledTimer(
            timeInterval: 180,  // 10 seconds (for testing)
            target: self,
            selector: #selector(closeCameraAfterTimeout),
            userInfo: nil,
            repeats: false
        )
        print("Timer reset due to activity")
    }

}
