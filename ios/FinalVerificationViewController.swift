import UIKit

class FinalVerificationViewController: UIViewController {
    
    private let scrollView = UIScrollView()
    private let contentStack = UIStackView()
    
    // Shared view model reference
    private let sharedVM = SharedViewModel.shared
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setupUI()
        populateData()
    }
    
    private func setupUI() {
        view.backgroundColor = UIColor(white: 0.95, alpha: 1) // Light gray background
        
        // Configure scroll view
        scrollView.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(scrollView)
        
        // Configure stack view
        contentStack.axis = .vertical
        contentStack.spacing = 20
        contentStack.translatesAutoresizingMaskIntoConstraints = false
        scrollView.addSubview(contentStack)
        
        // Constraints
        NSLayoutConstraint.activate([
            scrollView.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor),
            scrollView.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 16),
            scrollView.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -16),
            scrollView.bottomAnchor.constraint(equalTo: view.bottomAnchor),
            
            contentStack.topAnchor.constraint(equalTo: scrollView.topAnchor),
            contentStack.leadingAnchor.constraint(equalTo: scrollView.leadingAnchor),
            contentStack.trailingAnchor.constraint(equalTo: scrollView.trailingAnchor),
            contentStack.bottomAnchor.constraint(equalTo: scrollView.bottomAnchor),
            contentStack.widthAnchor.constraint(equalTo: scrollView.widthAnchor)
        ])
    }
    
    private func populateData() {
        // Reference Number
        if let refNumber = sharedVM.referenceNumber {
            let refView = createLabelStack(texts: ["Reference Number:", refNumber])
            contentStack.addArrangedSubview(createCard(title: "Reference Details", contentView: refView))
        }
        
        // Verification Status & Score
        if let result = sharedVM.verificationResult {
            let status = result["verification_status"] as? String ?? "N/A"
            let score = result["score"] as? Double ?? 0.0
            let statusView = createLabelStack(texts: ["Status:", status, "Score:", String(format: "%.2f", score)])
            contentStack.addArrangedSubview(createCard(title: "Verification Status", contentView: statusView))
        }
        
        // Front ID Details
        if let ocr = sharedVM.ocrResponse {
            let frontDetails = [
                "Name:", ocr.fullName,
                "DOB:", ocr.dob,
                "Gender:", ocr.sex,
                "Nationality:", ocr.nationality,
                "FCN:", ocr.fcn,
                "Expiry:", ocr.dateOfExpiry
            ]
            let frontView = createLabelStack(texts: frontDetails)
            contentStack.addArrangedSubview(createCard(title: "Front ID Details", contentView: frontView))
        }
        
        // Back ID Details
        if let ocrBack = sharedVM.ocrResponseBack {
            let backDetails = [
                "Phone:", ocrBack.phoneNumber,
                "Region:", ocrBack.region,
                "Zone:", ocrBack.zone,
                "Woreda:", ocrBack.woreda,
                "FIN:", ocrBack.fin
            ]
            let backView = createLabelStack(texts: backDetails)
            contentStack.addArrangedSubview(createCard(title: "Back ID Details", contentView: backView))
        }
        
        // ✅ Add images in the correct order
        addImageSections()
        
        // ✅ Add Close Button at the Bottom
        addCloseButton()
    }
    
    private func addImageSections() {
        // Front ID Image
        if let frontImage = sharedVM.frontImage {
            contentStack.addArrangedSubview(createImageCard(title: "Front ID Image", image: frontImage))
        }
        
        // Back ID Image
        if let backImage = sharedVM.backImage {
            contentStack.addArrangedSubview(createImageCard(title: "Back ID Image", image: backImage))
        }
        
        // ✅ Cropped Face Image (comes first before selfie)
        // if let ocr = sharedVM.ocrResponse, let imageUrl = URL(string: ocr.imageUrl) {
        //     loadImageFromURL(url: imageUrl) { image in
        //         DispatchQueue.main.async {
        //             if let image = image {
        //                 self.contentStack.addArrangedSubview(self.createImageCard(title: "Cropped Face Image", image: image))
        //             }
        //         }
        //     }
        // }
        if let croppedFaceImage = sharedVM.faceCropped {
            contentStack.addArrangedSubview(createImageCard(title: "Cropped Face Image", image: croppedFaceImage))
        }
        
        // ✅ Selfie Image should come **right before the close button**
        if let selfieData = sharedVM.selfieImage, let image = UIImage(data: selfieData) {
            contentStack.addArrangedSubview(createImageCard(title: "Selfie Image", image: image))
        }
    }

    private func addCloseButton() {
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

        print("✅ SharedViewModel reset successfully")


        let closeButton = UIButton(type: .system)
        closeButton.setTitle("Close", for: .normal)
        closeButton.backgroundColor = .systemBlue  // ✅ Changed to blue
        closeButton.setTitleColor(.white, for: .normal)
        closeButton.layer.cornerRadius = 10
        closeButton.translatesAutoresizingMaskIntoConstraints = false
        closeButton.addTarget(self, action: #selector(closeTapped), for: .touchUpInside)
        
        // ✅ Ensure the close button is **LAST**
        contentStack.addArrangedSubview(closeButton)
        
        NSLayoutConstraint.activate([
            closeButton.heightAnchor.constraint(equalToConstant: 50),
            closeButton.centerXAnchor.constraint(equalTo: contentStack.centerXAnchor),
            closeButton.leadingAnchor.constraint(equalTo: contentStack.leadingAnchor),  // ✅ Full width
            closeButton.trailingAnchor.constraint(equalTo: contentStack.trailingAnchor)
        ])
    }
    
    @objc private func closeTapped() {
    // ✅ Dismiss the native Swift UI and return to React Native
        DispatchQueue.main.async {
            if let rootVC = UIApplication.shared.connectedScenes
                .compactMap({ ($0 as? UIWindowScene)?.keyWindow })
                .first?.rootViewController {
                
                // ✅ Remove all presented view controllers
                rootVC.dismiss(animated: true) {
                    print("✅ All native screens closed, returning to React Native")
                }
            } else {
                print("❌ Failed to find root view controller")
            }
        }
    }
    
    private func createCard(title: String, contentView: UIView) -> UIView {
        let card = UIView()
        card.backgroundColor = .white
        card.layer.cornerRadius = 10
        card.layer.shadowRadius = 2
        card.layer.shadowOpacity = 0.1
        
        let titleLabel = UILabel()
        titleLabel.text = title
        titleLabel.font = UIFont.boldSystemFont(ofSize: 18)
        
        let stack = UIStackView(arrangedSubviews: [titleLabel, contentView])
        stack.axis = .vertical
        stack.spacing = 8
        stack.translatesAutoresizingMaskIntoConstraints = false
        
        card.addSubview(stack)
        
        NSLayoutConstraint.activate([
            stack.topAnchor.constraint(equalTo: card.topAnchor, constant: 12),
            stack.leadingAnchor.constraint(equalTo: card.leadingAnchor, constant: 16),
            stack.trailingAnchor.constraint(equalTo: card.trailingAnchor, constant: -16),
            stack.bottomAnchor.constraint(equalTo: card.bottomAnchor, constant: -12)
        ])
        
        return card
    }
    
    private func createLabelStack(texts: [String]) -> UIStackView {
        let stack = UIStackView()
        stack.axis = .vertical
        stack.spacing = 4
        
        for i in stride(from: 0, to: texts.count, by: 2) {
            let row = UIStackView()
            row.axis = .horizontal
            row.spacing = 8
            
            let keyLabel = UILabel()
            keyLabel.text = texts[i]
            keyLabel.font = UIFont.systemFont(ofSize: 14)
            keyLabel.setContentHuggingPriority(.required, for: .horizontal)
            
            let valueLabel = UILabel()
            valueLabel.text = i+1 < texts.count ? texts[i+1] : ""
            valueLabel.font = UIFont.systemFont(ofSize: 14, weight: .medium)
            
            row.addArrangedSubview(keyLabel)
            row.addArrangedSubview(valueLabel)
            stack.addArrangedSubview(row)
        }
        
        return stack
    }
     private func loadImageFromURL(url: URL, completion: @escaping (UIImage?) -> Void) {
        URLSession.shared.dataTask(with: url) { data, _, _ in
            if let data = data {
                completion(UIImage(data: data))
            } else {
                completion(nil)
            }
        }.resume()
    }
    private func createImageCard(title: String, image: UIImage) -> UIView {
        let imageView = UIImageView(image: image)
        imageView.contentMode = .scaleAspectFit
        imageView.heightAnchor.constraint(equalToConstant: 200).isActive = true
        return createCard(title: title, contentView: imageView)
    }
    
   
}
