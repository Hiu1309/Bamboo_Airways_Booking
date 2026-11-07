# Spring Boot backend

Đây là backend Spring Boot của đồ án. Backend chạy ở port 4000 và kết nối
đến MySQL.

```powershell
$env:MYSQL_URL="jdbc:mysql://localhost:3306/bamboo_booking"
$env:MYSQL_USER="bamboo"; $env:MYSQL_PASSWORD="change-me"
.\mvnw.cmd spring-boot:run
```

Trước khi chạy, cần bật MySQL và tạo database `bamboo_booking`. Trong PowerShell,
cấu hình thông tin kết nối:

```powershell
$env:MYSQL_URL="jdbc:mysql://localhost:3306/bamboo_booking?useSSL=false&serverTimezone=Asia/Ho_Chi_Minh"
$env:MYSQL_USER="root"
$env:MYSQL_PASSWORD="MAT_KHAU_MYSQL"
$env:JPA_DDL_AUTO="update"
```

Chạy backend:

```powershell
.\mvnw.cmd spring-boot:run
```

Build và kiểm tra:

```powershell
.\mvnw.cmd clean verify
```

Kiểm tra health tại `http://localhost:4000/health`.

Các entity trong code tương ứng với user, sân bay, chuyến bay, ghế, booking,
hành khách, thanh toán, đánh giá và điểm thưởng. Không commit mật khẩu thật
vào Git.
