# Bamboo Airways Booking

Website đặt vé máy bay phục vụ đồ án môn học. Dự án gồm frontend React và
backend Spring Boot kết nối MySQL.

## 1. Công nghệ sử dụng

### Frontend

- React 19
- TypeScript
- Vite
- npm

### Backend

- Java 17
- Spring Boot 3.5.7
- Spring Data JPA
- Maven Wrapper
- JWT dùng cho đăng nhập

### Database

- MySQL Server
- SQLTools trong VS Code để xem bảng và chạy câu lệnh SQL

## 2. Cấu trúc thư mục

```text
Bamboo_Airways_Booking/
├── frontend/                         # Giao diện React
├── backend/
│   ├── src/main/java/                # Code Spring Boot
│   │   └── com/bambooairways/booking/
│   │       ├── controller/            # Nhận request từ frontend
│   │       ├── service/               # Xử lý nghiệp vụ đặt vé
│   │       ├── repository/            # Truy vấn database qua JPA
│   │       ├── entity/                # Mỗi bảng database một class riêng
│   │       ├── dto/                   # Dữ liệu request
│   │       ├── config/                # Cấu hình CORS
│   │       └── initializer/           # Tạo dữ liệu mẫu khi chạy lần đầu
│   ├── src/main/resources/           # Cấu hình ứng dụng
│   ├── pom.xml                       # Dependency backend
│   └── mvnw.cmd                      # Maven Wrapper cho Windows
└── README.md
```

## 3. Cài đặt lần đầu

Mỗi thành viên trong nhóm cần cài:

1. Git
2. Node.js 20 trở lên
3. Java JDK 17
4. MySQL Server
5. Extension `SQLTools` và `SQLTools MySQL/MariaDB Driver` trong VS Code

Kiểm tra cài đặt:

```powershell
git --version
node --version
java --version
```

## 4. Tạo database MySQL

Khởi động dịch vụ MySQL trong Windows Services. Sau đó mở SQLTools và chạy:

```sql
CREATE DATABASE bamboo_booking
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

Có thể dùng tài khoản `root` cho đồ án. Nếu dùng tài khoản riêng:

```sql
CREATE USER 'bamboo'@'localhost' IDENTIFIED BY 'change-me';
GRANT ALL PRIVILEGES ON bamboo_booking.* TO 'bamboo'@'localhost';
FLUSH PRIVILEGES;
```

Spring Boot sẽ tự tạo các bảng khi chạy lần đầu. Không cần tự viết lệnh
`CREATE TABLE`.

## 5. Chạy backend

Mở terminal thứ nhất:

```powershell
cd D:\test\qldapm\Bamboo_Airways_Booking\backend
```

Thiết lập thông tin MySQL trong terminal hiện tại. Thay mật khẩu bằng mật khẩu
MySQL của máy bạn:

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

Không đóng terminal này trong lúc sử dụng website.

Kiểm tra backend bằng trình duyệt:

```text
http://localhost:4000/health
```

Kết quả đúng:

```json
{
  "status": "ok",
  "service": "bamboo-airways-booking-api"
}
```

Sau khi backend chạy, có thể kiểm tra bảng trong SQLTools:

```sql
USE bamboo_booking;
SHOW TABLES;
```

## 6. Chạy frontend

Mở terminal thứ hai:

```powershell
cd D:\test\qldapm\Bamboo_Airways_Booking\frontend
npm.cmd install
npm.cmd run dev
```

Mở website:

```text
http://localhost:8443
```

Frontend chạy bằng Node.js/npm, còn backend chạy bằng Java/Maven. Hai phần này
cần chạy đồng thời trong hai terminal khác nhau.

## 7. Cách tổ chức backend

Backend dùng mô hình đơn giản phù hợp với đồ án:

```text
Controller -> Service -> Repository -> MySQL
                   |
                 Entity
```

- `controller`: nhận request, đọc dữ liệu đầu vào và gọi service.
- `service`: xử lý nghiệp vụ như đăng nhập, tìm chuyến, tạo booking và thanh toán
  mô phỏng.
- `repository`: dùng Spring Data JPA để đọc và ghi dữ liệu.
- `dto`: mô tả dữ liệu frontend gửi lên, giúp code dễ đọc hơn `Map` ở request.
- `entity`: mô tả các bảng trong database, mỗi entity nằm trong một file riêng.
- `initializer`: tạo tài khoản, sân bay và chuyến bay mẫu để nhóm dễ kiểm thử.

Các thành viên chỉ cần tìm đúng lớp theo chức năng, không cần học thêm
microservices hay các framework phức tạp. Khi thêm một chức năng mới, có thể
thực hiện theo thứ tự: thêm DTO nếu cần, viết hàm trong service, sau đó thêm
endpoint trong controller.

## 8. Tài khoản mẫu

Tài khoản quản trị:

```text
Username: admin
Password: Password123!
```

Tài khoản khách hàng:

```text
Username: minhanh
Password: Password123!
```

Dữ liệu mẫu được tạo tự động khi backend khởi động lần đầu.

## 9. Các chức năng chính

1. Đăng ký và đăng nhập.
2. Tìm chuyến bay theo sân bay, ngày bay và số hành khách.
3. Chọn hạng vé Economy hoặc Business.
4. Nhập thông tin hành khách.
5. Chọn ghế.
6. Thanh toán mô phỏng.
7. Nhận mã đặt chỗ.
8. Xem lịch sử điểm cơ bản.
9. Xem và gửi đánh giá.
10. API quản trị xem booking và thêm chuyến bay.

## 10. API chính

| Nhóm | Method | Endpoint | Chức năng |
|---|---|---|---|
| Auth | POST | `/api/auth/register` | Đăng ký |
| Auth | POST | `/api/auth/login` | Đăng nhập |
| Auth | POST | `/api/auth/refresh` | Làm mới token |
| Catalog | GET | `/api/airports` | Danh sách sân bay |
| Catalog | GET | `/api/flights` | Tìm chuyến bay |
| Booking | GET | `/api/flights/{id}/seats` | Xem ghế |
| Booking | POST | `/api/bookings` | Tạo booking |
| Booking | GET | `/api/bookings` | Lịch sử booking |
| Booking | GET | `/api/bookings/{code}` | Chi tiết booking |
| Payment | POST | `/api/bookings/{code}/payments` | Thanh toán demo |
| Review | GET | `/api/reviews` | Xem đánh giá |
| Review | POST | `/api/reviews` | Gửi đánh giá |
| Loyalty | GET | `/api/loyalty/transactions` | Lịch sử điểm |
| Admin | GET | `/api/admin/bookings` | Xem booking |
| Admin | POST | `/api/admin/flights` | Thêm chuyến bay |

Các API cần đăng nhập sử dụng header:

```text
Authorization: Bearer <access-token>
```

## 11. Phân công hoàn thiện đồ án

| Thành viên | Phạm vi chính | Kết quả cần bàn giao |
|---|---|---|
| Thành viên 1 | Đăng ký, đăng nhập, đăng xuất, refresh token, quên/đặt lại mật khẩu, tài khoản | Luồng xác thực hoàn chỉnh và test lỗi nhập sai |
| Thành viên 2 | Tìm chuyến, một chiều/khứ hồi, chọn hạng vé, hành khách, ghế và tạo booking | Luồng đặt vé từ trang chủ đến mã đặt chỗ |
| Thành viên 3 | Thanh toán mô phỏng, trạng thái booking, điểm thưởng và các tab thanh toán | Thanh toán cập nhật đúng booking và hiển thị lịch sử điểm |
| Thành viên 4 | Trang quản trị chuyến bay/booking và đánh giá | Phân quyền admin, thêm chuyến, xem booking, gửi/xem review |
| Thành viên 5 | Kiểm thử giao diện/API, sửa lỗi tích hợp, README và chuẩn bị GitHub | Bảng lỗi, kết quả test, hướng dẫn chạy và checklist demo |

Sau khi 5 phần hoàn tất, cả nhóm cùng review trên một nhánh tích hợp,
chạy `clean verify`, build frontend và kiểm tra lại luồng đặt vé từ đầu đến cuối.

## 12. Kiểm tra và build

Kiểm tra backend:

```powershell
cd backend
.\mvnw.cmd clean verify
```

Build frontend:

```powershell
cd frontend
npm.cmd run build
```

## 13. Làm việc nhóm bằng Git

Trước khi làm việc:

```powershell
git pull
```

Sau khi sửa code:

```powershell
git status
git add .
git commit -m "Mô tả ngắn thay đổi"
git push
```

Không commit các file sau:

- Mật khẩu MySQL thật.
- JWT secret thật.
- Thư mục `target`.
- File `.env`.

## 14. Một số lỗi thường gặp

### Không kết nối được MySQL

Kiểm tra MySQL Service đang ở trạng thái `Running` và port là `3306`.

### Access denied for user

Kiểm tra lại `MYSQL_USER` và `MYSQL_PASSWORD` trong terminal chạy backend.

### Không thấy bảng trong SQLTools

Đảm bảo đang chọn đúng database:

```sql
USE bamboo_booking;
SHOW TABLES;
```

Kiểm tra log backend phải có kết nối MySQL, không phải:

```text
jdbc:h2:mem:bamboo
```

### Frontend báo Failed to fetch

Kiểm tra backend trước:

```text
http://localhost:4000/health
```

Sau đó kiểm tra frontend đang gọi đúng port `4000`.

## 15. Ghi chú cho đồ án

Thanh toán hiện là thanh toán mô phỏng, phù hợp để trình bày luồng đặt vé.
Database sử dụng `ddl-auto: update` để Spring Boot tự tạo bảng khi chạy. Khi
thay đổi entity, cần kiểm tra lại các bảng trong SQLTools và thông báo cho các
thành viên trước khi push code.
