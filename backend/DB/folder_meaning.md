Chức năng từng thư mục trong Backend:
Controllers: Nhận request, điều hướng tới service
Services: Nhận yêu cầu từ Controller, làm việc với repository để thực hiện logic nghiệp vụ
Repositories: Chịu trách nhiệm làm việc với DB
DTOs: Object chịu trách nhiệm chứa dữ liệu từ request và dùng để trả dữ liệu cho người dùng từ controller
Models: Object chính đại diện cho từng object có trong phần mềm và là object để serivce dùng để xử lý logic
Exceptions: Chứa những custom Exception (Mỗi exception sẽ có các field cần thiết để  hỗ  trợ báo lỗi cho người dùng khi gặp lỗi)
