import UIKit

class IdCardFrontBackCapturedViewController: UIViewController {
    private let scrollView = UIScrollView()
    private let contentView = UIView()
    
    private let frontImageView = UIImageView()
    private let backImageView = UIImageView()
    private let croppedFaceImageView = UIImageView()
    private let frontIdLabel = UILabel()
    private let backIdLabel = UILabel()
    private let croppedFaceLabel = UILabel()
    private let frontDataStackView = UIStackView()
    private let backDataStackView = UIStackView()
    private let processButton = UIButton(type: .system)
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setupUI()
        displayData()
    }

    private func setupUI() {
        view.backgroundColor = .white

        // Scroll View
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

        // ✅ Use a Vertical StackView to prevent overlapping
        let mainStackView = UIStackView()
        mainStackView.axis = .vertical
        mainStackView.spacing = 20
        mainStackView.alignment = .fill
        mainStackView.translatesAutoresizingMaskIntoConstraints = false
        contentView.addSubview(mainStackView)

        NSLayoutConstraint.activate([
            mainStackView.topAnchor.constraint(equalTo: contentView.topAnchor, constant: 20),
            mainStackView.leadingAnchor.constraint(equalTo: contentView.leadingAnchor, constant: 20),
            mainStackView.trailingAnchor.constraint(equalTo: contentView.trailingAnchor, constant: -20),
            mainStackView.bottomAnchor.constraint(equalTo: contentView.bottomAnchor, constant: -20)
        ])

        // ✅ Add Labels & Images in Order
        addSection(to: mainStackView, title: "Front ID Card", imageView: frontImageView)
        addSection(to: mainStackView, title: "Back ID Card", imageView: backImageView)
        addSection(to: mainStackView, title: "Cropped Face", imageView: croppedFaceImageView)
        addStackView(to: mainStackView, title: "Front ID Data", stackViewToAdd: frontDataStackView)
        addStackView(to: mainStackView, title: "Back ID Data", stackViewToAdd: backDataStackView)

        // ✅ Process Button
        processButton.setTitle("Process to Liveliness Detection", for: .normal)
        processButton.backgroundColor = UIColor(red: 0x59/255.0, green: 0xD5/255.0, blue: 0xFF/255.0, alpha: 1.0)
        processButton.setTitleColor(.white, for: .normal)
        processButton.layer.cornerRadius = 10
        processButton.translatesAutoresizingMaskIntoConstraints = false
        mainStackView.addArrangedSubview(processButton)
        processButton.heightAnchor.constraint(equalToConstant: 50).isActive = true
        processButton.addTarget(self, action: #selector(processLiveliness), for: .touchUpInside)
    }

    
    private func setupLabel(_ label: UILabel, text: String) {
        label.text = text
        label.font = UIFont.boldSystemFont(ofSize: 18)
        contentView.addSubview(label)
    }
    
    private func setupImageView(_ imageView: UIImageView) {
        imageView.contentMode = .scaleAspectFit
        imageView.layer.borderWidth = 2
        imageView.layer.cornerRadius = 10
        imageView.layer.borderColor = UIColor.lightGray.cgColor
        imageView.translatesAutoresizingMaskIntoConstraints = false
        contentView.addSubview(imageView)
    }
    
    private func setupStackView(_ stackView: UIStackView, title: String) {
        let titleLabel = UILabel()
        titleLabel.text = title
        titleLabel.font = UIFont.boldSystemFont(ofSize: 18)
        contentView.addSubview(titleLabel)
        
        stackView.axis = .vertical
        stackView.spacing = 10
        stackView.distribution = .fillEqually
        stackView.translatesAutoresizingMaskIntoConstraints = false
        contentView.addSubview(stackView)
    }
    
    private func displayData() {
        frontImageView.image = SharedViewModel.shared.frontImage
        backImageView.image = SharedViewModel.shared.backImage
        
        if let croppedFaceUrl = URL(string: SharedViewModel.shared.ocrResponse?.imageUrl ?? "") {
            DispatchQueue.global().async {
                if let data = try? Data(contentsOf: croppedFaceUrl),
                   let image = UIImage(data: data) {
                    DispatchQueue.main.async {
                        self.croppedFaceImageView.image = image
                    }
                }
            }
        }
        
        if let frontData = SharedViewModel.shared.ocrResponse {
            addInfoLabel(to: frontDataStackView, title: "Full Name", value: frontData.fullName)
            addInfoLabel(to: frontDataStackView, title: "Date of Birth", value: frontData.dob)
            addInfoLabel(to: frontDataStackView, title: "Sex", value: frontData.sex)
            addInfoLabel(to: frontDataStackView, title: "Nationality", value: frontData.nationality)
            addInfoLabel(to: frontDataStackView, title: "FCN", value: frontData.fcn)
            addInfoLabel(to: frontDataStackView, title: "Date of Expiry", value: frontData.dateOfExpiry)
        }
        
        if let backData = SharedViewModel.shared.ocrResponseBack {
            addInfoLabel(to: backDataStackView, title: "Date of Expiry", value: backData.dateOfExpiry)
            addInfoLabel(to: backDataStackView, title: "Phone Number", value: backData.phoneNumber)
            addInfoLabel(to: backDataStackView, title: "Region", value: backData.region)
            addInfoLabel(to: backDataStackView, title: "Zone", value: backData.zone)
            addInfoLabel(to: backDataStackView, title: "Woreda", value: backData.woreda)
            addInfoLabel(to: backDataStackView, title: "FIN", value: backData.fin)
        }
    }
    
    private func addInfoLabel(to stackView: UIStackView, title: String, value: String) {
        let label = UILabel()
        label.text = "\(title): \(value)"
        label.font = UIFont.systemFont(ofSize: 16)
        label.textColor = .black
        stackView.addArrangedSubview(label)
    }
    
    @objc private func processLiveliness() {
        let livelinessDetectionVC = LivelinessDetectionViewController()
        livelinessDetectionVC.modalPresentationStyle = .fullScreen
        present(livelinessDetectionVC, animated: true, completion: nil)
    }
    private func addSection(to stackView: UIStackView, title: String, imageView: UIImageView) {
    let label = UILabel()
    label.text = title
    label.font = UIFont.boldSystemFont(ofSize: 18)
    label.textAlignment = .center

    imageView.contentMode = .scaleAspectFit
    imageView.layer.borderWidth = 2
    imageView.layer.cornerRadius = 10
    imageView.layer.borderColor = UIColor.lightGray.cgColor
    imageView.translatesAutoresizingMaskIntoConstraints = false
    imageView.heightAnchor.constraint(equalToConstant: 200).isActive = true

    let sectionStackView = UIStackView(arrangedSubviews: [label, imageView])
    sectionStackView.axis = .vertical
    sectionStackView.alignment = .fill

    stackView.addArrangedSubview(sectionStackView)
}
private func addStackView(to mainStackView: UIStackView, title: String, stackViewToAdd: UIStackView) {
    let container = UIView()
    container.backgroundColor = .white
    container.layer.borderWidth = 2
  container.layer.borderColor = UIColor.lightGray.cgColor
    container.layer.cornerRadius = 10
    container.translatesAutoresizingMaskIntoConstraints = false

    let titleLabel = UILabel()
    titleLabel.text = title
    titleLabel.font = UIFont.boldSystemFont(ofSize: 18)
    titleLabel.textAlignment = .center
    titleLabel.translatesAutoresizingMaskIntoConstraints = false

    stackViewToAdd.axis = .vertical
    stackViewToAdd.spacing = 10
    stackViewToAdd.distribution = .fillEqually
    stackViewToAdd.translatesAutoresizingMaskIntoConstraints = false

    container.addSubview(titleLabel)
    container.addSubview(stackViewToAdd)
    mainStackView.addArrangedSubview(container)

    NSLayoutConstraint.activate([
        container.widthAnchor.constraint(equalTo: mainStackView.widthAnchor),
        
        titleLabel.topAnchor.constraint(equalTo: container.topAnchor, constant: 10),
        titleLabel.centerXAnchor.constraint(equalTo: container.centerXAnchor),

        stackViewToAdd.topAnchor.constraint(equalTo: titleLabel.bottomAnchor, constant: 10),
        stackViewToAdd.leadingAnchor.constraint(equalTo: container.leadingAnchor, constant: 10),
        stackViewToAdd.trailingAnchor.constraint(equalTo: container.trailingAnchor, constant: -10),
        stackViewToAdd.bottomAnchor.constraint(equalTo: container.bottomAnchor, constant: -10)
    ])
}


}
