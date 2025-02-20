import UIKit

class DigitalFrontDetailsViewController: UIViewController {
    private let scrollView = UIScrollView()
    private let contentView = UIView()
    
    private let titleLabel = UILabel()
    private let faceImageView = UIImageView()
    private let frontOcrContainer = UIView() // Renamed for clarity
    private let frontOcrDataStackView = UIStackView() // Renamed for clarity
    private let uploadBackButton = UIButton(type: .system)

    override func viewDidLoad() {
        super.viewDidLoad()
        setupUI()
        setupGradientBackground()
        displayData()
    }

    private func setupUI() {
        view.backgroundColor = UIColor.lightGray.withAlphaComponent(0.2)

        // Scroll View Setup
        scrollView.translatesAutoresizingMaskIntoConstraints = false
        contentView.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(scrollView)
        scrollView.addSubview(contentView)

        NSLayoutConstraint.activate([
            scrollView.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor),
            scrollView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            scrollView.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            scrollView.bottomAnchor.constraint(equalTo: view.safeAreaLayoutGuide.bottomAnchor),
            
            contentView.topAnchor.constraint(equalTo: scrollView.topAnchor),
            contentView.leadingAnchor.constraint(equalTo: scrollView.leadingAnchor),
            contentView.trailingAnchor.constraint(equalTo: scrollView.trailingAnchor),
            contentView.bottomAnchor.constraint(equalTo: scrollView.bottomAnchor),
            contentView.widthAnchor.constraint(equalTo: scrollView.widthAnchor)
        ])

        // Title Label
        titleLabel.text = "ID Verification Results"
        titleLabel.font = UIFont.boldSystemFont(ofSize: 22)
        titleLabel.textAlignment = .center
        titleLabel.translatesAutoresizingMaskIntoConstraints = false
        contentView.addSubview(titleLabel)

        // Face Image View
        faceImageView.contentMode = .scaleAspectFill
        faceImageView.layer.cornerRadius = 50
        faceImageView.layer.masksToBounds = true
        faceImageView.translatesAutoresizingMaskIntoConstraints = false
        contentView.addSubview(faceImageView)

        // Front ID Container
        frontOcrContainer.backgroundColor = .white
        frontOcrContainer.layer.cornerRadius = 10
        frontOcrContainer.translatesAutoresizingMaskIntoConstraints = false
        contentView.addSubview(frontOcrContainer)

        // Front ID Stack View
        frontOcrDataStackView.axis = .vertical
        frontOcrDataStackView.spacing = 10
        frontOcrDataStackView.alignment = .fill
        frontOcrDataStackView.translatesAutoresizingMaskIntoConstraints = false
        frontOcrContainer.addSubview(frontOcrDataStackView)

        // Upload Back Button
        uploadBackButton.setTitle("Upload ID Back", for: .normal)
        uploadBackButton.backgroundColor = .blue
        uploadBackButton.setTitleColor(.white, for: .normal)
        uploadBackButton.layer.cornerRadius = 10
        uploadBackButton.backgroundColor = UIColor(red: 0x59/255.0, green: 0xD5/255.0, blue: 0xFF/255.0, alpha: 1.0)
        uploadBackButton.translatesAutoresizingMaskIntoConstraints = false
        uploadBackButton.addTarget(self, action: #selector(uploadIdBackTapped), for: .touchUpInside)
        view.addSubview(uploadBackButton)

        // Constraints
        NSLayoutConstraint.activate([
            titleLabel.topAnchor.constraint(equalTo: contentView.topAnchor, constant: 20),
            titleLabel.centerXAnchor.constraint(equalTo: contentView.centerXAnchor),

            faceImageView.topAnchor.constraint(equalTo: titleLabel.bottomAnchor, constant: 20),
            faceImageView.centerXAnchor.constraint(equalTo: contentView.centerXAnchor),
            faceImageView.widthAnchor.constraint(equalToConstant: 100),
            faceImageView.heightAnchor.constraint(equalToConstant: 100),

            frontOcrContainer.topAnchor.constraint(equalTo: faceImageView.bottomAnchor, constant: 20),
            frontOcrContainer.leadingAnchor.constraint(equalTo: contentView.leadingAnchor, constant: 20),
            frontOcrContainer.trailingAnchor.constraint(equalTo: contentView.trailingAnchor, constant: -20),

            frontOcrDataStackView.topAnchor.constraint(equalTo: frontOcrContainer.topAnchor, constant: 10),
            frontOcrDataStackView.leadingAnchor.constraint(equalTo: frontOcrContainer.leadingAnchor, constant: 10),
            frontOcrDataStackView.trailingAnchor.constraint(equalTo: frontOcrContainer.trailingAnchor, constant: -10),
            frontOcrDataStackView.bottomAnchor.constraint(equalTo: frontOcrContainer.bottomAnchor, constant: -10),

            uploadBackButton.bottomAnchor.constraint(equalTo: view.safeAreaLayoutGuide.bottomAnchor, constant: -20),
            uploadBackButton.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 20),
            uploadBackButton.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -20),
            uploadBackButton.widthAnchor.constraint(equalToConstant: 280),
            uploadBackButton.heightAnchor.constraint(equalToConstant: 55)
        ])
    }

    private func displayData() {
        let sharedVM = SharedViewModel.shared

        // Load Cropped Face Image
        if let faceUrl = URL(string: sharedVM.ocrResponse?.imageUrl ?? "") {
            DispatchQueue.global().async {
                if let data = try? Data(contentsOf: faceUrl), let image = UIImage(data: data) {
                    DispatchQueue.main.async {
                        self.faceImageView.image = image
                        sharedVM.faceCropped = image
                    }
                }
            }
        }

        // Load Front ID Data
        if let frontData = sharedVM.ocrResponse {
            addSectionHeading("Front ID Details", to: frontOcrDataStackView)
            addInfoLabel(to: frontOcrDataStackView, title: "Full Name", value: frontData.fullName)
            addInfoLabel(to: frontOcrDataStackView, title: "Date of Birth", value: frontData.dob)
            addInfoLabel(to: frontOcrDataStackView, title: "Sex", value: frontData.sex)
            addInfoLabel(to: frontOcrDataStackView, title: "Nationality", value: frontData.nationality)
            addInfoLabel(to: frontOcrDataStackView, title: "FCN", value: frontData.fcn)
            addInfoLabel(to: frontOcrDataStackView, title: "Date of Expiry", value: frontData.dateOfExpiry)
        }

        // Load Back ID Data (Conditional)
        if let backData = sharedVM.ocrResponseBack {
            // Create Back Container
            let backOcrContainer = UIView()
            backOcrContainer.backgroundColor = .white
            backOcrContainer.layer.cornerRadius = 10
            backOcrContainer.translatesAutoresizingMaskIntoConstraints = false
            contentView.addSubview(backOcrContainer)

            // Create Back Stack View
            let backOcrDataStackView = UIStackView()
            backOcrDataStackView.axis = .vertical
            backOcrDataStackView.spacing = 10
            backOcrDataStackView.alignment = .fill
            backOcrDataStackView.translatesAutoresizingMaskIntoConstraints = false
            backOcrContainer.addSubview(backOcrDataStackView)

            // Constraints for Back Container
            NSLayoutConstraint.activate([
                backOcrContainer.topAnchor.constraint(equalTo: frontOcrContainer.bottomAnchor, constant: 20),
                backOcrContainer.leadingAnchor.constraint(equalTo: contentView.leadingAnchor, constant: 20),
                backOcrContainer.trailingAnchor.constraint(equalTo: contentView.trailingAnchor, constant: -20),
                
                backOcrDataStackView.topAnchor.constraint(equalTo: backOcrContainer.topAnchor, constant: 10),
                backOcrDataStackView.leadingAnchor.constraint(equalTo: backOcrContainer.leadingAnchor, constant: 10),
                backOcrDataStackView.trailingAnchor.constraint(equalTo: backOcrContainer.trailingAnchor, constant: -10),
                backOcrDataStackView.bottomAnchor.constraint(equalTo: backOcrContainer.bottomAnchor, constant: -10)
            ])

            // Add Back Data
            addSectionHeading("Back ID Details", to: backOcrDataStackView)
            addInfoLabel(to: backOcrDataStackView, title: "Date of Expiry", value: backData.dateOfExpiry)
            addInfoLabel(to: backOcrDataStackView, title: "Phone Number", value: backData.phoneNumber)
            addInfoLabel(to: backOcrDataStackView, title: "Region", value: backData.region)
            addInfoLabel(to: backOcrDataStackView, title: "Zone", value: backData.zone)
            addInfoLabel(to: backOcrDataStackView, title: "Woreda", value: backData.woreda)
            addInfoLabel(to: backOcrDataStackView, title: "FIN", value: backData.fin)
            uploadBackButton.setTitle("Proceed To Liveliness", for: .normal)
        }
    }

    private func addSectionHeading(_ title: String, to stackView: UIStackView) {
        let headingLabel = UILabel()
        headingLabel.text = title
        headingLabel.font = UIFont.boldSystemFont(ofSize: 18)
        headingLabel.textColor = .blue
        stackView.addArrangedSubview(headingLabel)
    }

    private func addInfoLabel(to stackView: UIStackView, title: String, value: String) {
        let label = UILabel()
        label.text = "\(title): \(value)"
        label.font = UIFont.systemFont(ofSize: 16)
        label.textColor = .black
        stackView.addArrangedSubview(label)
    }

@objc private func uploadIdBackTapped() {
        if uploadBackButton.title(for: .normal) == "Proceed To Liveliness" {
        // Navigate to LivelinessDetectionViewController
        let livelinessDetectionVC = LivelinessDetectionViewController()
        livelinessDetectionVC.modalPresentationStyle = .fullScreen
        present(livelinessDetectionVC, animated: true, completion: nil)
    } else {
        // Handle the case for uploading the back ID (existing logic)
        let uploadBackId = UploadBackDigitalIDViewController()
        uploadBackId.modalPresentationStyle = .fullScreen

        if let topVC = getTopViewController(), topVC.view.window != nil {
            topVC.present(uploadBackId, animated: true, completion: {
                print("✅ Successfully presented DigitalFrontDetailsViewController")
            })
        } else {
            print("❌ Unable to find a valid top view controller to present.")
        }
    }
    }

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

    func getTopViewController(_ rootViewController: UIViewController? = UIApplication.shared.connectedScenes
    .compactMap { ($0 as? UIWindowScene)?.keyWindow }
    .first?.rootViewController) -> UIViewController? {

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