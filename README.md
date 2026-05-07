# 📱 DDMoney - Personal Finance Management App

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-blue.svg)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-Latest-green.svg)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Material_3-Premium-purple.svg)](https://m3.material.io)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

**DDMoney** là một ứng dụng quản lý tài chính cá nhân cao cấp dành cho Android, mang lại trải nghiệm tinh tế, chuyên nghiệp và đầy cảm hứng thay vì các bảng biểu khô khan truyền thống.

---

## Tính năng nổi bật

### 1. Quản lý Đa ví (Multi-Wallet)

- Theo dõi nhiều nguồn tiền cùng lúc: Tiền mặt, Thẻ ATM, Ví điện tử (MoMo, ZaloPay), Sổ tiết kiệm.
- Chuyển tiền linh hoạt giữa các ví (Transfer) với số dư được cập nhật thời gian thực.

### 2. Ghi chép Giao dịch Thông minh

- Nhập liệu siêu nhanh với bàn phím số (Number Pad) tích hợp.
- Phân loại giao dịch (Thu nhập/Chi tiêu) theo danh mục với biểu tượng (emoji) sinh động.
- Hệ thống nhắc nhở thanh toán định kỳ.

### 3. Phân tích & Báo cáo Trực quan

- Biểu đồ tròn (Pie Chart) phân tích cơ cấu chi tiêu.
- Biểu đồ cột (Bar Chart) so sánh Thu - Chi theo tuần/tháng.
- Theo dõi tiến độ Mục tiêu tiết kiệm (Savings Goals) với hiệu ứng vòng tròn phần trăm.

### 4. Ngân sách & Cảnh báo

- Thiết lập hạn mức chi tiêu cho từng danh mục.
- Hệ thống cảnh báo thông minh khi chi tiêu chạm ngưỡng 80% hoặc vượt quá ngân sách.

### 5. AI Insights (Chưa phát triển)

- Tự động phân loại giao dịch dựa trên mô tả.
- Gợi ý thói quen chi tiêu và lời khuyên tiết kiệm từ Trợ lý ảo AI.

---

## Thiết kế: "The Luminous Architect"

DDMoney không chỉ là một công cụ, mà là một tác phẩm nghệ thuật UI/UX:

- **Editorial Layout:** Bố cục dạng tạp chí, tạo không gian "thở" cho mắt.
- **Glassmorphism:** Hiệu ứng kính mờ cho thanh điều hướng và các thành phần nổi.
- **Intentional Asymmetry:** Sử dụng sự bất đối xứng có chủ đích để dẫn dắt thị giác.
- **Tonal Depth:** Tạo chiều sâu bằng các lớp màu (Surface Hierarchy) thay vì các đường kẻ (No-Line Rule).

---

## 🛠 Công nghệ sử dụng

### Mobile (Android)

- **Ngôn ngữ:** Kotlin
- **UI Framework:** Jetpack Compose (Modern Toolkit)
- **Kiến trúc:** MVVM (Model-View-ViewModel)
- **Local Database:** Room Persistence
- **Networking:** Retrofit & OkHttp
- **Dependency Injection:** Hilt / Koin (Optional)
- **Charts:** Vico Chart / MPAndroidChart
- **Async:** Kotlin Coroutines & Flow

### Backend (Sync)

- **Framework:** Spring Boot (Java)
- **Database:** MySQL
- **Security:** JWT Authentication

## Cài đặt & Phát triển

1.  **Clone repository:**
    ```bash
    git clone https://github.com/DucDung0904/ddmoney-app-mobile.git
    ```
2.  **Mở bằng Android Studio:** (Bản Hedgehog hoặc mới hơn).
3.  **Cấu hình API:**
4.  **Build & Run:** Chọn thiết bị emulator hoặc máy thật để trải nghiệm.

---

## Lộ trình phát triển (Roadmap)

- [x] **Giai đoạn 1:** Xây dựng nền tảng (Ví, Giao dịch, Danh mục).
- [x] **Giai đoạn 2:** Phân tích & Kiểm soát (Biểu đồ, Ngân sách).
- [ ] **Giai đoạn 3:** Tiện ích Nâng cao (Tiết kiệm, Nhắc nhở).
- [ ] **Giai đoạn 4:** AI & Cloud Sync (Tích hợp Gemini API, Backup dữ liệu).

---

## Liên hệ

- **Tác giả:** Trần Đức Dũng
- **GitHub:** [@DucDung0904](https://github.com/DucDung0904)
- **Email:** tranducdung090406@gmail.com
