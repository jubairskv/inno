import UIKit

class IdCardFrontCapturedViewController: UIViewController {
    var ocrData: OcrResponseFront?
    
    private let frontImageView = UIImageView()
    private let croppedFaceImageView = UIImageView()
    private let infoStackView = UIStackView()
    private let processBackButton = UIButton(type: .system)

    override func viewDidLoad() {
        super.viewDidLoad()
        
        if SharedViewModel.shared.ocrResponse == nil {
            print("❌ Error: OCR data is nil! in ID Card Front Captured View Controller")
        } else {
            print("✅ OCR Data Available: \(SharedViewModel.shared.ocrResponse!)")
        }

        setupUI()
        displayData()
    }

 private func setupUI() {
    view.backgroundColor = .white

    // ✅ Front ID Image (Top) with Border
    frontImageView.translatesAutoresizingMaskIntoConstraints = false
    frontImageView.contentMode = .scaleAspectFit
    frontImageView.layer.cornerRadius = 10
    frontImageView.layer.borderWidth = 2  // Border thickness
    frontImageView.layer.borderColor = UIColor.lightGray.cgColor  // Border color
    frontImageView.clipsToBounds = true
    view.addSubview(frontImageView)

    // ✅ Container for OCR Data + Cropped Image
    let infoContainerView = UIView()
    infoContainerView.translatesAutoresizingMaskIntoConstraints = false
    infoContainerView.layer.cornerRadius = 10
    infoContainerView.layer.borderWidth = 2  // Border thickness
    infoContainerView.layer.borderColor = UIColor.lightGray.cgColor  // Border color
    infoContainerView.backgroundColor = UIColor(white: 0.95, alpha: 1)  // Light lightGray background
    view.addSubview(infoContainerView)

    // ✅ Stack View for OCR Data (Inside the Container)
    infoStackView.axis = .vertical
    infoStackView.spacing = 10
    infoStackView.translatesAutoresizingMaskIntoConstraints = false
    infoContainerView.addSubview(infoStackView)

    // ✅ Cropped Face Image (Inside the Container)
    croppedFaceImageView.translatesAutoresizingMaskIntoConstraints = false
    croppedFaceImageView.contentMode = .scaleAspectFit
    croppedFaceImageView.layer.cornerRadius = 10
    croppedFaceImageView.clipsToBounds = true // Border for cropped face
    croppedFaceImageView.layer.borderColor = UIColor.lightGray.cgColor
    infoContainerView.addSubview(croppedFaceImageView)

    // ✅ Process Back ID Card Button
    processBackButton.setTitle("PROCESS BACK ID CARD", for: .normal)
    processBackButton.setTitleColor(.white, for: .normal)
    processBackButton.backgroundColor = UIColor.systemBlue
    processBackButton.layer.cornerRadius = 10
   processBackButton.backgroundColor = UIColor(red: 0x59/255.0, green: 0xD5/255.0, blue: 0xFF/255.0, alpha: 1.0)
    processBackButton.translatesAutoresizingMaskIntoConstraints = false
    processBackButton.addTarget(self, action: #selector(processBackButtonTapped), for: .touchUpInside)
    view.addSubview(processBackButton)

    // ✅ Constraints
    NSLayoutConstraint.activate([
        // Front Image View (Top)
        frontImageView.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor, constant: 20),
        frontImageView.centerXAnchor.constraint(equalTo: view.centerXAnchor),
        frontImageView.widthAnchor.constraint(equalTo: view.widthAnchor, multiplier: 0.9),
        frontImageView.heightAnchor.constraint(equalToConstant: 200),

        // Container for OCR Data + Cropped Image
        infoContainerView.topAnchor.constraint(equalTo: frontImageView.bottomAnchor, constant: 20),
        infoContainerView.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 20),
        infoContainerView.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -20),
        
        // OCR Data Stack View Inside the Container
        infoStackView.topAnchor.constraint(equalTo: infoContainerView.topAnchor, constant: 20),
        infoStackView.leadingAnchor.constraint(equalTo: infoContainerView.leadingAnchor, constant: 20),
        infoStackView.trailingAnchor.constraint(equalTo: infoContainerView.trailingAnchor, constant: -20),

        // Cropped Face Image Inside the Container
        croppedFaceImageView.topAnchor.constraint(equalTo: infoStackView.bottomAnchor, constant: 20),
        croppedFaceImageView.centerXAnchor.constraint(equalTo: infoContainerView.centerXAnchor),
        croppedFaceImageView.widthAnchor.constraint(equalTo: infoContainerView.widthAnchor, multiplier: 0.5),
        croppedFaceImageView.heightAnchor.constraint(equalToConstant: 150),
        croppedFaceImageView.bottomAnchor.constraint(equalTo: infoContainerView.bottomAnchor, constant: -20),

        // Process Back Button
        processBackButton.bottomAnchor.constraint(equalTo: view.safeAreaLayoutGuide.bottomAnchor, constant: -20),
        processBackButton.centerXAnchor.constraint(equalTo: view.centerXAnchor),
        processBackButton.widthAnchor.constraint(equalTo: view.widthAnchor, multiplier: 0.8),
        processBackButton.heightAnchor.constraint(equalToConstant: 50)
    ])
}


    private func displayData() {
        guard let ocrData = SharedViewModel.shared.ocrResponse else { return }

        // ✅ Set the front image
        frontImageView.image = SharedViewModel.shared.frontImage

        // ✅ Display OCR Data
        addInfoLabel(title: "Full Name", value: ocrData.fullName)
        addInfoLabel(title: "Date of Birth", value: ocrData.dob)
        addInfoLabel(title: "Sex", value: ocrData.sex)
        addInfoLabel(title: "Nationality", value: ocrData.nationality)
        addInfoLabel(title: "FCN", value: ocrData.fcn)
        addInfoLabel(title: "Date of Expiry", value: ocrData.dateOfExpiry ?? "N/A")

        print("Full Name: \(ocrData.fullName)")
        print("Date of Birth: \(ocrData.dob)")
        print("Sex: \(ocrData.sex)")
        print("Nationality: \(ocrData.nationality)")
        print("FCN: \(ocrData.fcn)")
        print("Date of Expiry: \(ocrData.dateOfExpiry ?? "N/A")")
        print("Displaying In Captured Screen-------------------")

        // ✅ Load Cropped Face Image from URL
        if let croppedFaceUrl = URL(string: ocrData.imageUrl) {
            DispatchQueue.global().async {
                if let data = try? Data(contentsOf: croppedFaceUrl),
                   let image = UIImage(data: data) {
                    DispatchQueue.main.async {
                      SharedViewModel.shared.faceCropped = image
                        self.croppedFaceImageView.image = image
                    }
                }
            }
        }
    }

    private func addInfoLabel(title: String, value: String) {
        let label = UILabel()
        label.text = "\(title): \(value)"
        label.font = UIFont.systemFont(ofSize: 16, weight: .medium)
        label.textColor = .black
        infoStackView.addArrangedSubview(label)
    }
    @objc private func processBackButtonTapped() {
        let backCameraViewController = BackCameraViewController()
//        backCameraViewController.resolve = resolve
//        backCameraViewController.reject = reject
        backCameraViewController.modalPresentationStyle = .fullScreen
        present(backCameraViewController, animated: true, completion: nil)
    }
    @objc private func processLiveliness() {
        print("🚀 Navigating to Liveliness Detection")
        // TODO: Implement navigation to liveliness detection screen
    }
 }
