package com.dung.ddmoney.local

enum class SyncStatus {
    SYNCED,             // Đã khớp với Server
    PENDING_INSERT,     // Chờ đẩy lên Server (tạo mới)
    PENDING_UPDATE,     // Chờ cập nhật lên Server
    PENDING_DELETE      // Chờ xóa trên Server
}
