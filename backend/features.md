User story:
Người bán cần quản lý lượng lớn xe điện trẻ em trong một phiên làm và cần phần mềm đó tùy chỉnh giá 

Tính năng cho người bán:
- Quản lý phiên làm
    - Tạo phiên làm mới (Trạng thái mặc định Waiting)
      input:
        + Ngày
        + Thời gian
    - Bắt đầu phiên làm
      input: 
        + Danh sách mã nhận diện xe (Ở giao diện có thể là chọn xe từ danh sách drop down)
        Nếu không phải trong phiên làm được tạo trước => Tạo một session mới với trạng thái InProgress và yêu cầu thêm Input: 
        + Thời gian kết thúc phiên
        Nếu không cung cấp thời gian kết thúc phiên mặc định đặt là 12 giờ đêm
    - Kết thúc phiên làm
    - Xem thống kê phiên làm hiện tại
    - Xem lịch sử phiên làm
- Quản lý xe điện
    - Thêm xe điện (Trạng thái mặc định là nghỉ)
        input:
        + Tên nhận diện xe
    - Sửa thông tin xe
        - Sửa tên nhận diện xe 
    - Chuyển trạng thái 
    - Xóa xe điện
- Quản lý vé
    - Tìm vé theo số đt hoặc mã vé
    - Tạo vé mới (Mọi vé tạo mới mặc định sẽ được tính là đã thanh toán)
      input:
        + Số điện thoại
        + Phương thức thanh toán (Cash/Bank)
        + Thời gian
      output:
        + Mã số  vé tương ứng (Tăng dần và reset lại mỗi phiên làm)
    - Hủy vé (Thông báo số tiền đã thanh toán của vé đó để trả lại khách, không được hủy nếu trạng thái là đang dùng)
    - Đổi xe
      input:
        + Tên nhận diện xe
    - Trả xe (Thông báo phụ phí trả xe muộn nếu có, nếu trả xe sớm check cài đặt xem có bật cho phép hoàn tiền, nếu có trả lại số tiền tương ứng với số  tiền/phút còn lại)
    - Xem lịch sử vé trong phiên làm
    - Gia hạn thêm cho vé
      input:
        + Số phút gia hạn thêm 
- Cài đặt
    - Cho phép hoàn tiền {/api/settings/toggle_refund}
    - Bật/tắt gia hạn thêm giờ {/api/settings/toggle_extend}
    - Đặt ? tiền/? phút mặc định (Vd: 15k/30p, 30k/60p) {/api/settings/}
    - Phụ phí khi trả xe quá giờ (tiền/phút) {/api/settings/set_overdue_fee}
    - Đặt thời gian muộn trước khi tính phụ phí {/api/settings/set_overdue_limit}
    - Đặt thời gian tối thiểu để thuê xe {/api/settings/set_minimum_time}
    - Đăng xuất {/api/settings/logout} (Gửi API để chuyển trạng thái account người dùng từ online thành offline đồng thời xóa hoàn toàn cookie cũ, toàn bộ API gửi tới bằng người dùng đó sẽ bị từ chối)
Note: Trong DB luôn lưu cookie của người dùng để đối chiếu với cookie người dùng gửi chung với API nếu lệch thì từ chối
Trạng thái phiên:
- Chưa tới (Waiting)
- Đang làm (InProgress)
- Kết thúc (Ended)

Trạng thái xe:
- Chờ (Online)
- Sửa chữa (Repairing)
- Nghỉ (Offline)
- Đang dùng (Inused)

Trạng thái vé:
- Hoàn thành (Completed)
- Quá hạn (Overdue)
- Chờ (Waiting)
- Hủy (Canceled)
- Đang chạy (Inused)

Note: 
- Ngay khi vừa Trả xe sẽ thông báo gán xe cho vé tiếp theo trong hàng chờ
- Tên nhận diện sẽ là slug còn id của xe là số tăng dần