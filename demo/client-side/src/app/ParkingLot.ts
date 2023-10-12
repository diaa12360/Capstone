export interface ParkingLot{
  id: string,
  name: string,
  available: boolean,
  customerName?: string,
  phoneNumber?: string,
  carNumber?: string,
  color?: string,
  startDate?: Date,
  expirationDate?: Date
}
