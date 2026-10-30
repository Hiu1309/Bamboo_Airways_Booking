import 'dotenv/config'
import bcrypt from 'bcryptjs'
import cors from 'cors'
import crypto from 'node:crypto'
import express, { NextFunction, Request, Response } from 'express'
import jwt from 'jsonwebtoken'
import { z } from 'zod'
import { PrismaClient, Prisma } from '@prisma/client'

const prisma = new PrismaClient()
const app = express()
const port = Number(process.env.PORT ?? 4000)
const accessSecret = process.env.JWT_ACCESS_SECRET ?? 'development-access-secret'
const refreshSecret = process.env.JWT_REFRESH_SECRET ?? 'development-refresh-secret'
const allowedOrigins = (process.env.CLIENT_ORIGIN ?? 'http://localhost:5173,http://localhost:8443')
  .split(',')
  .map((origin) => origin.trim())
  .filter(Boolean)
const isDevelopmentOrigin = (origin: string) => /^http:\/\/(localhost|127\.0\.0\.1|192\.168\.\d+\.\d+):\d+$/.test(origin)

app.use(cors({
  origin: (origin, callback) => {
    if (!origin || allowedOrigins.includes(origin) || isDevelopmentOrigin(origin)) return callback(null, true)
    return callback(new Error(`CORS origin không được phép: ${origin}`))
  },
  credentials: true,
}))
app.use(express.json())

type AuthRequest = Request & { user?: { id: string; role: 'CUSTOMER' | 'ADMIN' } }
const asyncRoute = (handler: (req: AuthRequest, res: Response) => Promise<unknown>) =>
  async (req: AuthRequest, res: Response, next: NextFunction) => {
    try { await handler(req, res) } catch (error) { next(error) }
  }
const requireAuth = (req: AuthRequest, res: Response, next: NextFunction) => {
  const token = req.headers.authorization?.replace(/^Bearer\s+/i, '')
  if (!token) return res.status(401).json({ message: 'Thiếu access token.' })
  try {
    req.user = jwt.verify(token, accessSecret) as AuthRequest['user']
    next()
  } catch {
    return res.status(401).json({ message: 'Access token không hợp lệ hoặc đã hết hạn.' })
  }
}
const requireAdmin = (req: AuthRequest, res: Response, next: NextFunction) =>
  req.user?.role === 'ADMIN' ? next() : res.status(403).json({ message: 'Chỉ quản trị viên được phép thực hiện.' })
const hash = (value: string) => crypto.createHash('sha256').update(value).digest('hex')
const publicUser = (user: { id: string; username: string; email: string; fullName: string; role: string; memberNumber: string | null }) =>
  ({ id: user.id, username: user.username, email: user.email, fullName: user.fullName, role: user.role, memberNumber: user.memberNumber })
const issueTokens = async (user: { id: string; role: 'CUSTOMER' | 'ADMIN' }) => {
  const accessToken = jwt.sign({ id: user.id, role: user.role }, accessSecret, { expiresIn: '15m' })
  const refreshToken = jwt.sign({ id: user.id, role: user.role }, refreshSecret, { expiresIn: '7d' })
  await prisma.refreshToken.create({ data: { tokenHash: hash(refreshToken), userId: user.id, expiresAt: new Date(Date.now() + 7 * 86400000) } })
  return { accessToken, refreshToken }
}
const bookingCode = () => `BAM${crypto.randomBytes(3).toString('hex').toUpperCase()}`

app.get('/health', (_req, res) => res.json({ status: 'ok', service: 'bamboo-airways-booking-api' }))

app.post('/api/auth/register', asyncRoute(async (req, res) => {
  const body = z.object({
    username: z.string().min(3), email: z.string().email(), password: z.string().min(8),
    fullName: z.string().min(2), identityNumber: z.string().min(6).optional(),
    phone: z.string().min(8).optional(), dateOfBirth: z.coerce.date().optional(), gender: z.string().optional()
  }).parse(req.body)
  const exists = await prisma.user.findFirst({ where: { OR: [{ username: body.username }, { email: body.email }, ...(body.identityNumber ? [{ identityNumber: body.identityNumber }] : [])] } })
  if (exists) return res.status(409).json({ message: 'Tên đăng nhập, email hoặc giấy tờ đã tồn tại.' })
  const { password, ...profile } = body
  const user = await prisma.user.create({
    data: { ...profile, passwordHash: await bcrypt.hash(password, 12), memberNumber: `BAM${Date.now().toString().slice(-7)}` }
  })
  const tokens = await issueTokens({ id: user.id, role: user.role })
  res.status(201).json({ user: publicUser(user), ...tokens })
}))

app.post('/api/auth/login', asyncRoute(async (req, res) => {
  const body = z.object({ username: z.string().min(1), password: z.string().min(1) }).parse(req.body)
  const user = await prisma.user.findFirst({ where: { OR: [{ username: body.username }, { email: body.username }] } })
  if (!user || !(await bcrypt.compare(body.password, user.passwordHash))) return res.status(401).json({ message: 'Thông tin đăng nhập không chính xác.' })
  res.json({ user: publicUser(user), ...(await issueTokens({ id: user.id, role: user.role })) })
}))

app.post('/api/auth/refresh', asyncRoute(async (req, res) => {
  const body = z.object({ refreshToken: z.string().min(1) }).parse(req.body)
  let payload: { id: string; role: 'CUSTOMER' | 'ADMIN' }
  try { payload = jwt.verify(body.refreshToken, refreshSecret) as typeof payload } catch { return res.status(401).json({ message: 'Refresh token không hợp lệ.' }) }
  const saved = await prisma.refreshToken.findFirst({ where: { tokenHash: hash(body.refreshToken), userId: payload.id, revokedAt: null, expiresAt: { gt: new Date() } } })
  if (!saved) return res.status(401).json({ message: 'Refresh token đã bị thu hồi hoặc hết hạn.' })
  await prisma.refreshToken.update({ where: { id: saved.id }, data: { revokedAt: new Date() } })
  res.json(await issueTokens(payload))
}))

app.post('/api/auth/forgot-password', asyncRoute(async (req, res) => {
  const body = z.object({ email: z.string().email() }).parse(req.body)
  const user = await prisma.user.findUnique({ where: { email: body.email } })
  if (user) {
    const token = crypto.randomBytes(32).toString('hex')
    await prisma.passwordResetToken.deleteMany({ where: { userId: user.id, usedAt: null } })
    await prisma.passwordResetToken.create({ data: { tokenHash: hash(token), userId: user.id, expiresAt: new Date(Date.now() + 15 * 60000) } })
    // A mail adapter should send this one-time token in production.
    console.info(`Password reset requested for ${user.email}; token delivery is delegated to the mail provider.`)
  }
  res.json({ message: 'Nếu email tồn tại, liên kết đặt lại mật khẩu đã được gửi.' })
}))

app.post('/api/auth/reset-password', asyncRoute(async (req, res) => {
  const body = z.object({ token: z.string().min(20), password: z.string().min(8) }).parse(req.body)
  const reset = await prisma.passwordResetToken.findFirst({ where: { tokenHash: hash(body.token), usedAt: null, expiresAt: { gt: new Date() } } })
  if (!reset) return res.status(400).json({ message: 'Liên kết đặt lại mật khẩu không hợp lệ hoặc đã hết hạn.' })
  await prisma.$transaction([
    prisma.user.update({ where: { id: reset.userId }, data: { passwordHash: await bcrypt.hash(body.password, 12) } }),
    prisma.passwordResetToken.update({ where: { id: reset.id }, data: { usedAt: new Date() } }),
    prisma.refreshToken.updateMany({ where: { userId: reset.userId, revokedAt: null }, data: { revokedAt: new Date() } })
  ])
  res.json({ message: 'Đổi mật khẩu thành công.' })
}))

app.get('/api/airports', asyncRoute(async (_req, res) => res.json(await prisma.airport.findMany({ orderBy: { code: 'asc' } }))))

app.get('/api/flights', asyncRoute(async (req, res) => {
  const query = z.object({ from: z.string().length(3), to: z.string().length(3), date: z.coerce.date(), passengers: z.coerce.number().int().min(1).max(9).default(1) }).parse(req.query)
  const start = new Date(query.date); start.setHours(0, 0, 0, 0)
  const end = new Date(start); end.setDate(end.getDate() + 1)
  const flights = await prisma.flight.findMany({
    where: { departureAirport: { code: query.from.toUpperCase() }, arrivalAirport: { code: query.to.toUpperCase() }, departureAt: { gte: start, lt: end }, status: 'SCHEDULED' },
    include: { departureAirport: true, arrivalAirport: true, aircraft: true, fares: { where: { availableSeats: { gte: query.passengers } } } },
    orderBy: { departureAt: 'asc' }
  })
  res.json(flights.map((flight) => ({ ...flight, durationMinutes: Math.round((flight.arrivalAt.getTime() - flight.departureAt.getTime()) / 60000) })))
}))

app.get('/api/flights/:id/seats', asyncRoute(async (req, res) => {
  const seats = await prisma.seat.findMany({ where: { flightId: String(req.params.id) }, orderBy: [{ code: 'asc' }] })
  res.json(seats)
}))

app.post('/api/bookings', requireAuth, asyncRoute(async (req, res) => {
  const body = z.object({
    flightId: z.string(), cabinClass: z.string(), fareName: z.string(),
    contactEmail: z.string().email(), contactPhone: z.string().min(8),
    passenger: z.object({ title: z.string(), firstName: z.string().min(1), lastName: z.string().min(1), identityNumber: z.string().min(6), dateOfBirth: z.coerce.date().optional(), memberNumber: z.string().optional() }),
    seatCode: z.string().optional()
  }).parse(req.body)
  const result = await prisma.$transaction(async (tx) => {
    const fare = await tx.fare.findFirst({ where: { flightId: body.flightId, cabinClass: body.cabinClass, fareName: body.fareName, availableSeats: { gt: 0 } } })
    if (!fare) throw new Error('Hạng vé không còn chỗ.')
    let seatFee = 0
    if (body.seatCode) {
      const seat = await tx.seat.findFirst({ where: { flightId: body.flightId, code: body.seatCode, status: 'AVAILABLE' } })
      if (!seat) throw new Error('Ghế vừa được giữ bởi hành khách khác.')
      seatFee = seat.price
    }
    const tax = Math.round(fare.price * 0.306)
    const booking = await tx.booking.create({ data: {
      bookingCode: bookingCode(), userId: req.user!.id, flightId: body.flightId, cabinClass: body.cabinClass, fareName: body.fareName,
      contactEmail: body.contactEmail, contactPhone: body.contactPhone, subtotal: fare.price, tax, seatFee, total: fare.price + tax + seatFee,
      expiresAt: new Date(Date.now() + 20 * 60000), passengers: { create: body.passenger }
    }, include: { passengers: true } })
    await tx.fare.update({ where: { id: fare.id }, data: { availableSeats: { decrement: 1 } } })
    if (body.seatCode) await tx.seat.update({ where: { flightId_code: { flightId: body.flightId, code: body.seatCode } }, data: { status: 'HELD', bookingId: booking.id } })
    return booking
  })
  res.status(201).json(result)
}))

app.get('/api/bookings', requireAuth, asyncRoute(async (req, res) => {
  res.json(await prisma.booking.findMany({ where: { userId: req.user!.id }, include: { flight: { include: { departureAirport: true, arrivalAirport: true } }, passengers: true, payments: true, seats: true }, orderBy: { createdAt: 'desc' } }))
}))

app.get('/api/bookings/:code', requireAuth, asyncRoute(async (req, res) => {
  const booking = await prisma.booking.findFirst({ where: { bookingCode: String(req.params.code), userId: req.user!.id }, include: { flight: { include: { departureAirport: true, arrivalAirport: true } }, passengers: true, payments: true, seats: true } })
  if (!booking) return res.status(404).json({ message: 'Không tìm thấy mã đặt chỗ.' })
  res.json(booking)
}))

app.post('/api/bookings/:code/payments', requireAuth, asyncRoute(async (req, res) => {
  const body = z.object({ method: z.enum(['CARD', 'VIETQR', 'VNPAY', 'MOMO', 'ATM', 'PAY_LATER']) }).parse(req.body)
  const booking = await prisma.booking.findFirst({ where: { bookingCode: String(req.params.code), userId: req.user!.id } })
  if (!booking || booking.status === 'CANCELLED' || booking.status === 'EXPIRED') return res.status(404).json({ message: 'Đặt chỗ không hợp lệ.' })
  const payment = await prisma.payment.create({ data: { bookingId: booking.id, method: body.method, amount: booking.total, status: 'PAID', providerRef: `DEMO-${Date.now()}`, paidAt: new Date() } })
  await prisma.booking.update({ where: { id: booking.id }, data: { status: 'CONFIRMED' } })
  res.status(201).json({ payment, bookingCode: booking.bookingCode })
}))

app.get('/api/reviews', asyncRoute(async (_req, res) => res.json(await prisma.review.findMany({ where: { approved: true }, include: { user: { select: { fullName: true } } }, orderBy: { createdAt: 'desc' } }))))
app.post('/api/reviews', requireAuth, asyncRoute(async (req, res) => {
  const body = z.object({ rating: z.number().int().min(1).max(5), comment: z.string().min(10).max(1000) }).parse(req.body)
  res.status(201).json(await prisma.review.create({ data: { ...body, userId: req.user!.id }, include: { user: { select: { fullName: true } } } }))
}))
app.get('/api/loyalty/transactions', requireAuth, asyncRoute(async (req, res) => res.json(await prisma.loyaltyEntry.findMany({ where: { userId: req.user!.id }, orderBy: { createdAt: 'desc' } }))))

app.get('/api/admin/bookings', requireAuth, requireAdmin, asyncRoute(async (_req, res) => res.json(await prisma.booking.findMany({ include: { user: { select: { fullName: true, email: true } }, flight: true }, orderBy: { createdAt: 'desc' } }))))
app.post('/api/admin/flights', requireAuth, requireAdmin, asyncRoute(async (req, res) => {
  const body = z.object({ flightNumber: z.string(), departureAirportId: z.string(), arrivalAirportId: z.string(), aircraftId: z.string(), departureAt: z.coerce.date(), arrivalAt: z.coerce.date(), basePrice: z.number().int().positive(), businessPrice: z.number().int().positive() }).parse(req.body)
  const flight = await prisma.flight.create({ data: body })
  res.status(201).json(flight)
}))

app.use((error: unknown, _req: Request, res: Response, _next: NextFunction) => {
  if (error instanceof z.ZodError) return res.status(400).json({ message: 'Dữ liệu không hợp lệ.', issues: error.issues })
  if (error instanceof Error && error.message === 'Hạng vé không còn chỗ.') return res.status(409).json({ message: error.message })
  if (error instanceof Error && error.message === 'Ghế vừa được giữ bởi hành khách khác.') return res.status(409).json({ message: error.message })
  console.error(error)
  res.status(500).json({ message: 'Đã xảy ra lỗi máy chủ.' })
})

app.listen(port, () => console.log(`Bamboo booking API listening on http://localhost:${port}`))
