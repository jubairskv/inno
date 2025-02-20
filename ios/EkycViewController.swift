import UIKit

class EkycViewController: UIViewController {
    var resolve: RCTPromiseResolveBlock?
    var reject: RCTPromiseRejectBlock?

    override func viewDidLoad() {
        super.viewDidLoad()
        setupGradientBackground()
        setupButtons()
    }

    private func setupButtons() {
        // Title Label
        let titleLabel = UILabel()
        titleLabel.text = "Select ID Card Type"
        titleLabel.textAlignment = .center
        titleLabel.font = UIFont.boldSystemFont(ofSize: 25)
        titleLabel.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(titleLabel)

        // Instruction Label
        let instructionLabel = UILabel()
        instructionLabel.text = "Choose how you want to proceed with your eKYC verification"
        instructionLabel.textAlignment = .center
        instructionLabel.numberOfLines = 0
        instructionLabel.font = UIFont.systemFont(ofSize: 16)
        instructionLabel.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(instructionLabel)

        // Physical ID Button
        let physicalIDButton = createButton(title: "Physical ID", subtitle: "Capture physical card image", action: #selector(physicalIDButtonTapped))
        view.addSubview(physicalIDButton)

        // Digital ID Button
        let digitalIDButton = createButton(title: "Digital ID", subtitle: "Upload digital card image", action: #selector(digitalIDButtonTapped))
        view.addSubview(digitalIDButton)

        // Constraints
        NSLayoutConstraint.activate([
            titleLabel.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor, constant: 20),
            titleLabel.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 20),
            titleLabel.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -20),

            instructionLabel.topAnchor.constraint(equalTo: titleLabel.bottomAnchor, constant: 10),
            instructionLabel.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 20),
            instructionLabel.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -20),

            physicalIDButton.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 20),
            physicalIDButton.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -20),
            physicalIDButton.heightAnchor.constraint(equalToConstant: 80),
            physicalIDButton.bottomAnchor.constraint(equalTo: digitalIDButton.topAnchor, constant: -20),

            digitalIDButton.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 20),
            digitalIDButton.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -20),
            digitalIDButton.heightAnchor.constraint(equalToConstant: 80),
            digitalIDButton.bottomAnchor.constraint(equalTo: view.safeAreaLayoutGuide.bottomAnchor, constant: -20)
        ])
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

    private func createButton(title: String, subtitle: String, action: Selector) -> UIButton {
        let button = UIButton(type: .system)
        button.backgroundColor = .blue
        button.layer.cornerRadius = 10
        button.translatesAutoresizingMaskIntoConstraints = false
        button.addTarget(self, action: action, for: .touchUpInside)

        let stackView = UIStackView()
        stackView.axis = .vertical
        stackView.alignment = .center
        stackView.spacing = 5
        stackView.translatesAutoresizingMaskIntoConstraints = false
        stackView.isUserInteractionEnabled = false

        let titleLabel = UILabel()
        titleLabel.text = title
        titleLabel.font = UIFont.boldSystemFont(ofSize: 18)
        titleLabel.textColor = .white

        let subtitleLabel = UILabel()
        subtitleLabel.text = subtitle
        subtitleLabel.font = UIFont.systemFont(ofSize: 14)
        subtitleLabel.textColor = .white

        stackView.addArrangedSubview(titleLabel)
        stackView.addArrangedSubview(subtitleLabel)
        button.addSubview(stackView)

        NSLayoutConstraint.activate([
            stackView.centerXAnchor.constraint(equalTo: button.centerXAnchor),
            stackView.centerYAnchor.constraint(equalTo: button.centerYAnchor)
        ])

        return button
    }

    @objc private func digitalIDButtonTapped() {
    let uploadVC = UploadFrontDigitalIDViewController()
    uploadVC.modalPresentationStyle = .fullScreen
    present(uploadVC, animated: true)
}

    @objc private func physicalIDButtonTapped() {
        let cameraViewController = CameraViewController()
        cameraViewController.resolve = resolve
        cameraViewController.reject = reject
        cameraViewController.modalPresentationStyle = .fullScreen
        present(cameraViewController, animated: true, completion: nil)
    }
}
