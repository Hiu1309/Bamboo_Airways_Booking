import { PrismaClient } from '@prisma/client'
import bcrypt from 'bcryptjs'

const prisma = new PrismaClient()
const airports = [
  { code: 'HAN', name: 'Sân bay quốc tế Nội Bài', city: 'Hà Nội' },
  { code: 'SGN', name: 'Sân bay quốc tế Tân Sơn Nhất', city: 'TP. Hồ Chí Minh' },
  { code: 'DAD', name: 'Sân bay quốc tế Đà Nẵng', city: 'Đà Nẵng' },
  { code: 'CXR', name: 'Sân bay quốc tế Cam Ranh', city: 'Nha Trang' }
]

async function main() {
  for (const airport of airports) {
    await prisma.airport.upsert({ where: { code: airport.code }, update: airport, create: airport })
  }
  const aircraft = await prisma.aircraft.upsert({ where: { code: 'A321-BAV' }, update: {}, create: { code: 'A321-BAV', model: 'Airbus A321', seatRows: 16 } })
  const han = await prisma.airport.findUniqueOrThrow({ where: { code: 'HAN' } })
  const sgn = await prisma.airport.findUniqueOrThrow({ where: { code: 'SGN' } })
  const departure = new Date()
  departure.setHours(7, 30, 0, 0)
  const arrival = new Date(departure)
  arrival.setHours(9, 40, 0, 0)
  const flight = await prisma.flight.upsert({
    where: { flightNumber: 'QH101' },
    update: { departureAirportId: han.id, arrivalAirportId: sgn.id, aircraftId: aircraft.id, departureAt: departure, arrivalAt: arrival, status: 'SCHEDULED' },
    create: { flightNumber: 'QH101', departureAirportId: han.id, arrivalAirportId: sgn.id, aircraftId: aircraft.id, departureAt: departure, arrivalAt: arrival, basePrice: 500000, businessPrice: 1500000 }
  })
  for (const fare of [
    { cabinClass: 'ECONOMY', fareName: 'Economy Flex', price: 500000, baggageKg: 20, refundable: true, changeable: true, availableSeats: 60 },
    { cabinClass: 'BUSINESS', fareName: 'Business Flex', price: 1500000, baggageKg: 30, refundable: true, changeable: true, availableSeats: 10 }
  ]) {
    await prisma.fare.upsert({ where: { flightId_cabinClass_fareName: { flightId: flight.id, cabinClass: fare.cabinClass, fareName: fare.fareName } }, update: fare, create: { ...fare, flightId: flight.id } })
  }
  const cols = ['A', 'B', 'C', 'D', 'G', 'H', 'K']
  for (const [row, col] of Array.from({ length: 16 }).flatMap((_, index) => cols.map((seatCol) => [index, seatCol] as const))) {
    await prisma.seat.upsert({ where: { flightId_code: { flightId: flight.id, code: `${row + 1}${col}` } }, update: {}, create: { flightId: flight.id, code: `${row + 1}${col}`, cabinClass: row < 3 ? 'BUSINESS' : 'ECONOMY', price: 0 } })
  }
  const passwordHash = await bcrypt.hash('Password123!', 12)
  await prisma.user.upsert({ where: { username: 'admin' }, update: {}, create: { username: 'admin', email: 'admin@bamboo.local', fullName: 'Bamboo Administrator', passwordHash, role: 'ADMIN' } })
  await prisma.user.upsert({ where: { username: 'minhanh' }, update: {}, create: { username: 'minhanh', email: 'minhanh@example.com', fullName: 'Nguyễn Minh Anh', passwordHash, memberNumber: 'BAM1234567', phone: '0901234567' } })
}

main().finally(() => prisma.$disconnect())
