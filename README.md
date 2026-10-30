# Bamboo Airways Booking

Website đặt vé máy bay Bamboo Airways gồm frontend React/Vite và backend REST API Node.js/Express.

## 1. Công nghệ sử dụng

### Frontend

- React 19
- TypeScript
- Vite
- Tailwind CSS

Thư mục: [frontend/](./frontend/)

### Backend

- Node.js
- Express
- TypeScript
- Prisma ORM
- SQLite
- JWT access token và refresh token
- Zod validation

Thư mục: [backend/](./backend/)

### Database

Dự án dùng SQLite nên không cần cài SQL Server, MySQL hoặc PostgreSQL khi chạy local.

Database được Prisma tự tạo tại:

```text
backend/prisma/dev.db
```

Schema database nằm tại [backend/prisma/schema.prisma](./backend/prisma/schema.prisma).

## 2. Cấu trúc dự án

```text
Bamboo_Airways_Booking/
├── frontend/
│   ├── src/
│   │   ├── App.tsx
│   │   └── index.css
│   ├── package.json
│   └── vite.config.ts
├── backend/
│   ├── prisma/
│   │   ├── schema.prisma
│   │   └── seed.ts
│   ├── src/
│   │   └── server.ts
│   ├── package.json
│   └── .env.example
└── README.md
```

## 3. Yêu cầu môi trường

- Node.js 20 trở lên
- npm

Kiểm tra phiên bản:

```powershell
node --version
npm.cmd --version
```

## 4. Cài đặt và chạy backend

Mở terminal thứ nhất:

```powershell
cd D:\test\qldapm\Bamboo_Airways_Booking\backend
Copy-Item .env.example .env
npm.cmd install
npx.cmd prisma generate
npx.cmd prisma db push
npm.cmd run db:seed
npm.cmd run dev
```

Backend chạy tại:

```text
http://localhost:4000
```

Kiểm tra backend:

```text
GET http://localhost:4000/health
```

Kết quả mong đợi:

```json
{
  "status": "ok",
  "service": "bamboo-airways-booking-api"
}
```

## 5. Cài đặt và chạy frontend

Mở terminal thứ hai:

```powershell
cd D:\test\qldapm\Bamboo_Airways_Booking\frontend
npm.cmd install
npm.cmd run dev
```

Frontend thường chạy tại:

```text
http://localhost:8443/
```

Frontend và backend là hai project Node.js riêng, vì vậy mỗi thư mục cần chạy `npm.cmd install` riêng.

## 6. Tài khoản mẫu

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

## 7. Luồng nghiệp vụ của website

Website hỗ trợ các luồng chính:

1. Khách hàng đăng ký tài khoản Bamboo Club.
2. Khách hàng đăng nhập.
3. Tìm chuyến bay theo:
   - Sân bay đi
   - Sân bay đến
   - Ngày bay
   - Số lượng hành khách
4. Chọn chuyến bay.
5. Chọn hạng vé Economy hoặc Business.
6. Nhập thông tin hành khách và thông tin liên lạc.
7. Chọn ghế trên sơ đồ máy bay.
8. Chọn phương thức thanh toán.
9. Nhận mã đặt chỗ và xem vé điện tử.
10. Xem lịch sử đặt vé và điểm thưởng.
11. Gửi đánh giá về dịch vụ.
12. Quản trị viên quản lý chuyến bay và booking.

## 8. API chính

Các endpoint backend nằm trong [backend/src/server.ts](./backend/src/server.ts).

| Nhóm    | Method | Endpoint                       | Mục đích                 |
| ------- | ------ | ------------------------------ | ------------------------ |
| Auth    | POST   | `/api/auth/register`           | Đăng ký tài khoản        |
| Auth    | POST   | `/api/auth/login`              | Đăng nhập                |
| Auth    | POST   | `/api/auth/refresh`            | Làm mới access token     |
| Auth    | POST   | `/api/auth/forgot-password`    | Yêu cầu đặt lại mật khẩu |
| Auth    | POST   | `/api/auth/reset-password`     | Đặt mật khẩu mới         |
| Catalog | GET    | `/api/airports`                | Danh sách sân bay        |
| Catalog | GET    | `/api/flights`                 | Tìm chuyến bay           |
| Booking | GET    | `/api/flights/:id/seats`       | Xem sơ đồ ghế            |
| Booking | POST   | `/api/bookings`                | Tạo booking và giữ ghế   |
| Booking | GET    | `/api/bookings`                | Xem lịch sử booking      |
| Booking | GET    | `/api/bookings/:code`          | Xem chi tiết booking     |
| Payment | POST   | `/api/bookings/:code/payments` | Tạo thanh toán           |
| Reviews | GET    | `/api/reviews`                 | Xem đánh giá             |
| Reviews | POST   | `/api/reviews`                 | Gửi đánh giá             |
| Loyalty | GET    | `/api/loyalty/transactions`    | Xem lịch sử điểm         |
| Admin   | GET    | `/api/admin/bookings`          | Quản lý booking          |
| Admin   | POST   | `/api/admin/flights`           | Tạo chuyến bay           |

Các endpoint yêu cầu đăng nhập cần gửi header:

```text
Authorization: Bearer <accessToken>
```

## 9. Database và Prisma

Sau khi chạy:

```powershell
npx.cmd prisma db push
npm.cmd run db:seed
```

Prisma sẽ:

- Tạo database SQLite.
- Tạo các bảng theo schema.
- Sinh Prisma Client.
- Thêm dữ liệu mẫu sân bay, máy bay, chuyến bay, hạng vé, ghế và tài khoản.

Mở giao diện xem database:

```powershell
cd D:\test\qldapm\Bamboo_Airways_Booking\backend
npx.cmd prisma studio
```

Prisma Studio thường chạy tại:

```text
http://localhost:5555
```

## 10. Build kiểm tra dự án

Build backend:

```powershell
cd D:\test\qldapm\Bamboo_Airways_Booking\backend
npm.cmd run build
```

Build frontend:

```powershell
cd D:\test\qldapm\Bamboo_Airways_Booking\frontend
npm.cmd run build
```

## 11. Thanh toán

Backend hiện có adapter thanh toán demo để hoàn thiện luồng đồ án. Các phương thức đã được mô hình hóa:

- Thẻ tín dụng/ghi nợ
- VietQR
- VNPAY-QR
- MoMo
- Thẻ ATM nội địa
- Trả sau

Khi triển khai thực tế, cần thay adapter demo bằng tích hợp chính thức với VNPAY/MoMo, xác minh chữ ký callback/webhook và chỉ chuyển booking sang `CONFIRMED` sau khi giao dịch được xác thực.

## 12. Lưu ý bảo mật

- Không commit file `backend/.env`.
- Đổi `JWT_ACCESS_SECRET` và `JWT_REFRESH_SECRET` trước khi triển khai.
- Không sử dụng mật khẩu tài khoản seed trong môi trường production.
- Database SQLite phù hợp cho local và đồ án; hệ thống production nhiều người dùng nên chuyển sang PostgreSQL hoặc MySQL.
