import { FormEvent, ReactNode, useEffect, useMemo, useState } from 'react'

type Page =
  | 'home'
  | 'login'
  | 'register'
  | 'flights'
  | 'fare'
  | 'journey'
  | 'passengers'
  | 'seats'
  | 'payment'
  | 'success'
  | 'history'
  | 'reviews'

type SearchParams = { from: string; to: string; date: string; passengers: number }
type ApiFlight = {
  id: string
  flightNumber: string
  departureAt: string
  arrivalAt: string
  departureAirport: { code: string; name: string; city: string }
  arrivalAirport: { code: string; name: string; city: string }
  aircraft: { model: string }
  fares: Array<{ cabinClass: string; fareName: string; price: number; availableSeats: number }>
  durationMinutes: number
}
type SessionUser = { id: string; username: string; email: string; fullName: string; role: string; memberNumber?: string | null }

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:4000'
const currentYear = new Date().getFullYear()
const toDateInput = (date: Date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}
const todayInput = () => toDateInput(new Date())
const addDays = (input: string, amount: number) => {
  const date = new Date(`${input}T12:00:00`)
  date.setDate(date.getDate() + amount)
  return toDateInput(date)
}
const formatDate = (input: string, options: Intl.DateTimeFormatOptions = { day: '2-digit', month: '2-digit', year: 'numeric' }) =>
  new Intl.DateTimeFormat('vi-VN', options).format(new Date(`${input}T12:00:00`))
const money = (value: number) => new Intl.NumberFormat('vi-VN').format(value)
async function api<T>(path: string, init?: RequestInit): Promise<T> {
  const token = localStorage.getItem('bamboo.accessToken')
  const headers = new Headers(init?.headers)
  if (init?.body && !headers.has('Content-Type')) headers.set('Content-Type', 'application/json')
  if (token) headers.set('Authorization', `Bearer ${token}`)
  const response = await fetch(`${API_URL}${path}`, {
    ...init,
    headers,
  })
  const data = await response.json().catch(() => ({}))
  if (!response.ok) throw new Error(data.message || 'Không thể kết nối máy chủ.')
  return data as T
}

const HERO =
  'https://images.unsplash.com/photo-1730545478278-822e7e3a853d?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&q=80&w=1600'
const COAST =
  'https://images.unsplash.com/photo-1560692479-ead8723d424b?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&q=80&w=1200'
const BAY =
  'https://images.unsplash.com/photo-1778235982248-d4ee62f01064?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&q=80&w=1200'

function Icon({ name, size = 20 }: { name: string; size?: number }) {
  const paths: Record<string, ReactNode> = {
    menu: <path d="M4 7h16M4 12h16M4 17h16" />,
    user: <><circle cx="12" cy="8" r="3.5" /><path d="M5 20c.7-4 3-6 7-6s6.3 2 7 6" /></>,
    plane: <path d="M22 16.5 13 13V5.5c0-1-.5-3.5-1-3.5s-1 2.5-1 3.5V13l-9 3.5V18l9-1.5V21l-2 1v1l3-.5 3 .5v-1l-2-1v-4.5l9 1.5Z" />,
    calendar: <><rect x="3" y="5" width="18" height="16" rx="2" /><path d="M7 3v4m10-4v4M3 10h18" /></>,
    users: <><circle cx="9" cy="8" r="3" /><path d="M3 20c0-4 2-6 6-6s6 2 6 6m1-12a3 3 0 0 1 0 6m2 1c2 .8 3 2.5 3 5" /></>,
    swap: <path d="m7 7-4 4 4 4m-4-4h15m-1-4 4 4-4 4" />,
    arrow: <path d="m9 18 6-6-6-6" />,
    check: <path d="m5 12 4 4L19 6" />,
    clock: <><circle cx="12" cy="12" r="9" /><path d="M12 7v5l3 2" /></>,
    card: <><rect x="3" y="5" width="18" height="14" rx="2" /><path d="M3 10h18" /></>,
    star: <path d="m12 3 2.7 5.5 6.1.9-4.4 4.3 1 6.1-5.4-2.9-5.4 2.9 1-6.1-4.4-4.3 6.1-.9Z" />,
    close: <path d="m6 6 12 12M18 6 6 18" />,
    home: <><path d="m3 11 9-8 9 8" /><path d="M5 10v10h14V10" /><path d="M9 20v-6h6v6" /></>,
  }
  return (
    <svg className="icon" width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden>
      {paths[name] || paths.arrow}
    </svg>
  )
}

function Button({ children, className = '', onClick, type = 'button', disabled = false }: {
  children: ReactNode; className?: string; onClick?: () => void; type?: 'button' | 'submit'; disabled?: boolean
}) {
  return <button type={type} className={`btn ${className}`} onClick={onClick} disabled={disabled}>{children}</button>
}

function Field({ label, value, placeholder, type = 'text', required, onChange, error, icon }: {
  label?: string; value?: string; placeholder?: string; type?: string; required?: boolean
  onChange?: (value: string) => void; error?: string; icon?: string
}) {
  return (
    <label className={`field ${error ? 'field-error' : ''}`}>
      {label && <span>{label}{required && <b> *</b>}</span>}
      <span className="input-wrap">
        {icon && <Icon name={icon} size={17} />}
        <input type={type} value={value} placeholder={placeholder} onChange={(e) => onChange?.(e.target.value)} />
      </span>
      {error && <small>{error}</small>}
    </label>
  )
}

function SelectField({ label, children, required, value, onChange }: { label: string; children: ReactNode; required?: boolean; value?: string; onChange?: (value: string) => void }) {
  return <label className="field"><span>{label}{required && <b> *</b>}</span><select value={value} onChange={(event) => onChange?.(event.target.value)}>{children}</select></label>
}

function DateField({ label, value, min, onChange, disabled = false }: { label: string; value: string; min?: string; onChange: (value: string) => void; disabled?: boolean }) {
  return (
    <label className={`compact-field date-field ${disabled ? 'is-disabled' : ''}`}>
      <span>{label}</span>
      <strong>{formatDate(value)}</strong>
      <Icon name="calendar" size={18} />
      <input aria-label={label} type="date" value={value} min={min} disabled={disabled} onChange={(event) => onChange(event.target.value)} />
    </label>
  )
}

function Logo({ light = false }: { light?: boolean }) {
  return (
    <button className={`logo ${light ? 'light' : ''}`} onClick={() => window.scrollTo({ top: 0, behavior: 'smooth' })}>
      <span>BAMBOO</span><i>AIRWAYS</i><b aria-hidden><em /><em /><em /></b>
    </button>
  )
}

function Header({ go, loggedIn, user, onLogout }: { go: (p: Page) => void; loggedIn: boolean; user: SessionUser | null; onLogout: () => void }) {
  const [mobile, setMobile] = useState(false)
  return (
    <header className="header">
      <div className="header-inner">
        <span onClick={() => go('home')}><Logo /></span>
        <nav className={mobile ? 'open' : ''}>
          <button onClick={() => { go('home'); setMobile(false) }}>Khám phá</button>
          <button onClick={() => { go('home'); setMobile(false) }}>Dịch vụ</button>
          <button onClick={() => { go('home'); setMobile(false) }}>Khuyến mãi</button>
          <button onClick={() => { go('reviews'); setMobile(false) }}>Đánh giá</button>
        </nav>
        <div className="account">
          {loggedIn ? (
            <>
              <button className="user-pill" onClick={() => go('history')}>{user?.fullName || 'Tài khoản Bamboo Club'} <span><Icon name="user" size={18} /></span></button>
              <button className="text-btn muted" onClick={onLogout}>Đăng xuất</button>
            </>
          ) : (
            <>
              <button className="text-btn navy" onClick={() => go('login')}>Đăng nhập</button>
              <button className="text-btn green" onClick={() => go('register')}>Đăng kí</button>
            </>
          )}
        </div>
        <Button className="mobile-menu" onClick={() => setMobile(!mobile)}><Icon name={mobile ? 'close' : 'menu'} /></Button>
      </div>
    </header>
  )
}

const footerCols = [
  ['Thông tin đặt vé', 'Điều kiện giá vé', 'Giá vé đặc biệt', 'Hoàn, hủy, đổi vé', 'Thuế, phí và phụ thu', 'Trẻ em, Go-show'],
  ['Chính sách vé', 'Chính sách hoàn vé', 'Chính sách hủy vé', 'Chính sách đổi vé', 'Chính sách phí', 'Chính sách bảo hiểm'],
  ['Mẹo du lịch', 'Chuẩn bị hành lý', 'Điều chỉnh thời gian', 'Lên kế hoạch chuyến đi', 'Chọn địa điểm', 'Đảm bảo an toàn'],
  ['Những địa điểm du lịch nổi bật', 'Địa điểm tại Hà Nội', 'Địa điểm tại Đà Nẵng', 'Địa điểm tại TP.HCM', 'Địa điểm tại Nha Trang', 'Địa điểm tại Phú Quốc'],
]

function Footer() {
  return (
    <footer>
      <div className="newsletter">
        <div><strong>Đăng ký Email!</strong><span>Nhận ưu đãi mới nhất từ Bamboo Airways</span></div>
        <label><input placeholder="Địa chỉ email của bạn" /><Button>Đăng ký</Button></label>
      </div>
      <div className="footer-main">
        <div className="footer-brand">
          <Logo />
          <p>HƠN CẢ MỘT CHUYẾN BAY</p>
        </div>
        <div className="footer-links">
          {footerCols.map((col) => <div key={col[0]}><h4>{col[0]}</h4>{col.slice(1).map((x) => <button key={x}>{x}</button>)}</div>)}
        </div>
        <div className="footer-bottom">
          <div className="awards"><b>WORLD<br />TRAVEL<br />AWARDS</b><b>AIRLINE<br />EXCELLENCE</b><b>PAX<br />CHOICE</b></div>
          <div className="apps"><span className="qr">▦</span><p><b>Tải ứng dụng Bamboo Airways</b><br />App Store &nbsp; Google Play</p></div>
          <div className="social">Theo dõi chúng tôi &nbsp; ● &nbsp; ◎ &nbsp; ◉</div>
        </div>
      </div>
      <div className="copyright">© {currentYear} Bamboo Airways. Mọi quyền được bảo lưu.</div>
    </footer>
  )
}

function SearchCard({ go, onSearch }: { go: (p: Page) => void; onSearch: (params: SearchParams) => void }) {
  const [tab, setTab] = useState('Đặt Vé')
  const [trip, setTrip] = useState('Khứ hồi')
  const [from, setFrom] = useState('HAN')
  const [to, setTo] = useState('SGN')
  const [date, setDate] = useState(todayInput())
  const [returnDate, setReturnDate] = useState(addDays(todayInput(), 7))
  const [passengers, setPassengers] = useState('1')
  const updateDate = (nextDate: string) => {
    setDate(nextDate)
    if (returnDate < nextDate) setReturnDate(nextDate)
  }
  return (
    <div className="search-card">
      <div className="search-tabs">
        {['Đặt Vé', 'Làm Thủ Tục', 'Đặt Chỗ Của Tôi'].map((x) => <button className={tab === x ? 'active' : ''} onClick={() => setTab(x)} key={x}><Icon name={x === 'Đặt Vé' ? 'plane' : x === 'Làm Thủ Tục' ? 'check' : 'calendar'} size={17} />{x}</button>)}
      </div>
      {tab === 'Đặt Vé' ? (
        <div className="search-content">
          <div className="pay-tabs"><button className="active">Bằng Tiền</button><button>Bằng Điểm</button><button>Bằng Tiền & Điểm</button></div>
          <div className="radio-row">{['Một chiều', 'Khứ hồi', 'Đa chặng'].map((x) => <label key={x}><input type="radio" checked={trip === x} onChange={() => setTrip(x)} /> {x}</label>)}</div>
          <div className="search-fields">
            <label className="airport"><span>TỪ</span><select value={from} onChange={(event) => setFrom(event.target.value)}><option value="HAN">Hà Nội (HAN)</option><option value="SGN">TP. Hồ Chí Minh (SGN)</option><option value="DAD">Đà Nẵng (DAD)</option><option value="CXR">Nha Trang (CXR)</option></select><b>{from}</b></label>
            <Button className="swap" onClick={() => { setFrom(to); setTo(from) }}><Icon name="swap" size={17} /></Button>
            <label className="airport"><span>TỚI</span><select value={to} onChange={(event) => setTo(event.target.value)}><option value="SGN">TP. Hồ Chí Minh (SGN)</option><option value="HAN">Hà Nội (HAN)</option><option value="DAD">Đà Nẵng (DAD)</option><option value="CXR">Nha Trang (CXR)</option></select><b>{to}</b></label>
            <DateField label="NGÀY ĐI" value={date} min={todayInput()} onChange={updateDate} />
            <DateField label="NGÀY VỀ" value={returnDate} min={date} disabled={trip === 'Một chiều'} onChange={setReturnDate} />
            <label className="compact-field"><span>HÀNH KHÁCH</span><select value={passengers} onChange={(event) => setPassengers(event.target.value)}><option value="1">1 người lớn</option><option value="2">2 người lớn</option><option value="3">3 người lớn</option><option value="4">4 người lớn</option></select></label>
            <Button className="search-btn" onClick={() => { onSearch({ from, to, date, passengers: Number(passengers) }); go('flights') }}>Tìm Chuyến Bay</Button>
          </div>
          <div className="search-meta">
            <label className="toggle-label"><input type="checkbox" /> <i /> Dùng tài khoản Bamboo Club của tôi để đặt vé</label>
            <Field placeholder="NHẬP MÃ GIẢM" />
          </div>
        </div>
      ) : (
        <div className="search-alt"><Field label="Mã đặt chỗ" placeholder="Nhập mã đặt chỗ" /><Field label="Họ" placeholder="Nhập họ hành khách" /><Button>Tiếp tục</Button></div>
      )}
    </div>
  )
}

const services = [
  ['card', 'Suất Ăn'], ['users', 'Hành Lý'], ['check', 'Chọn Chỗ Ngồi'], ['card', 'Bảo Hiểm'],
  ['star', 'Phòng Chờ'], ['card', 'Thẻ Quà Tặng'], ['home', 'Khách Sạn'],
]

function Home({ go, onSearch }: { go: (p: Page) => void; onSearch: (params: SearchParams) => void }) {
  return (
    <>
      <section className="hero" style={{ backgroundImage: `linear-gradient(90deg, rgba(0,72,92,.15), rgba(20,150,155,.05)), url(${HERO})` }}>
        <div className="hero-copy"><span>COMBO BAY & NGHỈ DƯỠNG</span><h1>Thêm ưu đãi<br /><b>Bay trọn yêu thương</b></h1><p>Mua combo tiết kiệm đến <strong>40%</strong></p><Button className="white-btn" onClick={() => document.getElementById('promotions')?.scrollIntoView({ behavior: 'smooth', block: 'start' })}>Chi tiết <Icon name="arrow" size={15} /></Button></div>
        <SearchCard go={go} onSearch={onSearch} />
      </section>
      <main>
        <div className="service-strip">{services.map(([icon, label]) => <button key={label}><Icon name={icon} /><span>{label}</span></button>)}</div>
        <section className="useful">
          <div><span>CẨM NANG BAY</span><h2>Thông tin hữu ích cho chuyến bay của bạn</h2><p>Tiêu chuẩn hành lý, điều kiện vé bay và yêu cầu đặc biệt.</p></div>
          <Button className="outline-light">Tra cứu <Icon name="arrow" size={16} /></Button>
        </section>
        <div className="news-bar"><b>Tin tức</b><span>•</span><p>Bamboo Airways mở thêm nhiều đường bay nội địa hấp dẫn</p><button>Xem thêm <Icon name="arrow" size={14} /></button></div>
        <SectionTitle eyebrow="ƯU ĐÃI" title="Bay nhiều hơn, trải nghiệm nhiều hơn" />
        <section id="promotions" className="promo-grid">
          <article className="promo-main"><small>ƯU ĐÃI ĐẶC BIỆT</small><h3>Bay đẳng cấp<br />Nhận đặc quyền</h3><p>Giảm đến 15% cho hội viên Bamboo Club</p><Button className="white-btn">Khám phá ngay</Button></article>
          <article className="promo-food"><small>TIẾT KIỆM 30%</small><h3>Ưu đãi suất ăn trên mây</h3><p>Đặt trước để tận hưởng hương vị Việt</p></article>
          <article className="promo-night"><small>HÀNH TRÌNH MỚI</small><h3>Bay đêm, thêm tiết kiệm</h3><p>Khởi hành nhẹ nhàng, giá vé dễ chịu</p></article>
        </section>
        <SectionTitle eyebrow="KHÁM PHÁ" title="BAMBOO EXPERIENCE" />
        <ImageCards items={[
          ['Đội Bay', 'Đội tàu bay hiện đại, an toàn', COAST],
          ['Mạng bay', 'Kết nối những điểm đến tuyệt vời', BAY],
          ['Hành trình tận tâm', 'Dịch vụ từ trái tim', HERO],
        ]} />
        <SectionTitle eyebrow="DỊCH VỤ BỔ TRỢ" title="Dịch vụ bổ trợ" />
        <div className="ancillary">
          {['Hành lý trả trước', 'Chọn chỗ ngồi', 'Suất ăn trên mây', 'Phòng chờ thương gia'].map((x, i) => <article key={x}><span>0{i + 1}</span><Icon name={i === 1 ? 'users' : i === 2 ? 'card' : 'star'} size={28} /><h3>{x}</h3><p>Chuẩn bị trước để chuyến bay thêm nhẹ nhàng.</p><button>Tìm hiểu thêm <Icon name="arrow" size={14} /></button></article>)}
        </div>
        <SectionTitle eyebrow="GỢI Ý CHO BẠN" title="Điểm đến phổ biến" />
        <div className="destination-grid">
          {[
            ['Hà Nội', 'Đà Nẵng', formatDate(addDays(todayInput(), 5)), '799.000', COAST],
            ['TP. Hồ Chí Minh', 'Phú Quốc', formatDate(addDays(todayInput(), 8)), '699.000', BAY],
            ['Hà Nội', 'Nha Trang', formatDate(addDays(todayInput(), 12)), '899.000', HERO],
          ].map(([from, to, date, price, image]) => <article key={to} style={{ backgroundImage: `linear-gradient(0deg, rgba(0,28,56,.88), transparent 70%), url(${image})` }}><div><small>{from} →</small><h3>{to}</h3><p>{date}</p><span>chỉ từ <b>{price} VND</b></span></div><Button className="round"><Icon name="arrow" /></Button></article>)}
        </div>
        <div className="center"><Button className="outline">Xem Thêm</Button></div>
      </main>
      <Footer />
    </>
  )
}

function SectionTitle({ eyebrow, title }: { eyebrow: string; title: string }) {
  return <div className="section-title"><span>{eyebrow}</span><h2>{title}</h2><i /></div>
}

function ImageCards({ items }: { items: string[][] }) {
  return <div className="image-cards">{items.map(([title, text, img]) => <article key={title} style={{ backgroundImage: `linear-gradient(0deg, rgba(0,32,62,.82), transparent 68%), url(${img})` }}><div><h3>{title}</h3><p>{text}</p><button>Khám phá <Icon name="arrow" size={14} /></button></div></article>)}</div>
}

function AuthPage({ type, go, onLogin }: { type: 'login' | 'register'; go: (p: Page) => void; onLogin: (user: SessionUser) => void }) {
  const [submitted, setSubmitted] = useState(false)
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const submit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    setSubmitted(true)
    setError('')
    const form = new FormData(e.currentTarget)
    const username = String(form.get('username') || '')
    const password = String(form.get('password') || '')
    if (!username || !password) return
    if (type === 'register' && password !== String(form.get('passwordConfirmation') || '')) {
      setError('Mật khẩu nhập lại không khớp.')
      return
    }
    setBusy(true)
    try {
      const payload = type === 'login'
        ? await api<{ user: SessionUser; accessToken: string; refreshToken: string }>('/api/auth/login', { method: 'POST', body: JSON.stringify({ username, password }) })
        : await api<{ user: SessionUser; accessToken: string; refreshToken: string }>('/api/auth/register', {
          method: 'POST',
          body: JSON.stringify({
            username,
            password,
            email: String(form.get('email') || ''),
            fullName: String(form.get('fullName') || ''),
            identityNumber: String(form.get('identityNumber') || ''),
            phone: String(form.get('phone') || ''),
            gender: String(form.get('gender') || ''),
            dateOfBirth: String(form.get('dateOfBirth') || '') || undefined,
          }),
        })
      localStorage.setItem('bamboo.accessToken', payload.accessToken)
      localStorage.setItem('bamboo.refreshToken', payload.refreshToken)
      onLogin(payload.user)
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : 'Không thể đăng nhập.')
    } finally {
      setBusy(false)
    }
  }
  return (
    <div className="auth-page" style={{ backgroundImage: `linear-gradient(rgba(222,247,255,.42),rgba(222,247,255,.18)), url(${type === 'register' ? '/report-assets/register-side.jpg' : HERO})` }}>
      <button className="auth-back" onClick={() => go('home')}>← Về trang chủ</button>
      <form className={`auth-card ${type === 'register' ? 'register-card' : ''}`} onSubmit={submit}>
        <Logo />
        <h1>{type === 'login' ? 'Đăng Nhập Bamboo Club' : 'Đăng Ký Bamboo Club'}</h1>
        <p>{type === 'login' ? 'Chào mừng Quý khách trở lại' : 'Trở thành hội viên để nhận thêm nhiều đặc quyền'}</p>
        <div className={type === 'register' ? 'form-grid' : ''}>
          <label className="field"><span>Tên đăng nhập *</span><span className="input-wrap"><input name="username" placeholder="Nhập tên đăng nhập" required /></span>{submitted && !error && type === 'register' && <small>Vui lòng nhập tên đăng nhập</small>}</label>
          {type === 'register' && <label className="field"><span>CCCD/CMND *</span><span className="input-wrap"><input name="identityNumber" placeholder="Nhập số giấy tờ" required /></span></label>}
          <label className="field"><span>Mật khẩu *</span><span className="input-wrap"><input name="password" placeholder="Nhập mật khẩu" type="password" minLength={8} required /></span></label>
          {type === 'register' && <label className="field"><span>Email *</span><span className="input-wrap"><input name="email" placeholder="vidu@email.com" type="email" required /></span></label>}
          {type === 'register' && <><label className="field"><span>Nhập lại mật khẩu *</span><span className="input-wrap"><input name="passwordConfirmation" placeholder="Nhập lại mật khẩu" type="password" required /></span></label><label className="field"><span>Số điện thoại *</span><span className="input-wrap"><input name="phone" placeholder="090 123 4567" required /></span></label><label className="field"><span>Họ và tên *</span><span className="input-wrap"><input name="fullName" placeholder="NGUYỄN MINH ANH" required /></span></label><label className="field"><span>Ngày sinh *</span><span className="input-wrap"><input name="dateOfBirth" type="date" required /></span></label><label className="field"><span>Giới tính *</span><span className="input-wrap"><select name="gender" required><option value="">Chọn giới tính</option><option>Nam</option><option>Nữ</option><option>Khác</option></select></span></label></>}
        </div>
        {error && <p className="error-text">{error}</p>}
        {type === 'login' && <button className="forgot">Quên Mật Khẩu?</button>}
        <Button type="submit" className="full" disabled={busy}>{busy ? 'Đang xử lý...' : type === 'login' ? 'Đăng nhập' : 'Đăng ký'}</Button>
        <p className="auth-switch">{type === 'login' ? 'Bạn chưa là hội viên?' : 'Đã có tài khoản?'} <button onClick={() => go(type === 'login' ? 'register' : 'login')}>{type === 'login' ? 'Đăng ký Bamboo Club' : 'Đăng nhập ngay'}</button></p>
      </form>
    </div>
  )
}

function BookingBar({ go, total = '500.000 VND', date }: { go: (p: Page) => void; total?: string; date?: string }) {
  return (
    <div className="booking-bar">
      <button onClick={() => go('home')} className="route"><b>HAN</b><span>Hà Nội</span><i>→</i><b>SGN</b><span>TP. Hồ Chí Minh</span></button>
      <div><span>Khởi hành</span><b>{formatDate(date || todayInput(), { weekday: 'short', day: '2-digit', month: 'short' })}</b></div>
      <div><span>Hành khách</span><b>1 người lớn</b></div>
      <Button onClick={() => go('journey')}><span>Đặt chỗ của bạn</span><b>{total}</b></Button>
    </div>
  )
}

function Banner({ title, subtitle }: { title: string; subtitle?: string }) {
  return <div className="page-banner" style={{ backgroundImage: `linear-gradient(90deg,rgba(0,45,75,.55),rgba(19,144,151,.18)),url(${BAY})` }}><div><h1>{title}</h1>{subtitle && <p>{subtitle}</p>}</div></div>
}

function Flights({ go, search, onSearch }: { go: (p: Page) => void; search: SearchParams; onSearch: (params: SearchParams) => void }) {
  const [flights, setFlights] = useState<ApiFlight[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  useEffect(() => {
    setLoading(true)
    setError('')
    api<ApiFlight[]>(`/api/flights?from=${search.from}&to=${search.to}&date=${search.date}&passengers=${search.passengers}`)
      .then(setFlights)
      .catch((requestError) => setError(requestError instanceof Error ? requestError.message : 'Không thể tải chuyến bay.'))
      .finally(() => setLoading(false))
  }, [search])
  return (
    <><BookingBar go={go} total="Chưa chọn" date={search.date} /><Banner title="Vui lòng chọn chuyến bay" subtitle={`${search.from} đến ${search.to} · ${formatDate(search.date)}`} />
      <main className="booking-main">
        <div className="booking-navigation">
          <Button className="outline" onClick={() => go('home')}><span aria-hidden>←</span> Quay lại tìm kiếm</Button>
        </div>
        <div className="notice"><b>1 lưu ý</b><span>Giá vé hiển thị là giá cho một hành khách, chưa bao gồm thuế và phí.</span></div>
        <div className="date-strip">
          <button aria-label="Các ngày trước" onClick={() => onSearch({ ...search, date: addDays(search.date, -1) })} disabled={search.date <= todayInput()}>‹</button>
          {Array.from({ length: 7 }, (_, index) => addDays(search.date, index - 3)).map((date) => {
            const selected = date === search.date
            const past = date < todayInput()
            return <button className={`${selected ? 'selected' : ''} ${past ? 'disabled' : ''}`} key={date} disabled={past} onClick={() => onSearch({ ...search, date })}>
              <span>{formatDate(date, { weekday: 'long' })}</span><b>{formatDate(date, { day: '2-digit', month: '2-digit' })}</b><small>{selected ? 'Đang chọn' : 'Tìm chuyến'}</small>
            </button>
          })}
          <button aria-label="Các ngày tiếp theo" onClick={() => onSearch({ ...search, date: addDays(search.date, 1) })}>›</button>
        </div>
        <div className="flight-list">
          {loading && <div className="notice"><span>Đang tìm chuyến bay...</span></div>}
          {error && <div className="notice"><b>Lỗi</b><span>{error}</span></div>}
          {!loading && !error && flights.length === 0 && <div className="notice"><b>Không có chuyến</b><span>Không tìm thấy chuyến bay phù hợp. Vui lòng đổi ngày hoặc hành trình.</span></div>}
          {flights.map((flight) => <FlightCard key={flight.id} go={go} time={new Date(flight.departureAt).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })} arrival={new Date(flight.arrivalAt).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })} duration={`${Math.floor(flight.durationMinutes / 60)} giờ ${flight.durationMinutes % 60} phút`} aircraft={flight.aircraft.model} fares={flight.fares} />)}
        </div>
      </main>
    </>
  )
}

function FlightCard({ go, time, arrival, duration, aircraft, fares }: { go: (p: Page) => void; time: string; arrival: string; duration: string; aircraft: string; fares: ApiFlight['fares'] }) {
  const economy = fares.find((fare) => fare.cabinClass === 'ECONOMY')
  const business = fares.find((fare) => fare.cabinClass === 'BUSINESS')
  return (
    <article className="flight-card">
      <div className="flight-info">
        <div className="time"><strong>{time}</strong><span>HAN</span><small>Nhà ga 1</small></div>
        <div className="flight-line"><span>Bay thẳng</span><i><Icon name="plane" size={15} /></i><small>{duration}</small></div>
        <div className="time"><strong>{arrival}</strong><span>SGN</span><small>Nhà ga 3</small></div>
        <div className="flight-detail"><p><Icon name="clock" size={15} /> Thời gian bay: {duration}</p><p><Icon name="plane" size={15} /> {aircraft} được Bamboo Airways khai thác</p><button>Xem chi tiết hành trình</button></div>
      </div>
      {economy && <button className="fare-tile economy" onClick={() => go('fare')}><small>Còn lại {economy.availableSeats} chỗ</small><span>{economy.fareName}</span><i>từ</i><strong>{money(economy.price)}</strong><b>VND</b><em>⌄</em></button>}
      {business && <button className="fare-tile business" onClick={() => go('fare')}><small>Còn lại {business.availableSeats} chỗ</small><span>{business.fareName}</span><i>từ</i><strong>{money(business.price)}</strong><b>VND</b><em>⌄</em></button>}
    </article>
  )
}

const benefits = ['Hành lý xách tay: 7kg', '01 kiện hành lý ký gửi 20kg', 'Hoàn/huỷ vé trước giờ khởi hành', 'Thay đổi miễn phí', 'Hệ số cộng điểm Bamboo Club: 1.00', 'Chọn ghế ngồi miễn phí', 'Đổi chuyến bay sớm miễn phí']

function Fare({ go }: { go: (p: Page) => void }) {
  return <><BookingBar go={go} /><main className="booking-main narrow"><div className="mini-flight"><b>07:30 HAN</b><span>Bay thẳng — 2 giờ 10 phút</span><b>09:40 SGN</b></div><h2 className="booking-title">Chọn hạng vé</h2><p className="center-text">Tiện ích với mỗi hành khách.</p><article className="fare-detail"><header><strong>500.000 <small>VND</small></strong><span>Economy Flex</span><em>Còn 60 chỗ ở mức giá này</em></header><ul>{benefits.map((x) => <li key={x}><Icon name="check" size={17} />{x}</li>)}</ul><button className="detail-link">Xem chi tiết</button></article><p className="selected-note"><Icon name="check" size={17} /> Bạn đã chọn Economy Flex.</p><Button onClick={() => go('journey')}>Xác nhận và tiếp tục</Button></main></>
}

function Journey({ go }: { go: (p: Page) => void }) {
  return <><BookingBar go={go} /><Banner title="Hành trình của Quý khách" subtitle="Kiểm tra thông tin trước khi tiếp tục" /><main className="booking-main narrow"><article className="journey-card"><div className="journey-head"><span>CHỦ NHẬT, 05 THÁNG 10</span><b>Hà Nội → TP. Hồ Chí Minh</b></div><div className="journey-flight"><div><b>07:30</b><span>HAN</span></div><i><Icon name="plane" /><small>Bay thẳng · 2 giờ 10 phút</small></i><div><b>09:40</b><span>SGN</span></div></div><div className="journey-fare"><span>Economy Flex · 1 người lớn</span><b>500.000 VND</b></div></article><article className="total-card"><span>Tổng giá</span><strong>653.000 VND</strong><small>Đã bao gồm thuế và phí</small></article><div className="policy-links"><button>Chính sách hành lý chi tiết</button><button>Điều kiện giá vé</button><button>Quy định hành lý nguy hiểm</button></div><Button className="full" onClick={() => go('passengers')}>Điền thông tin hành khách</Button></main></>
}

function Passengers({ go, loggedIn }: { go: (p: Page) => void; loggedIn: boolean }) {
  const [consent, setConsent] = useState(false)
  const [submitted, setSubmitted] = useState(false)
  const next = () => { setSubmitted(true); if (consent) go('seats') }
  return <><BookingBar go={go} total="653.000 VND" /><Banner title="Nhập thông tin khách hàng" subtitle="Vui lòng nhập đúng thông tin trên giấy tờ tùy thân" /><main className="booking-main form-page"><FormCard title="Hành khách 1 · Người lớn" subtitle="Thông tin cá nhân"><div className="form-grid four"><SelectField label="Danh xưng" required><option>Ông</option><option>Bà</option><option>Khác</option></SelectField><Field label="Tên đệm và tên" value={loggedIn ? 'MINH ANH' : undefined} placeholder="MINH ANH" required /><Field label="Họ" value={loggedIn ? 'NGUYỄN' : undefined} placeholder="NGUYỄN" required /><Field label="Ngày sinh" placeholder="dd/mm/yyyy" required /></div></FormCard><FormCard title="Chương trình Khách hàng thân thiết"><div className="form-grid two"><SelectField label="Chương trình"><option>Bamboo Club</option><option>Chương trình khác</option></SelectField><Field label="Số thẻ hội viên" value={loggedIn ? 'BAM1234567' : undefined} placeholder="Nhập số thẻ" /></div></FormCard><FormCard title="Thông tin liên lạc" subtitle="Vé điện tử sẽ được gửi đến thông tin dưới đây"><div className="form-grid two"><Field label="Email" type="email" value={loggedIn ? 'minhanh@example.com' : undefined} placeholder="vidu@email.com" required /><button className="add-link">+ Thêm địa chỉ email</button><SelectField label="Loại điện thoại"><option>Di động</option><option>Nhà riêng</option></SelectField><SelectField label="Mã quốc gia"><option>Việt Nam (+84)</option></SelectField><Field label="Số điện thoại" value={loggedIn ? '0901234567' : undefined} placeholder="090 123 4567" required /></div><label className="toggle-label"><input type="checkbox" /><i /> Thêm thông tin liên lạc khẩn cấp</label></FormCard><label className={`consent ${submitted && !consent ? 'invalid' : ''}`}><input type="checkbox" checked={consent} onChange={(e) => setConsent(e.target.checked)} /><span>Tôi đã đọc và đồng ý với <u>Chính sách bảo vệ dữ liệu cá nhân</u> của Bamboo Airways.</span></label>{submitted && !consent && <p className="error-text">Quý khách vui lòng đồng ý với chính sách để tiếp tục.</p>}<Button onClick={next}>Xác nhận</Button></main></>
}

function FormCard({ title, subtitle, children }: { title: string; subtitle?: string; children: ReactNode }) {
  return <section className="form-card"><header><h2>{title}</h2>{subtitle && <p>{subtitle}</p>}</header><div className="form-card-body">{children}</div></section>
}

const seatRows = Array.from({ length: 16 }, (_, i) => i + 1)
const seatCols = ['A', 'B', 'C', 'D', 'G', 'H', 'K']

function Seats({ go }: { go: (p: Page) => void }) {
  const [legend, setLegend] = useState(true)
  const [selected, setSelected] = useState('8G')
  const [seconds, setSeconds] = useState(1200)
  useEffect(() => { const t = setInterval(() => setSeconds((s) => Math.max(s - 1, 0)), 1000); return () => clearInterval(t) }, [])
  const timer = `${String(Math.floor(seconds / 60)).padStart(2, '0')}:${String(seconds % 60).padStart(2, '0')}`
  const blocked = ['1A', '1B', '2K', '4D', '6A', '8H', '10C', '13G', '15K']
  const preferred = ['2A', '2B', '2C', '2D', '2G', '2H']
  return <><BookingBar go={go} total="653.000 VND" /><Banner title="Chọn chỗ ngồi" subtitle="Hà Nội → TP. Hồ Chí Minh" /><main className="booking-main seat-page"><div className="seat-toolbar"><div><h2>Chọn chỗ ngồi</h2><p>Economy · Hành khách 1: NGUYỄN MINH ANH</p></div><div className="hold"><Icon name="clock" /><span>Giữ chỗ trong</span><b>{timer}</b></div><Button className="outline" onClick={() => setLegend(true)}>Chú giải</Button></div><div className="plane"><div className="plane-nose"><Icon name="plane" size={30} /><span>Đầu máy bay</span></div><div className="column-labels">{seatCols.map((c, i) => <span key={c} className={i === 4 ? 'aisle-after' : ''}>{c}</span>)}</div>{seatRows.map((row) => <div className={`seat-row ${row === 7 ? 'exit-row' : ''}`} key={row}>{seatCols.map((col, i) => { const code = `${row}${col}`; const state = blocked.includes(code) ? 'blocked' : selected === code ? 'chosen' : preferred.includes(code) ? 'preferred' : row === 7 ? 'exit-seat' : ''; return <button key={code} className={`seat ${state} ${i === 4 ? 'aisle-after' : ''}`} disabled={state === 'blocked'} onClick={() => setSelected(code)}>{code}</button>})}{row === 7 && <span className="exit-label">LỐI THOÁT HIỂM</span>}</div>)}<div className="plane-tail"><span>WC</span><b>Đuôi máy bay</b><span>WC</span></div></div><div className="seat-summary"><span>Ghế đã chọn: <b>{selected}</b></span><strong>Phí chọn ghế: 0 VND</strong><Button onClick={() => go('payment')}>Tiếp tục thanh toán</Button></div></main>{legend && <Legend onClose={() => setLegend(false)} />}</>
}

function Legend({ onClose }: { onClose: () => void }) {
  const items = [['standard', 'Ghế tiêu chuẩn'], ['quiet', 'Ghế ngồi yên tĩnh'], ['blocked', 'Không còn trống'], ['preferred', 'Ghế ngồi ưa thích'], ['exit', 'Hàng ghế lối thoát hiểm'], ['chosen', 'Ghế đang chọn'], ['door', 'Lối thoát hiểm'], ['wc', 'Nhà vệ sinh']]
  return <div className="modal-backdrop"><div className="legend-dialog"><header><h2>Chú giải Sơ đồ chỗ ngồi</h2><button onClick={onClose}><Icon name="close" /></button></header><div className="legend-grid">{items.map(([state, label]) => <div key={state}><i className={state}>{state === 'wc' ? 'WC' : ''}</i><span>{label}</span></div>)}</div><footer><Button className="outline" onClick={onClose}>Hủy</Button><Button onClick={onClose}>OK</Button></footer></div></div>
}

const paymentMethods = [
  ['card', 'Thẻ ghi nợ / Thẻ tín dụng', 'VISA · Mastercard · JCB'],
  ['card', 'Thanh toán bằng mã VietQR', 'Quét mã bằng ứng dụng ngân hàng'],
  ['card', 'Thanh toán bằng mã VNPAY-QR', 'Thanh toán nhanh chóng và bảo mật'],
  ['card', 'Ví điện tử Momo', 'Thanh toán qua ví MoMo'],
  ['card', 'Thẻ ATM / Thẻ nội địa', 'Áp dụng với ngân hàng tại Việt Nam'],
  ['clock', 'Trả sau', 'Đặt chỗ ngay, thanh toán sau dễ dàng'],
]

function Payment({ go }: { go: (p: Page) => void }) {
  const [open, setOpen] = useState(2)
  return <><BookingBar go={go} total="653.000 VND" /><Banner title="Thanh toán" subtitle="Hoàn tất đặt chỗ của Quý khách" /><main className="booking-main payment-page"><details className="price-details" open><summary><span>Tổng giá</span><b>653.000 VND</b></summary><div><span>Giá vé</span><b>500.000 VND</b><span>Thuế và phí</span><b>153.000 VND</b></div></details><button className="promo-link">Bạn có muốn thêm mã khuyến mãi không?</button><section className="payment-card"><h2>Chọn phương thức thanh toán của Quý khách</h2><button className="voucher-link">Bạn có muốn sử dụng voucher không?</button>{paymentMethods.map(([icon, title, note], i) => <div className={`pay-method ${open === i ? 'open' : ''}`} key={title}><button onClick={() => setOpen(open === i ? -1 : i)}><Icon name={icon} /><span><b>{title}</b><small>{note}</small></span><em>⌄</em></button>{open === i && <div className="pay-content">{i === 2 ? <><div className="vnpay-mark">VNPAY</div><p>Quý khách sẽ được chuyển đến cổng thanh toán VNPAY để quét mã QR.</p><label className="consent"><input type="checkbox" defaultChecked /><span>Tôi đồng ý với điều khoản thanh toán.</span></label><Button onClick={() => go('success')}>Thanh toán 653.000 VND</Button></> : <><p>Phương thức thanh toán này đang sẵn sàng cho đặt chỗ của Quý khách.</p><Button onClick={() => go('success')}>Tiếp tục thanh toán</Button></>}</div>}</div>)}</section><Button className="outline" onClick={() => go('seats')}>Quay lại</Button></main></>
}

function Success({ go }: { go: (p: Page) => void }) {
  return <main className="success-page"><div className="success-icon"><Icon name="check" size={44} /></div><span>THANH TOÁN THÀNH CÔNG</span><h1>Chuyến bay của Quý khách<br />đã được xác nhận</h1><p>Mã đặt chỗ</p><strong>Q7BAM9</strong><div className="ticket"><div><small>HÀNH TRÌNH</small><b>HAN <Icon name="plane" /> SGN</b><span>07:30 · Chủ nhật, 05/10/2025</span></div><div><small>HÀNH KHÁCH</small><b>NGUYỄN MINH ANH</b><span>Ghế 8G · Economy Flex</span></div></div><p>Vé điện tử đã được gửi tới email của Quý khách. Vui lòng kiểm tra cả thư mục rác.</p><Button onClick={() => go('home')}>Về trang chủ</Button></main>
}

function History() {
  const [entries, setEntries] = useState<Array<{ id: string; transactionNo: string; createdAt: string; description: string; type: string; points: number }>>([])
  const [error, setError] = useState('')
  useEffect(() => { api<typeof entries>('/api/loyalty/transactions').then(setEntries).catch((requestError) => setError(requestError instanceof Error ? requestError.message : 'Không thể tải lịch sử.')) }, [])
  return <main className="history-page"><div className="history-head"><span>TÀI KHOẢN BAMBOO CLUB</span><h1>Hoạt động gần đây</h1><p>Theo dõi điểm thưởng và lịch sử chuyến bay của Quý khách.</p></div><section className="history-card"><div className="history-tabs"><button className="active">Lịch Sử Điểm Thưởng</button><button>Lịch Sử Điểm Xét Hạng</button><button>Lịch Sử Chặng Bay Xét Hạng</button></div><div className="filters"><Field label="Từ" type="date" /><Field label="Đến" type="date" /><p>Chỉ hiển thị dữ liệu trong 13 tháng.</p><button className="reset">Đặt lại bộ lọc</button><Button>Bộ lọc</Button></div><div className="result-bar"><span>Hiển thị {entries.length} mục.</span><button>Xuất dữ liệu</button></div>{error && <p className="error-text">{error}</p>}<div className="table-wrap"><table><thead><tr>{['#', 'Số giao dịch', 'Ngày', 'Mô tả', 'Loại', 'Điểm thưởng'].map((x) => <th key={x}>{x}</th>)}</tr></thead><tbody>{entries.length ? entries.map((entry, index) => <tr key={entry.id}><td>{index + 1}</td><td>{entry.transactionNo}</td><td>{new Date(entry.createdAt).toLocaleDateString('vi-VN')}</td><td>{entry.description}</td><td>{entry.type}</td><td>{entry.points > 0 ? '+' : ''}{entry.points}</td></tr>) : <tr><td colSpan={6}>Chưa có dữ liệu điểm thưởng.</td></tr>}</tbody></table></div></section></main>
}

const initialReviews = [
  ['Trần Minh Huy', 5, 'Dịch vụ rất chu đáo, tiếp viên thân thiện và chuyến bay đúng giờ.'],
  ['Lê Thu Hà', 4, 'Trải nghiệm đặt vé dễ dàng, khoang máy bay sạch sẽ.'],
  ['Nguyễn Hoàng Nam', 5, 'Tôi rất hài lòng với dịch vụ của Bamboo Airways.'],
]

function Reviews({ loggedIn, go }: { loggedIn: boolean; go: (p: Page) => void }) {
  const [rating, setRating] = useState(5)
  const [comment, setComment] = useState('')
  const [reviews, setReviews] = useState(initialReviews)
  const [error, setError] = useState('')
  useEffect(() => { api<Array<{ user: { fullName: string }; rating: number; comment: string }>>('/api/reviews').then((items) => setReviews(items.map((item) => [item.user.fullName, item.rating, item.comment]))).catch(() => undefined) }, [])
  const add = async () => {
    if (comment.trim().length < 10) { setError('Đánh giá cần có ít nhất 10 ký tự.'); return }
    try {
      await api('/api/reviews', { method: 'POST', body: JSON.stringify({ rating, comment }) })
      setReviews([['Bạn', rating, comment], ...reviews])
      setComment('')
      setError('')
    } catch (requestError) { setError(requestError instanceof Error ? requestError.message : 'Không thể gửi đánh giá.') }
  }
  return <><Banner title="Danh Sách Đánh Giá" subtitle="Lắng nghe chia sẻ từ hành khách Bamboo Airways" /><main className="review-page">{loggedIn ? <section className="comment-box"><div className="comment-head"><span className="avatar">MA</span><div><b>Chia sẻ trải nghiệm của bạn</b><div className="star-picker">{[1, 2, 3, 4, 5].map((n) => <button className={n <= rating ? 'on' : ''} onClick={() => setRating(n)} key={n}><Icon name="star" size={22} /></button>)}</div></div></div><textarea value={comment} onChange={(e) => setComment(e.target.value)} placeholder="Viết đánh giá của bạn..." /><Button onClick={add}>Gửi đánh giá</Button>{error && <p className="error-text">{error}</p>}</section> : <p className="login-warning">Bạn cần <button onClick={() => go('login')}>đăng nhập</button> để thêm bình luận.</p>}<div className="review-list">{reviews.map(([name, stars, text], i) => <article className="review-card" key={`${name}-${i}`}><span className="avatar">{String(name).split(' ').slice(-1)[0].slice(0, 1)}</span><div><header><b>{name}</b><span>{Array.from({ length: 5 }, (_, j) => <Icon key={j} name="star" size={16} />)}</span></header><p>{text}</p><footer><span>10 tháng trước</span><button>Trả lời</button><button>Xem câu trả lời</button></footer></div></article>)}</div></main><Footer /></>
}

export default function App() {
  const [page, setPage] = useState<Page>('home')
  const [loggedIn, setLoggedIn] = useState(false)
  const [user, setUser] = useState<SessionUser | null>(null)
  const [search, setSearch] = useState<SearchParams>({ from: 'HAN', to: 'SGN', date: todayInput(), passengers: 1 })
  useEffect(() => {
    setLoggedIn(Boolean(localStorage.getItem('bamboo.accessToken')))
  }, [])
  const go = (next: Page) => { setPage(next); window.scrollTo({ top: 0, behavior: 'smooth' }) }
  const login = (nextUser: SessionUser) => { setUser(nextUser); setLoggedIn(true); go('home') }
  const logout = () => { localStorage.removeItem('bamboo.accessToken'); localStorage.removeItem('bamboo.refreshToken'); setUser(null); setLoggedIn(false); go('home') }
  const bookingPages = useMemo(() => ['flights', 'fare', 'journey', 'passengers', 'seats', 'payment'].includes(page), [page])
  return (
    <div className="app">
      {!bookingPages && !['login', 'register', 'success'].includes(page) && <Header go={go} loggedIn={loggedIn} user={user} onLogout={logout} />}
      {page === 'home' && <Home go={go} onSearch={setSearch} />}
      {page === 'login' && <AuthPage type="login" go={go} onLogin={login} />}
      {page === 'register' && <AuthPage type="register" go={go} onLogin={login} />}
      {page === 'flights' && <Flights go={go} search={search} onSearch={setSearch} />}
      {page === 'fare' && <Fare go={go} />}
      {page === 'journey' && <Journey go={go} />}
      {page === 'passengers' && <Passengers go={go} loggedIn={loggedIn} />}
      {page === 'seats' && <Seats go={go} />}
      {page === 'payment' && <Payment go={go} />}
      {page === 'success' && <Success go={go} />}
      {page === 'history' && (loggedIn ? <History /> : <AuthPage type="login" go={go} onLogin={login} />)}
      {page === 'reviews' && <Reviews loggedIn={loggedIn} go={go} />}
    </div>
  )
}
