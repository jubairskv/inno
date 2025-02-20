import Foundation

class MultipartFormData {
    private var boundary: String
    private var httpBody = Data()

    var contentType: String {
        return "multipart/form-data; boundary=\(boundary)"
    }

    init() {
        self.boundary = "Boundary-\(UUID().uuidString)"
    }

    func addTextField(_ value: String, fieldName: String) {
        let fieldString = "--\(boundary)\r\n"
            + "Content-Disposition: form-data; name=\"\(fieldName)\"\r\n\r\n"
            + "\(value)\r\n"
        httpBody.append(Data(fieldString.utf8))
    }

    func addFileData(_ fileData: Data, fieldName: String, fileName: String, mimeType: String) {
        let fieldString = "--\(boundary)\r\n"
            + "Content-Disposition: form-data; name=\"\(fieldName)\"; filename=\"\(fileName)\"\r\n"
            + "Content-Type: \(mimeType)\r\n\r\n"
        httpBody.append(Data(fieldString.utf8))
        httpBody.append(fileData)
        httpBody.append("\r\n".data(using: .utf8)!)
    }

    func build() -> Data {
        httpBody.append("--\(boundary)--\r\n".data(using: .utf8)!)
        return httpBody
    }
}
