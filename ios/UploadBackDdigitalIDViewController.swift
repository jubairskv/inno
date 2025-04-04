import UIKit

class UploadBackDigitalIDViewController: UIViewController, UIImagePickerControllerDelegate,
    UINavigationControllerDelegate
{

    private let titleLabel = UILabel()
    private let imageView = UIImageView()
    private let placeholderLabel = UILabel()
    private let uploadButton = UIButton(type: .system)
    private let loadingIndicator = UIActivityIndicatorView(style: .large)
    private let processingLabel = UILabel()
    var inactivityTimer: Timer?

    override func viewDidLoad() {
        super.viewDidLoad()
        setupGradientBackground()
        setupUI()
    }

    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        resetInactivityTimer()  // Start the initial timer
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

    // ✅ Reset the timer for any other UI interactions (e.g., buttons)
    @objc func someButtonTapped() {
        resetInactivityTimer()
        print("Button tapped")
    }

    // ✅ Gradient Background
    private func setupGradientBackground() {
        let gradientLayer = CAGradientLayer()
        gradientLayer.frame = view.bounds
        gradientLayer.colors = [
            UIColor(hex: "#60CFFF").cgColor,
            UIColor(hex: "#C5EEFF").cgColor,
        ]
        gradientLayer.startPoint = CGPoint(x: 0.5, y: 0.0)
        gradientLayer.endPoint = CGPoint(x: 0.5, y: 1.0)
        view.layer.insertSublayer(gradientLayer, at: 0)
    }

    private func setupUI() {
        view.backgroundColor = .white

        // ✅ Title Label
        titleLabel.text = "Upload Digital ID (Back Side)"
        titleLabel.font = UIFont.boldSystemFont(ofSize: 24)
        titleLabel.textAlignment = .center
        titleLabel.textColor = .white
        titleLabel.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(titleLabel)

        // ✅ Image View (Takes Full Screen Space)
        imageView.contentMode = .scaleAspectFit
        imageView.layer.cornerRadius = 10
        imageView.clipsToBounds = true
        imageView.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(imageView)

        // ✅ Placeholder Label (Displayed When No Image Selected)
        placeholderLabel.text = "No Image Selected\nTap 'Upload ID' to select an image"
        placeholderLabel.numberOfLines = 2
        placeholderLabel.textAlignment = .center
        placeholderLabel.font = UIFont.systemFont(ofSize: 18, weight: .bold)
        placeholderLabel.textColor = .darkGray
        placeholderLabel.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(placeholderLabel)

        // ✅ Upload Button (At Bottom)
        uploadButton.setTitle("Upload ID", for: .normal)
        uploadButton.backgroundColor = .blue
        uploadButton.setTitleColor(.white, for: .normal)
        uploadButton.layer.cornerRadius = 10
        uploadButton.translatesAutoresizingMaskIntoConstraints = false
        uploadButton.backgroundColor = UIColor(
            red: 0x59 / 255.0, green: 0xD5 / 255.0, blue: 0xFF / 255.0, alpha: 1.0)
        uploadButton.addTarget(self, action: #selector(uploadButtonTapped), for: .touchUpInside)
        view.addSubview(uploadButton)

        // ✅ Loading Indicator
        loadingIndicator.hidesWhenStopped = true
        loadingIndicator.transform = CGAffineTransform(scaleX: 1.5, y: 1.5)
        loadingIndicator.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(loadingIndicator)

        // ✅ Processing Label
        processingLabel.text = "Processing..."
        processingLabel.font = UIFont.boldSystemFont(ofSize: 18)
        processingLabel.textAlignment = .center
        processingLabel.textColor = .black
        processingLabel.isHidden = true
        processingLabel.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(processingLabel)

        // ✅ Constraints
        NSLayoutConstraint.activate([
            titleLabel.topAnchor.constraint(
                equalTo: view.safeAreaLayoutGuide.topAnchor, constant: 20),
            titleLabel.centerXAnchor.constraint(equalTo: view.centerXAnchor),

            imageView.topAnchor.constraint(equalTo: titleLabel.bottomAnchor, constant: 20),
            imageView.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 20),
            imageView.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -20),
            imageView.bottomAnchor.constraint(equalTo: uploadButton.topAnchor, constant: -30),

            placeholderLabel.centerXAnchor.constraint(equalTo: imageView.centerXAnchor),
            placeholderLabel.centerYAnchor.constraint(equalTo: imageView.centerYAnchor),

            uploadButton.bottomAnchor.constraint(
                equalTo: view.safeAreaLayoutGuide.bottomAnchor, constant: -20),
            uploadButton.centerXAnchor.constraint(equalTo: view.centerXAnchor),
            uploadButton.widthAnchor.constraint(equalToConstant: 280),
            uploadButton.heightAnchor.constraint(equalToConstant: 55),

            loadingIndicator.centerXAnchor.constraint(equalTo: view.centerXAnchor),
            loadingIndicator.centerYAnchor.constraint(equalTo: view.centerYAnchor),

            processingLabel.topAnchor.constraint(
                equalTo: loadingIndicator.bottomAnchor, constant: 10),
            processingLabel.centerXAnchor.constraint(equalTo: view.centerXAnchor),
        ])

        // ✅ Show Image if Already Selected
        if let selectedImage = SharedViewModel.shared.digitalBackImage {
            imageView.image = selectedImage
            placeholderLabel.isHidden = true
            uploadButton.setTitle("Continue", for: .normal)  // ✅ Change Button Text
        } else {
            placeholderLabel.isHidden = false
        }
    }

    // ✅ Open Gallery
    @objc private func uploadButtonTapped() {
        if let selectedImage = SharedViewModel.shared.digitalBackImage {
            // ✅ Image Already Selected → Start Loading & API Call
            showLoading()
            if let imageData = selectedImage.jpegData(compressionQuality: 0.9) {
                let referenceID = SharedViewModel.shared.referenceNumber!
                uploadImageToAPI(data: imageData, referenceID: referenceID)
            }
        } else {
            // ✅ No Image Selected → Open Image Picker
            let imagePicker = UIImagePickerController()
            imagePicker.delegate = self
            imagePicker.sourceType = .photoLibrary
            present(imagePicker, animated: true)
        }
    }

    // ✅ Handle Image Selection
    func imagePickerController(
        _ picker: UIImagePickerController,
        didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey: Any]
    ) {
        if let selectedImage = info[.originalImage] as? UIImage {
            SharedViewModel.shared.digitalBackImage = selectedImage
            imageView.image = selectedImage
            placeholderLabel.isHidden = true
            uploadButton.setTitle("Continue", for: .normal)  // ✅ Change Button Text
        }
        dismiss(animated: true)
    }

    // ✅ Handle Image Picker Cancellation
    func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
        dismiss(animated: true)
    }

    // ✅ Show Loading
    private func showLoading() {
        loadingIndicator.startAnimating()
        processingLabel.isHidden = false
        uploadButton.isEnabled = false
    }

    // ✅ Hide Loading
    private func hideLoading() {
        loadingIndicator.stopAnimating()
        processingLabel.isHidden = true
        uploadButton.isEnabled = true
    }
    private func showLoadingIndicator() {
        DispatchQueue.main.async {
            self.loadingIndicator.startAnimating()
            self.processingLabel.isHidden = false
            self.uploadButton.isEnabled = false
        }
    }

    private func hideLoadingIndicator() {
        DispatchQueue.main.async {
            self.loadingIndicator.stopAnimating()
            self.processingLabel.isHidden = true
            self.uploadButton.isEnabled = true
        }
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
        ocrRequestBody.append("back\r\n".data(using: .utf8)!)

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
                    let backObject = dataObject["back"] as? [String: Any]
                else {
                    DispatchQueue.main.async {
                        self.showAlert("Error", "Incomplete or missing OCR analysis data.")
                    }
                    return
                }
                let ocrResponse = OcrResponseBack(
                    dateOfExpiry: backObject["Date_of_Expiry"] as? String ?? "",
                    dateOfIssue: backObject["Date_of_Issue"] as? String ?? "",
                    phoneNumber: backObject["Phone_Number"] as? String ?? "",
                    region: backObject["Region_City_Admin"] as? String ?? "",
                    zone: backObject["Zone_City_Admin_Sub_City"] as? String ?? "",
                    woreda: backObject["Woreda_City_Admin_Kebele"] as? String ?? "",
                    fin: backObject["FIN"] as? String ?? ""
                )
                let bitmap = UIImage(data: data)
                SharedViewModel.shared.ocrResponseBack = ocrResponse

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
                DispatchQueue.main.async {
                    //                    self.closeFrontCapturedScreen()

                    let frontBackCapturedVC = DigitalFrontDetailsViewController()
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

    func showAlert(_ title: String, _ message: String) {
        DispatchQueue.main.async {
            let alert = UIAlertController(title: title, message: message, preferredStyle: .alert)
            alert.addAction(
                UIAlertAction(title: "Retry", style: .default) { _ in
                    //                self.restartCameraPreview()
                    let imagePicker = UIImagePickerController()
                    imagePicker.delegate = self
                    imagePicker.sourceType = .photoLibrary
                    self.present(imagePicker, animated: true)
                })
            alert.addAction(UIAlertAction(title: "Cancel", style: .cancel, handler: nil))
            self.present(alert, animated: true, completion: nil)
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
        //        print("⚠️ Camera closed due to inactivity")
        //
        //        // Stop the camera session
        //        captureSession.stopRunning()
        Inno.sharedInstance?.sendEvent(withName: "onScreenTimeout", body: 1)

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