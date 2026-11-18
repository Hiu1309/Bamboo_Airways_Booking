# Phan tich du an Bamboo Airways Booking

## 1. Tong quan du an

Bamboo Airways Booking la ung dung dat ve may bay phuc vu do an mon hoc.

- **Frontend:** React 19, TypeScript, Vite.
- **Backend:** Java 17, Spring Boot 3.5.7.
- **Database:** MySQL.
- **Truyen du lieu:** REST API.
- **Xac thuc:** JWT access token/refresh token va BCrypt.
- **Trinh quan ly phu thuoc backend:** Maven.
- **Trinh quan ly phu thuoc frontend:** npm/pnpm.

Luong tong the:

```text
React UI -> REST API -> Controller -> Service -> Repository -> JPA/MySQL
```

## 2. Cau truc thu muc

```text
Bamboo_Airways_Booking/
├── frontend/
│   ├── src/
│   │   ├── App.tsx
│   │   ├── index.css
│   │   ├── main.tsx
│   │   └── vite-env.d.ts
│   ├── public/
│   ├── index.html
│   ├── package.json
│   └── vite.config.ts
├── backend/
│   ├── src/main/java/com/bambooairways/
│   │   ├── booking/
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   ├── entity/
│   │   │   ├── initializer/
│   │   │   ├── repository/
│   │   │   └── service/
│   │   ├── enums/
│   │   └── interfaces/
│   ├── src/main/resources/application.yml
│   └── pom.xml
└── README.md
```

## 3. Cau truc backend

Backend su dung layered architecture don gian:

```text
Controller -> Service -> Repository -> MySQL
                   |
                 Entity
```

### Controller

`BookingController` nhan request tu frontend va goi `BookingService`.

Nhom API chinh:

| Nhom | Method | Endpoint | Chuc nang |
|---|---|---|---|
| Auth | POST | `/api/auth/register` | Dang ky |
| Auth | POST | `/api/auth/login` | Dang nhap |
| Auth | POST | `/api/auth/refresh` | Lam moi token |
| Auth | POST | `/api/auth/forgot-password` | Yeu cau dat lai mat khau |
| Auth | POST | `/api/auth/reset-password` | Dat lai mat khau |
| Catalog | GET | `/api/airports` | Lay danh sach san bay |
| Catalog | GET | `/api/flights` | Tim chuyen bay |
| Booking | GET | `/api/flights/{id}/seats` | Lay danh sach ghe |
| Booking | POST | `/api/bookings` | Tao booking |
| Booking | GET | `/api/bookings` | Xem booking cua nguoi dung |
| Booking | GET | `/api/bookings/{code}` | Xem chi tiet booking |
| Payment | POST | `/api/bookings/{code}/payments` | Thanh toan |
| Review | GET | `/api/reviews` | Xem danh gia |
| Review | POST | `/api/reviews` | Gui danh gia |
| Loyalty | GET | `/api/loyalty/transactions` | Xem lich su diem |
| Admin | GET | `/api/admin/bookings` | Xem booking cho quan tri vien |
| Admin | POST | `/api/admin/flights` | Them chuyen bay |

### Service

`BookingService` xu ly hau het nghiep vu cua he thong:

- Dang ky, dang nhap va refresh token.
- Tim san bay, chuyen bay va ghe.
- Tao booking va luu hanh khach.
- Tinh tong tien.
- Thanh toan va cap nhat trang thai.
- Xem lich su booking.
- Gui danh gia.
- Xem lich su diem va du lieu quan tri.

### Repository

Cac repository ke thua `JpaRepository` cua Spring Data JPA:

- `UserRepository`
- `RefreshTokenRepository`
- `PasswordResetTokenRepository`
- `AirportRepository`
- `AircraftRepository`
- `FlightRepository`
- `FareRepository`
- `SeatRepository`
- `BookingRepository`
- `PassengerRepository`
- `PaymentRepository`
- `ReviewRepository`
- `LoyaltyEntryRepository`

### Entity

Entity tuong ung voi cac bang du lieu:

- `UserEntity`
- `AirportEntity`
- `AircraftEntity`
- `FlightEntity`
- `FareEntity`
- `SeatEntity`
- `BookingEntity`
- `PassengerEntity`
- `PaymentEntity`
- `ReviewEntity`
- `LoyaltyEntryEntity`
- `RefreshTokenEntity`
- `PasswordResetTokenEntity`

### Cau hinh

- Backend chay tren port `4000`.
- Frontend chay tren port `8443`.
- Database mac dinh la MySQL `bamboo_booking`.
- JPA dung che do `ddl-auto: update`.
- CORS cho phep localhost va cac origin phu hop khi phat trien.

## 4. Cau truc frontend

Frontend hien tai la SPA React va `App.tsx` chua hau het giao dien, state va goi API.

Khong su dung React Router; dieu huong duoc thuc hien bang trang thai `page`.

Cac buoc trong luong dat ve:

```text
Trang chu
  -> Tim chuyen
  -> Chon hang ve
  -> Nhap hanh khach
  -> Chon ghe
  -> Thanh toan
  -> Trang thanh cong
```

Frontend cung co cac trang/man hinh:

- Trang chu va tim chuyen.
- Dang nhap/dang ky.
- Chon chuyen bay va hang ve.
- Xac nhan hanh trinh.
- Nhap thong tin hanh khach.
- Chon ghe.
- Thanh toan.
- Ma dat cho va trang thanh cong.
- Lich su diem.
- Danh sach va gui danh gia.

Access token va refresh token duoc luu trong `localStorage`.

## 5. Luong thanh toan

1. Nguoi dung hoan tat thong tin hanh khach va chon ghe.
2. Frontend gui `POST /api/bookings` kem:
   - `flightId`
   - `cabinClass`
   - `fareName`
   - Thong tin lien he
   - Thong tin hanh khach
   - `seatCode`
3. Backend kiem tra hinh thuc gia con cho va ghe con trong.
4. Backend tao booking voi trang thai `PENDING`.
5. Backend dat thoi han booking 20 phut.
6. Backend giam mot so ghe con trong cua hinh thuc gia.
7. Ghe duoc chon chuyen sang trang thai `HELD`.
8. Frontend gui `POST /api/bookings/{code}/payments` voi phuong thuc thanh toan.
9. Backend tao ban ghi `PaymentEntity` voi trang thai `PAID`.
10. Backend cap nhat booking thanh `CONFIRMED`.
11. Frontend chuyen sang trang thanh cong va hien ma dat cho.

```text
Chon ghe
  -> POST /api/bookings
  -> Booking PENDING
  -> Giam so ghe va ghe HOLD
  -> POST /api/bookings/{code}/payments
  -> Payment PAID
  -> Booking CONFIRMED
  -> Hien ma dat cho
```

### Tinh tien

Backend tinh tong tien theo cong thuc:

```text
Tong = Gia ve + Thue (30.6%) + Phi ghe
```

`BookingService.createBooking()` thuc hien toan bo cong viec nay.

### Phuong thuc thanh toan tren giao dien

- The tin dung
- VietQR
- VNPay
- MoMo
- ATM
- Thanh toan sau

Backend hien nhan gia tri `method` va tao thanh toan gia lap:

- `status = PAID`
- `providerRef = DEMO-{timestamp}`
- Khong goi cong thanh toan that
- Khong tao URL redirect
- Khong xu ly callback

## 6. Nghiep vu da hoan thanh

### Tai khoan va xac thuc

- Dang ky tai khoan.
- Tao ma thanh vien.
- Ma hoa mat khau bang BCrypt.
- Dang nhap bang username hoac email.
- Tao access token va refresh token JWT.
- Lam moi access token.
- Thu hoi refresh token cu.
- Quen va dat lai mat khau bang token.
- Phan quyen admin cho mot so API.

### Tim kiem va dat ve

- Lay danh sach san bay.
- Tim chuyen bay theo san bay, ngay va mot nguoi lon/mac dinh.
- Hien thi thong tin chuyen bay va hinh thuc gia.
- Chon Economy hoac Business.
- Hien thi ghe va trang thai ghe.
- Luu thong tin hanh khach.
- Tao booking va giu ghe.
- Tao ma dat cho.

### Thanh toan mo phong

- Tinh gia ve, thue va phi ghe.
- Tao payment.
- Cap nhat booking thanh confirmed.
- Hien trang thanh cong va ma dat cho.

### Danh gia va diem

- Xem danh sach danh gia da duyet.
- Gui danh gia sau khi dang nhap.
- Co API lich su giao dich diem thuong.

### Quan tri

- Admin xem toan bo booking.
- Admin them chuyen bay.

### Du lieu mau va cau hinh

- Khoi tao tai khoan admin va khach hang mau.
- Khoi tao san bay, may bay va chuyen bay mau.
- Cau hinh CORS.
- Cau hinh bien moi truong cho MySQL, JWT va port.

## 7. Nghiep vu chua hoan thanh

### Luong bay va booking

- Khong hoan chinh khur hoi va da chang.
- Frontend co lua chon khur hoi nhung backend chi tim mot chieu.
- Chua co hieu so ghe dong thoi/real-time lock cho nhieu nguoi dat cung mot ghe.
- Chua tu dong het han booking sau 20 phut.
- Chua giai phong ghe sau khi huy hoac het han.
- Chua co tu dong xoa booking PENDING qua scheduler.

### Thanh toan

- Chua tich hop VNPay.
- Chua tich hop MoMo.
- Chua tich hop ZaloPay.
- Chua hoan chinh thanh toan the, VietQR va ATM.
- Chua tao URL thanh toan that.
- Chua xu ly callback/webhook va chu ky.
- Chua co rech thanh toan neu giao dich that bai.
- Phuong thuc thanh toan sau chua duoc xu ly dung nghia.

### Mat khau va thong bao

- Quen mat khau chi tao token trong database, chua gui email.
- Chua gui email ve dien tu sau khi dat ve thanh cong.
- Chua co mau/thiet lap dich vu email.

### Huy, doi, hoan ve

- Chua co huy booking.
- Chua co doi ngay bay.
- Chua co doi ghe.
- Chua co hoan tien.
- Chua mo lai ghe va so ghe khi huy/hoan.
- Chua cap nhat point khi huy/hoan.

### Loyalty va thuong

- Co entity lich su diem nhung chua tao diem sau thanh toan.
- Chua thanh toan bang diem.
- Chua thanh toan bang tien ket hop diem.
- Chua cong/tru diem khi dat ve, huy ve hoac hoan ve.

### Quan tri

- Chi xem booking va them chuyen bay.
- Chua sua/xoa chuyen bay.
- Chua quan ly ghe va hinh thuc gia.
- Chua duyet, tu choi hoac xoa danh gia.
- Chua xem va quan ly nguoi dung.
- Chua quan ly trang thai booking.

### Frontend

- Chua dung React Router.
- Toan bo state va giao dien tap trung trong `App.tsx`.
- Reload trang co the mat luong dat ve dang thuc hien.
- Chua co state quan ly lich su booking day du.
- Mot so lien ket/chuc nang trong footer va header moi la giao dien, chua co nghiep vu.
- Chua co xu ly timeout va refresh token tu dong.

### Kiem thu

- Backend chi co test contextLoads; chua co test nghiep vu.
- Chua co test dang ky/dang nhap.
- Chua co test tim chuyen.
- Chua co test tao booking va tranh dat cung ghe.
- Chua co test thanh toan.
- Chua co test phan quyen admin.
- Chua co test tich hop frontend-backend.

### Bao mat va production

- JWT secret mac dinh chi phu hop cho development.
- Chua co du lieu config bang secret manager cho production.
- Chua co refresh token rotation hoan chinh.
- Chua co cookie HttpOnly cho authentication.
- Chua co CSRF strategy neu chuyen sang cookie.
- Chua co rate limit cho dang nhap, dang ky va dat lai mat khau.
- Chua co audit log cho thanh toan va quan tri.

## 8. Danh gia tong quat

Du an da co nen tang hoan chinh cho trinh dien luong dat ve ban demo:

```text
Tim chuyen -> Chon ve -> Nhap hanh khach -> Chon ghe
-> Tao booking -> Thanh toan gia lap -> Xac nhan thanh cong
```

Khong nen mo ta thanh toan hien tai la thanh toan that hoac tuong tac truc tiep voi
cong thanh toan. Ngang nghiep vu hien tai la **booking va thanh toan demo**, co
the dang thanh cong ngay sau khi tao payment, chua co trang thai PENDING/FAILED
thu that tu cong thanh toan ngoai.
