import AVFoundation
import CoreMotion
import UIKit
import Vision

class LivelinessDetectionViewController: UIViewController {
    private var captureSession: AVCaptureSession!
    private var previewLayer: AVCaptureVideoPreviewLayer!
    private var faceDetectionRequest: VNDetectFaceLandmarksRequest!
    private var cameraOutput = AVCapturePhotoOutput()
    private var isBlinkDetected = false
    private var isLeftTurnDetected = false
    private var isRightTurnDetected = false
    private var countdownLabel = UILabel()
    private var loadingIndicator = UIActivityIndicatorView(style: .large)
    private let translucentBox = UIView()
    private let statusLabel = UILabel()
    private var lastEyeState: Bool = false
    private let earThreshold: Float = 0.025
    private var countdownTimer: Timer?
    private var countdownSeconds = 3
    private let motionManager = CMMotionManager()
    private var imageCaptured = false
    var innoInstance: Inno?
    var inactivityTimer: Timer?
    private var hasEmittedTimeout: Bool = false

    private var faceBoundingBoxLayer: CAShapeLayer?

    private let orientationWarningLabel = UILabel()
    private var isPortraitMode = true

    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        startInactivityTimer()  // Start the initial timer
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        setupCamera()
        setupUI()
        setupFaceDetection()
        startDeviceMotionUpdates()
    }

    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        stopInactivityTimer()
        inactivityTimer?.invalidate()
        inactivityTimer = nil
    }
    // override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
    //     super.touchesBegan(touches, with: event)
    //     //        someButtonTapped()
    //     print("Touch detected")
    //     resetInactivityTimer()  // Reset the timer on interaction
    // }

    // ✅ Reset the timer for any other UI interactions (e.g., buttons)
    // @objc func someButtonTapped() {
    //     resetInactivityTimer()
    //     print("Button tapped")
    // }

    enum LivelinessState {
        case waitingForBlink
        case waitingForLeftTurn
        case waitingForRightTurn
        case countingDown
        case capturingSelfie
        case loading
    }
    private var currentState: LivelinessState = .waitingForBlink

    private func setupCamera() {
        captureSession = AVCaptureSession()
        guard
            let frontCamera = AVCaptureDevice.default(
                .builtInWideAngleCamera, for: .video, position: .front)
        else {
            print("No front camera available")
            return
        }
        do {
            let input = try AVCaptureDeviceInput(device: frontCamera)
            captureSession.addInput(input)
            captureSession.addOutput(cameraOutput)
        } catch {
            print("Error accessing front camera: \(error)")
        }

        previewLayer = AVCaptureVideoPreviewLayer(session: captureSession)
        previewLayer.videoGravity = .resizeAspectFill
        previewLayer.frame = view.bounds  // Set initial frame
        view.layer.addSublayer(previewLayer)
        captureSession.startRunning()
        
    }

    private func setupUI() {
        // startInactivityTimer()

        // Initialize face bounding box layer
        faceBoundingBoxLayer = CAShapeLayer()
        faceBoundingBoxLayer?.strokeColor = UIColor.green.cgColor
        faceBoundingBoxLayer?.lineWidth = 2.0
        faceBoundingBoxLayer?.fillColor = nil
        previewLayer.addSublayer(faceBoundingBoxLayer!)

        orientationWarningLabel.frame = CGRect(
            x: 20, y: 100, width: view.frame.width - 40, height: 100)
        orientationWarningLabel.numberOfLines = 0
        orientationWarningLabel.textAlignment = .center
        orientationWarningLabel.backgroundColor = UIColor.black.withAlphaComponent(0.7)
        orientationWarningLabel.textColor = .white
        orientationWarningLabel.text = "Please rotate your device\nto portrait mode to continue."
        orientationWarningLabel.layer.cornerRadius = 10
        orientationWarningLabel.clipsToBounds = true
        orientationWarningLabel.isHidden = true
        view.addSubview(orientationWarningLabel)

        orientationWarningLabel.translatesAutoresizingMaskIntoConstraints = false
        orientationWarningLabel.numberOfLines = 0
        orientationWarningLabel.textAlignment = .center
        orientationWarningLabel.backgroundColor = UIColor.black.withAlphaComponent(0.7)
        orientationWarningLabel.textColor = .white
        orientationWarningLabel.text = "Please rotate your device\nto portrait mode to continue."
        orientationWarningLabel.layer.cornerRadius = 10
        orientationWarningLabel.clipsToBounds = true
        orientationWarningLabel.isHidden = true
        view.addSubview(orientationWarningLabel)

        NSLayoutConstraint.activate([
            orientationWarningLabel.centerXAnchor.constraint(equalTo: view.centerXAnchor),
            orientationWarningLabel.centerYAnchor.constraint(equalTo: view.centerYAnchor),
            orientationWarningLabel.leadingAnchor.constraint(
                equalTo: view.leadingAnchor, constant: 20),
            orientationWarningLabel.trailingAnchor.constraint(
                equalTo: view.trailingAnchor, constant: -20),
            orientationWarningLabel.heightAnchor.constraint(equalToConstant: 100),
        ])

        translucentBox.frame = CGRect(x: 20, y: 100, width: view.frame.width - 40, height: 50)
        translucentBox.backgroundColor = UIColor.black.withAlphaComponent(0.6)
        translucentBox.layer.cornerRadius = 10
        translucentBox.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(translucentBox)
        NSLayoutConstraint.activate([
            translucentBox.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 20),
            translucentBox.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -20),
            translucentBox.topAnchor.constraint(
                equalTo: view.safeAreaLayoutGuide.topAnchor, constant: 20),
            translucentBox.heightAnchor.constraint(equalToConstant: 50),
        ])

        statusLabel.frame = translucentBox.bounds
        statusLabel.text = "Please place your face in the camera view"
        statusLabel.textColor = .white
        statusLabel.textAlignment = .center
        statusLabel.translatesAutoresizingMaskIntoConstraints = false
        translucentBox.addSubview(statusLabel)
        NSLayoutConstraint.activate([
            statusLabel.leadingAnchor.constraint(
                equalTo: translucentBox.leadingAnchor, constant: 10),
            statusLabel.trailingAnchor.constraint(
                equalTo: translucentBox.trailingAnchor, constant: -10),
            statusLabel.topAnchor.constraint(equalTo: translucentBox.topAnchor),
            statusLabel.bottomAnchor.constraint(equalTo: translucentBox.bottomAnchor),
        ])
        countdownLabel.frame = CGRect(
            x: (view.frame.width - 100) / 2, y: view.center.y - 50, width: 100, height: 100)
        countdownLabel.font = UIFont.boldSystemFont(ofSize: 40)
        countdownLabel.textColor = .white
        countdownLabel.textAlignment = .center
        countdownLabel.backgroundColor = UIColor.black.withAlphaComponent(0.6)
        countdownLabel.layer.cornerRadius = 10
        countdownLabel.isHidden = true
        countdownLabel.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(countdownLabel)
        NSLayoutConstraint.activate([
            countdownLabel.centerXAnchor.constraint(equalTo: view.centerXAnchor),
            countdownLabel.centerYAnchor.constraint(equalTo: view.centerYAnchor),
            countdownLabel.widthAnchor.constraint(equalToConstant: 100),
            countdownLabel.heightAnchor.constraint(equalToConstant: 100),
        ])

        loadingIndicator.center = view.center
        loadingIndicator.hidesWhenStopped = true
        loadingIndicator.transform = CGAffineTransform(scaleX: 1.5, y: 1.5)
        loadingIndicator.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(loadingIndicator)
        NSLayoutConstraint.activate([
            loadingIndicator.centerXAnchor.constraint(equalTo: view.centerXAnchor),
            loadingIndicator.centerYAnchor.constraint(equalTo: view.centerYAnchor),
        ])

    }

    private func setupFaceDetection() {
        faceDetectionRequest = VNDetectFaceLandmarksRequest { [weak self] request, error in
            guard let results = request.results as? [VNFaceObservation], let self = self else {
                return
            }
            DispatchQueue.main.async {
                self.processFaceObservations(results)
            }
        }
        let videoOutput = AVCaptureVideoDataOutput()
        videoOutput.setSampleBufferDelegate(
            self, queue: DispatchQueue.global(qos: .userInteractive))
        captureSession.addOutput(videoOutput)
    }

    private func processFaceObservations(_ observations: [VNFaceObservation]) {
        guard isPortraitMode else {
            DispatchQueue.main.async {
                self.statusLabel.text = "Please rotate to portrait mode"
                self.faceBoundingBoxLayer?.path = nil
            }
            return
        }
        guard let face = observations.first else {
            DispatchQueue.main.async {
                self.statusLabel.text = "Please place your face in the camera view"
                self.faceBoundingBoxLayer?.path = nil
                self.countdownTimer?.invalidate()
                self.countdownTimer = nil
                self.countdownLabel.isHidden = true
            }
            return
        }

        switch currentState {
        case .waitingForBlink:
            DispatchQueue.main.async {
                self.statusLabel.text = "Please Blink Your Eyes"
            }
            detectBlink(face)
        case .waitingForLeftTurn:
            DispatchQueue.main.async {
                self.statusLabel.text = "Please turn your head to the right"
            }
            detectHeadTurn(face, direction: "left")
        case .waitingForRightTurn:
            DispatchQueue.main.async {
                self.statusLabel.text = "Please turn your head to the left"
            }
            detectHeadTurn(face, direction: "right")
        case .countingDown:
            statusLabel.text = "Get ready for the Selfie"
            // Start countdown only if not already started
            if countdownTimer == nil {
                startCountdown()
            }
        case .capturingSelfie, .loading:
            statusLabel.text = "Perfect! Processing..."
            // Do nothing, waiting for the process to complete
            break
        }

        DispatchQueue.main.async {
            if let layerRect = self.convertFaceBoundingBox(face) {
                let path = UIBezierPath(rect: layerRect)
                self.faceBoundingBoxLayer?.path = path.cgPath
            } else {
                self.faceBoundingBoxLayer?.path = nil
            }
        }

    }
    private func detectBlink(_ face: VNFaceObservation) {
        guard let landmarks = face.landmarks,
            let leftEye = landmarks.leftEye,
            let rightEye = landmarks.rightEye
        else {
            return
        }

        resetInactivityTimer()

        let leftEAR = calculateEyeAspectRatio(eye: leftEye)
        let rightEAR = calculateEyeAspectRatio(eye: rightEye)
        let averageEAR = (leftEAR + rightEAR) / 2.0

        let blinkThreshold: Float = 0.05
        let eyesClosed = averageEAR < blinkThreshold

        if eyesClosed && !lastEyeState {
            currentState = .waitingForLeftTurn
            print("Blink detected! Waiting for left turn.")
        }

        lastEyeState = eyesClosed
    }

    private func detectHeadTurn(_ face: VNFaceObservation, direction: String) {
        let yaw = face.yaw?.doubleValue ?? 0.0

        if direction == "left" && yaw < -0.3 {
            currentState = .waitingForRightTurn
            print("Left turn detected! Waiting for right turn.")
        } else if direction == "right" && yaw > 0.3 {
            currentState = .countingDown
            print("Right turn detected! Starting countdown.")
        }
    }

    private func calculateEyeAspectRatio(eye: VNFaceLandmarkRegion2D) -> Float {
        let eyePoints = eye.normalizedPoints

        // Ensure we have enough points to calculate EAR
        guard eyePoints.count >= 6 else { return 0.0 }

        // Extract the required points
        let p1 = eyePoints[0]  // Left corner of the eye
        let p2 = eyePoints[1]  // Top of the eye
        let p3 = eyePoints[2]  // Inner top of the eye
        let p4 = eyePoints[3]  // Right corner of the eye
        let p5 = eyePoints[4]  // Bottom of the eye
        let p6 = eyePoints[5]  // Inner bottom of the eye

        // Calculate distances
        let vertical1 = distanceBetweenPoints(p2, p6)
        let vertical2 = distanceBetweenPoints(p3, p5)
        let horizontal = distanceBetweenPoints(p1, p4)

        // Calculate EAR
        let ear = (vertical1 + vertical2) / (2 * horizontal)
        return Float(ear)
    }

    private func distanceBetweenPoints(_ p1: CGPoint, _ p2: CGPoint) -> CGFloat {
        return sqrt(pow(p2.x - p1.x, 2) + pow(p2.y - p1.y, 2))
    }

    private func startCountdown() {
        // Reset countdown state
        countdownSeconds = 3
        countdownLabel.isHidden = false
        countdownLabel.text = "\(countdownSeconds)"

        resetInactivityTimer()

        // Invalidate any existing timer
        countdownTimer?.invalidate()
        countdownTimer = nil

        // Start new timer
        countdownTimer = Timer.scheduledTimer(withTimeInterval: 1.0, repeats: true) {
            [weak self] timer in
            guard let self = self else {
                timer.invalidate()
                return
            }

            self.countdownSeconds -= 1
            DispatchQueue.main.async {
                self.countdownLabel.text = "\(self.countdownSeconds)"
            }

            if self.countdownSeconds <= 0 {
                timer.invalidate()
                self.countdownTimer = nil
                self.countdownLabel.isHidden = true
                self.captureSelfie()
            }
        }
    }

    private func updateCountdownLabel() {
        DispatchQueue.main.async {
            self.countdownLabel.text = "\(self.countdownSeconds)"
        }
    }

    private func captureSelfie() {
        currentState = .loading  // Add this line
        DispatchQueue.main.async {
            self.loadingIndicator.startAnimating()
        }

        let settings = AVCapturePhotoSettings()
        cameraOutput.capturePhoto(with: settings, delegate: self)
        // DispatchQueue.main.async {
        //     self.captureSession.stopRunning()
        // }
    }

    @objc private func handleOrientationChange(for orientation: UIDeviceOrientation) {
        DispatchQueue.main.async {
            if orientation.isLandscape {
                self.isPortraitMode = false
                self.showOrientationWarning()
                self.resetDetectionState()
            } else if orientation.isPortrait {
                self.isPortraitMode = true
                self.hideOrientationWarning()
                self.restartDetectionIfNeeded()
            }
            self.faceBoundingBoxLayer?.path = nil
            // Update previewLayer frame
            self.previewLayer.frame = self.view.bounds
        }
    }

    private func showOrientationWarning() {
        orientationWarningLabel.isHidden = false
        translucentBox.isHidden = true
        countdownLabel.isHidden = true
        loadingIndicator.stopAnimating()
    }

    private func hideOrientationWarning() {
        orientationWarningLabel.isHidden = true
        translucentBox.isHidden = false
    }

    private func resetDetectionState() {
        currentState = .waitingForBlink
        countdownTimer?.invalidate()
        countdownTimer = nil
        countdownLabel.isHidden = true
        loadingIndicator.stopAnimating()
        statusLabel.text = "Please place your face in the camera view"
        lastEyeState = false
    }

    private func restartDetectionIfNeeded() {
        if captureSession?.isRunning == false && imageCaptured == false {
            captureSession.startRunning()
        }
        if imageCaptured {
            captureSession.stopRunning()
        }
    }

    override func viewWillTransition(
        to size: CGSize, with coordinator: UIViewControllerTransitionCoordinator
    ) {
        super.viewWillTransition(to: size, with: coordinator)

        coordinator.animate(
            alongsideTransition: { _ in
                // Update previewLayer frame
                self.previewLayer.frame = CGRect(origin: .zero, size: size)

                // Update UI elements
                self.orientationWarningLabel.frame = CGRect(
                    x: 20, y: 100, width: size.width - 40, height: 100)
                self.translucentBox.frame = CGRect(
                    x: 20, y: 100, width: size.width - 40, height: 50)
                self.countdownLabel.frame = CGRect(
                    x: (size.width - 100) / 2, y: size.height / 2 - 50, width: 100, height: 100)
            }, completion: nil)
    }

    private func startDeviceMotionUpdates() {
        guard motionManager.isDeviceMotionAvailable else {
            print("Device motion is not available")
            return
        }

        motionManager.deviceMotionUpdateInterval = 0.1  // Update every 0.1 seconds
        motionManager.startDeviceMotionUpdates(to: .main) { [weak self] (motion, error) in
            guard let self = self, let motion = motion else { return }

            let gravity = motion.gravity
            let orientation: UIDeviceOrientation

            if gravity.x >= 0.5 {
                orientation = .landscapeRight
            } else if gravity.x <= -0.5 {
                orientation = .landscapeLeft
            } else if gravity.y <= -0.5 {
                orientation = .portrait
            } else if gravity.y >= 0.5 {
                orientation = .portraitUpsideDown
            } else {
                orientation = .unknown
            }

            self.handleOrientationChange(for: orientation)
        }
    }

    private func convertFaceBoundingBox(_ face: VNFaceObservation) -> CGRect? {
        guard let deviceInput = captureSession.inputs.first as? AVCaptureDeviceInput else {
            return nil
        }
        let device = deviceInput.device
        let format = device.activeFormat
        let dimensions = CMVideoFormatDescriptionGetDimensions(format.formatDescription)
        let imageWidth = CGFloat(dimensions.width)
        let imageHeight = CGFloat(dimensions.height)

        let visionBoundingBox = face.boundingBox

        // Convert Vision's normalized coordinates to image coordinates
        let imageRect = VNImageRectForNormalizedRect(
            visionBoundingBox, Int(imageWidth), Int(imageHeight))

        // Flip Y-axis to convert from Vision's bottom-left to image's top-left origin
        let flippedY = imageHeight - imageRect.origin.y - imageRect.height
        let flippedImageRect = CGRect(
            x: imageRect.origin.x, y: flippedY, width: imageRect.width, height: imageRect.height)

        // Convert to metadata output normalized rect (0.0-1.0) with top-left origin
        let metadataOutputRect = CGRect(
            x: flippedImageRect.origin.x / imageWidth,
            y: flippedImageRect.origin.y / imageHeight,
            width: flippedImageRect.width / imageWidth,
            height: flippedImageRect.height / imageHeight)

        // Convert to preview layer coordinates
        let layerRect = previewLayer.layerRectConverted(fromMetadataOutputRect: metadataOutputRect)
        return layerRect
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
        Inno.sharedInstance?.sendEvent(withName: "onScreenTimeout", body: 0)

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
            timeInterval: 180,  // 120 seconds
            target: self,
            selector: #selector(closeCameraAfterTimeout),
            userInfo: nil,
            repeats: false
        )
        print("Timer reset due to activity inside LivelinessDetectionViewController")
    }

}

extension LivelinessDetectionViewController: AVCaptureVideoDataOutputSampleBufferDelegate,
    AVCapturePhotoCaptureDelegate
{
    func captureOutput(
        _ output: AVCaptureOutput, didOutput sampleBuffer: CMSampleBuffer,
        from connection: AVCaptureConnection
    ) {
        guard let pixelBuffer = CMSampleBufferGetImageBuffer(sampleBuffer) else { return }
        let requestHandler = VNImageRequestHandler(cvPixelBuffer: pixelBuffer, options: [:])
        do {
            try requestHandler.perform([faceDetectionRequest])
        } catch {
            print("Face detection error: \(error)")
        }
    }

    func photoOutput(
        _ output: AVCapturePhotoOutput, didFinishProcessingPhoto photo: AVCapturePhoto,
        error: Error?
    ) {
        if let error = error {
            print("Photo capture error: \(error)")
            return
        }

        guard let imageData = photo.fileDataRepresentation() else { return }

        guard let originalImage = UIImage(data: imageData),
            let compressedData = originalImage.jpegData(compressionQuality: 0.3)
        else {
            print("Error: Could not compress image")
            return
        }

        SharedViewModel.shared.selfieImage = compressedData
        print(SharedViewModel.shared.selfieImage, "--------KB-------------------========KB")
        loadingIndicator.startAnimating()

        // No need to get fileDataRepresentation() again - use the compressed data we already have
        guard let selfieImageData = SharedViewModel.shared.selfieImage else {
            print("Error: Selfie image data is nil after compression")
            return
        }

        print("✅ Selfie Image stored successfully!")

        // Rest of the code remains the same...
        guard let croppedFaceUrlString = SharedViewModel.shared.ocrResponse?.imageUrl,
            let croppedFaceUrl = URL(string: croppedFaceUrlString)
        else {
            print("❌ Error: Cropped Face Image URL is missing or invalid")
            return
        }

        print("🔄 Downloading Cropped Face Image...")
        let downloadTask = URLSession.shared.dataTask(with: croppedFaceUrl) {
            (data, response, error) in
            if let error = error {
                print("❌ Error downloading cropped face: \(error)")
                return
            }

            guard let croppedFaceData = data else {
                print("❌ Error: No data received for cropped face")
                return
            }

            print("✅ Cropped Face Image downloaded successfully!")
            SharedViewModel.shared.croppedFaceImageData = croppedFaceData

            DispatchQueue.main.asyncAfter(deadline: .now() + 3) {
                self.callVerificationAPI()
            }
        }
        downloadTask.resume()
    }

    private func callVerificationAPI() {
        print("🔄 Starting Verification API Call...")

        guard let croppedFaceData = SharedViewModel.shared.croppedFaceImageData,
            let selfieImageData = SharedViewModel.shared.selfieImage,
            let referenceID = SharedViewModel.shared.referenceNumber
        else {
            print("❌ Missing required data for verification API")
            return
        }
        self.resetDetectionState()
        imageCaptured = true
        let formData = MultipartFormData()
        formData.addFileData(
            croppedFaceData, fieldName: "reference_image",
            fileName: referenceID + "_profile_image.jpg", mimeType: "image/jpeg")
        formData.addFileData(
            selfieImageData, fieldName: "candidate_image", fileName: referenceID + "_selfie.jpg",
            mimeType: "image/jpeg")
        formData.addTextField(referenceID, fieldName: "reference_id")

        // var request = URLRequest(url: URL(string: "https://api-innovitegra.online/innomatcher/verify-images")!)
        var request = URLRequest(
            url: URL(string: "https://api.innovitegrasuite.online/neuro/verify")!)
        request.httpMethod = "POST"
        request.setValue(formData.contentType, forHTTPHeaderField: "Content-Type")
        request.setValue("testapikey", forHTTPHeaderField: "api-key")
        request.httpBody = formData.build()
        let task = URLSession.shared.dataTask(with: request) { (data, response, error) in
            DispatchQueue.main.async {
                if let error = error {
                    print("❌ Error calling verify-images API: \(error)")
                    return
                }

                guard let data = data, let httpResponse = response as? HTTPURLResponse,
                    httpResponse.statusCode == 200
                else {
                    print("❌ Invalid response from verify-images API")
                    return
                }
                print(data, "---------------------------")

                do {
                    let jsonObject =
                        try JSONSerialization.jsonObject(with: data, options: []) as? [String: Any]
                    print("✅ Verification API Response:", jsonObject ?? "No Data")

                    // Stop Loading Indicator
                    self.loadingIndicator.stopAnimating()
                    print(jsonObject, "---------------------------")
                    print(jsonObject?["verification_status"], "---------------------------")
                    print(referenceID, "---------------------------")

                    // In your ViewController's callVerificationAPI function, after dismissing the view controllers:
                    if let rootVC = UIApplication.shared.connectedScenes
                        .filter({ $0.activationState == .foregroundActive })
                        .compactMap({ $0 as? UIWindowScene })
                        .first?.windows
                        .filter({ $0.isKeyWindow }).first?.rootViewController
                    {

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

                        rootVC.dismiss(animated: true) {
                            print("✅ All native screens closed, returning to React Native")
    
                            // Assuming jsonObject is your dictionary, e.g.,
                            // Optional(["verification_status": "succeeded", "score": 111, "success": 1])
                            guard let jsonData = jsonObject as? [String: Any],
                                let success = jsonData["success"] as? Int else {
                                print("Error: Could not retrieve the 'success' value from jsonObject.")
                                return
                            }
                            
                            // Determine the value to send: "1" if success equals 1, else "0"
                            let valueToSend = (success == 1) ? "1" : "0"
                            
                            let bridge = LivelinessDetectionBridge()
                            bridge.sendSessionTimeout(valueToSend)
                        }
                    } else {
                        print("❌ Failed to find root view controller")
                    }
                } catch {
                    print("❌ Error parsing verify-images response: \(error)")
                }
            }
        }
        task.resume()
    }

    func getTopViewController(
        _ rootViewController: UIViewController? = UIApplication.shared.connectedScenes
            .compactMap { ($0 as? UIWindowScene)?.keyWindow }
            .first?.rootViewController
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

}
