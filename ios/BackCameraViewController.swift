import AVFoundation
import UIKit


class BackCameraViewController: UIViewController, AVCapturePhotoCaptureDelegate {
    var resolve: RCTPromiseResolveBlock?
    var reject: RCTPromiseRejectBlock?
    var captureSession: AVCaptureSession!
    var previewLayer: AVCaptureVideoPreviewLayer!
    var photoOutput: AVCapturePhotoOutput!
    var captureButton: UIButton!
    var capturedImageView: UIImageView!
    var loadingIndicator: UIActivityIndicatorView!

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

    override func viewDidLoad() {
        super.viewDidLoad()
        setupCamera()
        addCameraOverlay()
        setupCapturedImageView()
        setupLoadingIndicator()
    }

    private func setupCamera() {
        captureSession = AVCaptureSession()
        captureSession.sessionPreset = .photo

        guard let backCamera = AVCaptureDevice.default(.builtInWideAngleCamera, for: .video, position: .back) else {
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

    snapLabel.text = "Snap the Back of your ID"
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
    captureButton.setTitle("Capture Back Side", for: .normal)
    captureButton.titleLabel?.font = UIFont.boldSystemFont(ofSize: 18)
    captureButton.backgroundColor = .blue
    captureButton.setTitleColor(.white, for: .normal)
    captureButton.backgroundColor = UIColor(red: 0x59/255.0, green: 0xD5/255.0, blue: 0xFF/255.0, alpha: 1.0)
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
        captureButton.heightAnchor.constraint(equalToConstant: 55)
    ])

    // ✅ Ensure UI elements stay on top
    view.bringSubviewToFront(translucentBox)
    view.bringSubviewToFront(snapLabel)
    view.bringSubviewToFront(borderBox)
    view.bringSubviewToFront(captureButton)
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
        // captureButton.isEnabled = false
    }

    @objc private func capturePhoto() {
    let photoSettings = AVCapturePhotoSettings()
    photoOutput.capturePhoto(with: photoSettings, delegate: self)

}

func photoOutput(_ output: AVCapturePhotoOutput, didFinishProcessingPhoto photo: AVCapturePhoto, error: Error?) {
    if let error = error {
        showAlert("Capture Error", error.localizedDescription)
        return
    }

    guard let imageData = photo.fileDataRepresentation() else {
        showAlert("Error", "Failed to process image")
        return
    }

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
        self.processImage(imageData)
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

  let referenceID = SharedViewModel.shared.referenceNumber ?? "null"
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

    // First API call (Cropping)
    var croppingRequest = URLRequest(url: URL(string: "https://api.innovitegrasuite.online/crop-aadhar-card/")!)
    croppingRequest.httpMethod = "POST"
    let boundary = UUID().uuidString
    var croppingRequestBody = Data()

    // Add image data to form
    croppingRequestBody.append("--\(boundary)\r\n".data(using: .utf8)!)
    croppingRequestBody.append("Content-Disposition: form-data; name=\"file\"; filename=\"image.jpg\"\r\n".data(using: .utf8)!)
    croppingRequestBody.append("Content-Type: image/jpeg\r\n\r\n".data(using: .utf8)!)
    croppingRequestBody.append(data)
    croppingRequestBody.append("\r\n--\(boundary)--\r\n".data(using: .utf8)!)
    croppingRequest.setValue("multipart/form-data; boundary=\(boundary)", forHTTPHeaderField: "Content-Type")
    croppingRequest.httpBody = croppingRequestBody

    let croppingTask = client.dataTask(with: croppingRequest) { croppingData, croppingResponse, error in
        if let error = error {
            print("❌ Cropping API Request Failed: \(error.localizedDescription)")
            DispatchQueue.main.async {
                self.hideLoadingIndicator()
                self.showAlert("Cropping API error", "Failed to process image.")
            }
            return
        }

        guard let croppingData = croppingData,
              let httpResponse = croppingResponse as? HTTPURLResponse else {
            print("❌ No response or data from server")
            DispatchQueue.main.async {
                self.hideLoadingIndicator()
                self.showAlert("Cropping API error", "No response from server.")
            }
            return
        }

        SharedViewModel.shared.backImage = UIImage(data: croppingData)

        // ✅ Log response status and headers
        print("ℹ️ Cropping API HTTP Status Code: \(httpResponse.statusCode)")
        print("ℹ️ Cropping API Response Headers: \(httpResponse.allHeaderFields)")

        // ✅ Print response body as string
        if let responseString = String(data: croppingData, encoding: .utf8) {
            print("✅ Cropping API Response: \(responseString)")
        } else {
            print("⚠️ Unable to parse response data")
        }

        var ocrRequest = URLRequest(url: URL(string: "https://api.innovitegrasuite.online/process-id")!)
        ocrRequest.httpMethod = "POST"
        ocrRequest.setValue("testapikey", forHTTPHeaderField: "api-key")
        ocrRequest.setValue("Basic \(base64Credentials)", forHTTPHeaderField: "Authorization")

        let ocrBoundary = UUID().uuidString
        var ocrRequestBody = Data()

        // Add file field
        ocrRequestBody.append("--\(ocrBoundary)\r\n".data(using: .utf8)!)
        ocrRequestBody.append("Content-Disposition: form-data; name=\"file\"; filename=\"image.jpg\"\r\n".data(using: .utf8)!)
        ocrRequestBody.append("Content-Type: image/jpeg\r\n\r\n".data(using: .utf8)!)
        ocrRequestBody.append(croppingData) // Ensure valid image data is passed
        ocrRequestBody.append("\r\n".data(using: .utf8)!)

        // Add reference_id field
        ocrRequestBody.append("--\(ocrBoundary)\r\n".data(using: .utf8)!)
        ocrRequestBody.append("Content-Disposition: form-data; name=\"reference_id\"\r\n\r\n".data(using: .utf8)!)
        ocrRequestBody.append("\(referenceID)\r\n".data(using: .utf8)!)

        // Add side field
        ocrRequestBody.append("--\(ocrBoundary)\r\n".data(using: .utf8)!)
        ocrRequestBody.append("Content-Disposition: form-data; name=\"side\"\r\n\r\n".data(using: .utf8)!)
        ocrRequestBody.append("back\r\n".data(using: .utf8)!)

        ocrRequestBody.append("--\(ocrBoundary)--\r\n".data(using: .utf8)!)
        ocrRequest.setValue("multipart/form-data; boundary=\(ocrBoundary)", forHTTPHeaderField: "Content-Type")
        ocrRequest.httpBody = ocrRequestBody

        let ocrTask = client.dataTask(with: ocrRequest) { ocrData, ocrResponse, error in
            DispatchQueue.main.async { self.hideLoadingIndicator() }

            guard let httpResponse = ocrResponse as? HTTPURLResponse else {
                DispatchQueue.main.async {
                    self.showAlert("OCR API error", "No response received from server.")
                }
                return
            }

            if let responseData = ocrData, let responseString = String(data: responseData, encoding: .utf8) {
                print("OCR API Response Body:", responseString)
            }

            guard let ocrData = ocrData, httpResponse.statusCode == 200 else {
                DispatchQueue.main.async {
                    self.showAlert("OCR API error", "Failed to analyze image. Status Code: \(httpResponse.statusCode)")
                }
                return
            }
            do {
                let jsonResponse = try JSONSerialization.jsonObject(with: ocrData, options: []) as! [String: Any]
                print("OCR JSON Response:", jsonResponse)

                guard let dataObject = jsonResponse["id_analysis"] as? [String: Any],
                      let backObject = dataObject["back"] as? [String: Any] else {
                    DispatchQueue.main.async {
                        self.showAlert("Error", "Incomplete or missing OCR analysis data.")
                    }
                    return
                }
                let ocrResponse = OcrResponseBack(
                        dateOfExpiry: backObject["Date_of_Expiry"] as? String ?? "N/A",
                        phoneNumber: backObject["Phone_Number"] as? String ?? "N/A",
                        region: backObject["Region"] as? String ?? "N/A",
                        zone: backObject["Zone"] as? String ?? "N/A",
                        woreda: backObject["Woreda"] as? String ?? "N/A",
                        fin: backObject["FIN"] as? String ?? "N/A"
                    )
                let bitmap = UIImage(data: croppingData)
                SharedViewModel.shared.ocrResponseBack = ocrResponse

                func getTopViewController(_ rootViewController: UIViewController? = UIApplication.shared.windows.first?.rootViewController) -> UIViewController? {
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
                DispatchQueue.main.async {
                    self.closeFrontCapturedScreen()


                    let frontBackCapturedVC = IdCardFrontBackCapturedViewController()
                    frontBackCapturedVC.modalPresentationStyle = .fullScreen
                    self.present(frontBackCapturedVC, animated: true, completion: nil)
                }
            } catch {
                DispatchQueue.main.async {
                    self.showAlert("Parsing Error", "Failed to parse OCR response.")
                }
            }
        }
        ocrTask.resume()
    }

    croppingTask.resume()
}
     func showAlert(_ title: String, _ message: String) {
        DispatchQueue.main.async {
            let alert = UIAlertController(title: title, message: message, preferredStyle: .alert)
            alert.addAction(UIAlertAction(title: "Retry", style: .default) { _ in
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
}
