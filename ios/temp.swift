// im getting error like this   /Users/innovitegrasolutions/Documents/cam-ocr-lib/ios/CamOcrLib.swift:336:12 Initializer for conditional binding must have Optional type, not 'AVCapturePhotoOutput'
// for the code for capturing the photo func openCamera() {
//         captureSession = AVCaptureSession()
//         captureSession.sessionPreset = .photo

//         guard let backCamera = AVCaptureDevice.default(.builtInWideAngleCamera, for: .video, position: .back) else {
//             print("No back camera available")
//             return
//         }

//         do {
//             let input = try AVCaptureDeviceInput(device: backCamera)
//             captureSession.addInput(input)
//         } catch {
//             print("Error accessing camera: \(error)")
//             return
//         }

//         previewLayer = AVCaptureVideoPreviewLayer(session: captureSession)
//         previewLayer.videoGravity = .resizeAspectFill
//         previewLayer.frame = view.layer.bounds
//         view.layer.addSublayer(previewLayer)

//         // Add custom overlay UI
//         addCameraOverlay()

//         captureSession.startRunning()
//     }

//     func addCameraOverlay() {
//         // Semi-transparent background
//         let overlayView = UIView(frame: view.bounds)
//         overlayView.backgroundColor = UIColor.black.withAlphaComponent(0.1)
//         view.addSubview(overlayView)

//         let translucentBox = UIView()
//         translucentBox.translatesAutoresizingMaskIntoConstraints = false
//         translucentBox.backgroundColor = UIColor.black.withAlphaComponent(0.4)
//         translucentBox.layer.cornerRadius = 10
//         view.addSubview(translucentBox)

//         // Text: "Snap the front of your ID"
//         let snapLabel = UILabel()
//         snapLabel.text = "Snap the front of your ID"
//         snapLabel.textColor = .white
//         snapLabel.font = UIFont.boldSystemFont(ofSize: 20)
//         snapLabel.textAlignment = .center
//         snapLabel.translatesAutoresizingMaskIntoConstraints = false
//         overlayView.addSubview(snapLabel)

//         // Rectangle border box
//         let borderBox = UIView()
//         borderBox.layer.borderColor = UIColor.white.cgColor
//         borderBox.layer.borderWidth = 2.0
//         borderBox.translatesAutoresizingMaskIntoConstraints = false
//         overlayView.addSubview(borderBox)

//         // Capture button
//         captureButton = UIButton(type: .system)
//         captureButton.setTitle("Capture Front Side", for: .normal)
//         captureButton.titleLabel?.font = UIFont.boldSystemFont(ofSize: 18)
//         captureButton.backgroundColor = .blue
//         captureButton.setTitleColor(.white, for: .normal)
//         captureButton.layer.cornerRadius = 10
//         captureButton.translatesAutoresizingMaskIntoConstraints = false
//         captureButton.addTarget(self, action: #selector(capturePhoto), for: .touchUpInside)
//         overlayView.addSubview(captureButton)

//         // Constraints
//         NSLayoutConstraint.activate([
//             translucentBox.centerXAnchor.constraint(equalTo: view.centerXAnchor),
//             translucentBox.widthAnchor.constraint(equalTo: view.widthAnchor, multiplier: 0.8),
//             translucentBox.heightAnchor.constraint(equalToConstant: 80),
//             translucentBox.topAnchor.constraint(equalTo: overlayView.topAnchor, constant: 73),

//             snapLabel.centerXAnchor.constraint(equalTo: overlayView.centerXAnchor),
//             snapLabel.topAnchor.constraint(equalTo: overlayView.topAnchor, constant: 100),

//             borderBox.centerXAnchor.constraint(equalTo: overlayView.centerXAnchor),
//             borderBox.centerYAnchor.constraint(equalTo: overlayView.centerYAnchor),
//             borderBox.widthAnchor.constraint(equalToConstant: 300),
//             borderBox.heightAnchor.constraint(equalToConstant: 200),

//             captureButton.centerXAnchor.constraint(equalTo: overlayView.centerXAnchor),
//             captureButton.bottomAnchor.constraint(equalTo: overlayView.bottomAnchor, constant: -50),
//             captureButton.widthAnchor.constraint(equalToConstant: 200),
//             captureButton.heightAnchor.constraint(equalToConstant: 50)
//         ])
//     }


//     @objc func capturePhoto() {
//         let photoSettings = AVCapturePhotoSettings()
//         if let photoOutput = AVCapturePhotoOutput() {
//             if captureSession.canAddOutput(photoOutput) {
//                 captureSession.addOutput(photoOutput)
//                 photoOutput.capturePhoto(with: photoSettings, delegate: self)
//             }
//         }
//     }

//     func photoOutput(_ output: AVCapturePhotoOutput, didFinishProcessingPhoto photo: AVCapturePhoto, error: Error?) {
//         if let imageData = photo.fileDataRepresentation(), let image = UIImage(data: imageData) {
//             // Handle the captured image (e.g., save or process it)
//             print("Image captured: \(image)")
//             resolve?("Image captured successfully")
//             dismiss(animated: true, completion: nil)
//         } else {
//             reject?("CAPTURE_ERROR", "Failed to capture image", error)
//         }
//     } fix this