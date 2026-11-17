# Spring Boot backend

Đây là backend Spring Boot của đồ án. Backend chạy ở port 4000 và kết nối
đến MySQL của XAMPP.

Trước khi chạy, mở XAMPP Control Panel và bật **MySQL**. Tạo database
`bamboo_booking` bằng cách import file
[`bamboo_booking.database`](./bamboo_booking.database) trong phpMyAdmin
(`http://localhost/phpmyadmin`), hoặc chạy:

```sql
CREATE DATABASE bamboo_booking
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

Mặc định XAMPP dùng `root`, mật khẩu rỗng và port `3306`. Nếu bạn đã đổi
mật khẩu hoặc port trong XAMPP, sửa các biến môi trường bên dưới:

```powershell
$env:MYSQL_URL="jdbc:mysql://localhost:3306/bamboo_booking?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Ho_Chi_Minh"
$env:MYSQL_USER="root"
$env:MYSQL_PASSWORD=""
$env:JPA_DDL_AUTO="update"
```

Chạy backend:

```powershell
.\mvnw.cmd package -DskipTests
java -jar target\booking-api-1.0.0.jar
```

Build và kiểm tra:

```powershell
.\mvnw.cmd clean verify
```

Kiểm tra health tại `http://localhost:4000/health`.

Các entity trong code tương ứng với user, sân bay, chuyến bay, ghế, booking,
hành khách, thanh toán, đánh giá và điểm thưởng. Không commit mật khẩu thật
vào Git.
