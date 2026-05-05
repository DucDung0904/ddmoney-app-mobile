# Kế hoạch Phát triển Ứng dụng Quản lý Chi tiêu Cá nhân

Dựa trên sơ đồ tư duy (mindmap) bạn cung cấp, ứng dụng bao gồm 8 tính năng chính. Để quá trình phát triển diễn ra suôn sẻ, logic và tối ưu nhất, chúng ta sẽ chia việc thực hiện thành **4 giai đoạn (Phases)** từ các tính năng cốt lõi làm nền tảng cho đến các tính năng nâng cao và AI.

---

## Giai đoạn 1: Xây dựng Nền tảng (Core Foundation)
*Mục tiêu: Hoàn thiện luồng dữ liệu cơ bản nhất của một ứng dụng tài chính (Tiền vào, Tiền ra, và Nơi chứa tiền).*

### 1. Quản lý ví (Nhiều tài khoản, ngân hàng)
Đây là gốc rễ của dữ liệu. Người dùng cần có nơi để chứa tiền trước khi tiêu.
*   **Database:** Tạo bảng `Wallet` (Ví) với các thông tin: Tên ví (Tiền mặt, Thẻ ATM...), Loại ví, Số dư ban đầu, Đơn vị tiền tệ.
*   **Backend API:** Các API CRUD (Thêm, Đọc, Sửa, Xóa) cho Ví.
*   **Mobile UI/UX:** 
    *   Màn hình danh sách các ví hiện có và tổng tài sản.
    *   Màn hình thêm mới/chỉnh sửa thông tin ví.

### 2. Ghi chép giao dịch (Thu nhập, chi tiêu, danh mục)
Tính năng được sử dụng nhiều nhất, cần thiết kế giao diện sao cho việc nhập liệu diễn ra nhanh nhất (chỉ 2-3 chạm).
*   **Database:** 
    *   Bảng `Category` (Danh mục): Thu nhập (Lương, Thưởng...), Chi tiêu (Ăn uống, Di chuyển...). Nên có icon và màu sắc cho danh mục.
    *   Bảng `Transaction` (Giao dịch): Số tiền, Ghi chú, Ngày tháng, ID Danh mục, ID Ví nguồn/đích.
*   **Backend Logic:** Khi một giao dịch được tạo/sửa/xóa, phải **cập nhật lại số dư** của Ví tương ứng.
*   **Mobile UI/UX:**
    *   Nút "Thêm giao dịch" (FAB) luôn hiển thị.
    *   Màn hình danh sách lịch sử giao dịch (nhóm theo ngày/tháng).
    *   Màn hình điền thông tin giao dịch với bàn phím số tuỳ chỉnh.

---

## Giai đoạn 2: Phân tích & Kiểm soát (Analysis & Control)
*Mục tiêu: Giúp người dùng nhìn lại bức tranh tài chính và kìm hãm việc chi tiêu quá đà.*

### 3. Báo cáo (Biểu đồ, thống kê)
Dữ liệu khô khan cần được trực quan hóa để dễ hiểu.
*   **Backend Logic:** Viết các Query/API tổng hợp dữ liệu (Sum) theo khoảng thời gian (Tuần, Tháng, Năm) và theo từng Danh mục.
*   **Mobile UI/UX:** 
    *   Sử dụng thư viện biểu đồ (như Vico Chart, MPAndroidChart).
    *   **Biểu đồ tròn (Pie Chart):** Thể hiện cơ cấu chi tiêu (VD: 50% ăn uống, 20% mua sắm).
    *   **Biểu đồ cột (Bar Chart):** So sánh tổng Thu - Chi qua các tháng.

### 4. Đặt ngân sách (Cảnh báo vượt hạn mức)
Thiết lập các giới hạn chi tiêu.
*   **Database:** Bảng `Budget` (Ngân sách): Số tiền giới hạn, Tháng áp dụng, Danh mục áp dụng (VD: Ngân sách ăn uống tháng 5 là 3 triệu).
*   **Backend/Mobile Logic:** So sánh (Ngân sách) với (Tổng giao dịch chi tiêu của danh mục đó trong tháng).
*   **Mobile UI/UX:**
    *   Màn hình tạo ngân sách.
    *   Sử dụng Thanh tiến trình (Progress Bar) đổi màu: Xanh (an toàn) -> Vàng (sắp hết) -> Đỏ (vượt hạn mức).
    *   **Cảnh báo:** Hiển thị thông báo (Local Notification hoặc Banner trên app) khi tiến trình đạt 80% hoặc 100%.

---

## Giai đoạn 3: Tiện ích Nâng cao (Advanced Utilities)
*Mục tiêu: Tăng tính tương tác, tự động hóa và định hướng tương lai.*

### 5. Mục tiêu tiết kiệm (Theo dõi tiến độ)
Kích thích người dùng tích lũy tài sản cho các sự kiện (Mua xe, Du lịch...).
*   **Database:** Bảng `SavingGoal` (Mục tiêu): Tên, Số tiền cần đạt, Ngày mong muốn hoàn thành, Số dư hiện tại.
*   **Logic:** Chức năng "Chuyển tiền vào quỹ": Tạo một giao dịch chuyển tiền từ Ví -> Quỹ mục tiêu.
*   **Mobile UI/UX:** Giao diện trực quan với vòng biểu đồ % hoàn thành, tạo cảm giác thành tựu khi vòng tròn đầy dần.

### 6. Nhắc nhở (Thanh toán định kỳ)
Giải quyết bài toán quên đóng tiền điện, tiền nhà, tiền mạng.
*   **Database:** Bảng `Reminder`: Tiêu đề, Số tiền, Chu kỳ lặp lại (Hàng tuần, hàng tháng, ngày mùng 5 hàng tháng...).
*   **Logic (Mobile):** Sử dụng `WorkManager` hoặc `AlarmManager` của Android để cài đặt hẹn giờ kích hoạt thông báo (Local Notification) ngay cả khi không mở app.
*   **Tự động hóa (Tùy chọn):** Nút "Thanh toán ngay" trên màn hình nhắc nhở để tự động sinh ra một Giao dịch mới mà không cần nhập tay.

---

## Giai đoạn 4: Dữ liệu lớn & Trí tuệ nhân tạo (Data & AI)
*Mục tiêu: Đưa ứng dụng lên đẳng cấp chuyên nghiệp, an toàn dữ liệu và thông minh.*

### 7. Xuất dữ liệu (Excel, Cloud Backup)
*   **Xuất Excel/CSV:** 
    *   Trên Mobile hoặc Backend, sử dụng thư viện (như Apache POI trên Java) để biến danh sách `Transaction` thành file `.xlsx` và cho phép người dùng lưu về máy hoặc chia sẻ qua Zalo/Email.
*   **Cloud Backup:** (Với Spring Boot/MySQL hiện tại thì dữ liệu đã được lưu trên mây nếu backend deploy thật). Nếu là app Offline-first dùng SQLite, cần viết chức năng đồng bộ (Sync) dữ liệu lên Firebase Firestore hoặc Google Drive.

### 8. AI gợi ý thông minh (Phân tích thói quen chi tiêu)
Tính năng khác biệt nhất của app, giúp biến các con số thành lời khuyên thực tế.
*   **Cách thức 1 (Tích hợp LLM API - như Gemini/ChatGPT API):** Gửi dữ liệu chi tiêu tổng hợp của tháng (dưới dạng text/JSON ẩn danh) lên API và yêu cầu AI trả về đoạn văn bản nhận xét. VD: *"Bạn đã chi tiêu cho mua sắm gấp đôi tháng trước, hãy chú ý nhé"*.
*   **Cách thức 2 (Thuật toán tự xây dựng):** 
    *   Phát hiện chi tiêu bất thường (Anomaly Detection).
    *   **Auto-categorization:** Khi người dùng nhập ghi chú "trà sữa", AI tự động chọn danh mục "Ăn uống" mà người dùng không cần chọn thủ công.
*   **Mobile UI/UX:** Tạo một mục "Trợ lý ảo" hoặc "Insights" trên màn hình chính để hiển thị các thẻ lời khuyên này.

---

### Tóm tắt Lộ trình Khuyến nghị (Roadmap):
1. **Tuần 1-2:** Hoàn thiện Database, API Backend và UI cho **Giai đoạn 1** (Ví + Giao dịch).
2. **Tuần 3:** Xây dựng tính năng Báo cáo (Vẽ biểu đồ) và Đặt Ngân sách.
3. **Tuần 4:** Bổ sung tính năng Tiết kiệm và Hẹn giờ nhắc nhở.
4. **Tuần 5+:** Nghiên cứu tích hợp chức năng Xuất Excel và kết nối API Trí tuệ nhân tạo (AI).
