import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {ParkingLot} from "../ParkingLot";
import { environment } from '../environment';


@Injectable({
  providedIn: 'root'
})
export class ParkingLotService {
  private baseUrl = environment.apiBaseUrl; // Replace with your Spring backend API URL

  constructor(private http: HttpClient) { }

  getParkingLots(): Observable<ParkingLot[]> {
    console.log(this.http.get(`${this.baseUrl}/parking-lot/all`));
    return this.http.get<ParkingLot[]>(`${this.baseUrl}/parking-lot/all`);
  }
  addParkingLot(parkingLot: ParkingLot): Observable<ParkingLot> {
    return this.http.post<ParkingLot>(`${this.baseUrl}/parking-lot/add`, parkingLot);
  }

  removeParkingLot(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/parking-lot/delete/${id}`);
  }

  assignParkingLot(assignmentData: ParkingLot): Observable<ParkingLot> {
    return this.http.put<ParkingLot>(`${this.baseUrl}/parking-lot/update`, assignmentData);
  }

  getParkingLotById(id: string): Observable<ParkingLot> {
    return this.http.get<ParkingLot>(`${this.baseUrl}/parking-lot/${id}`);
  }

  saveParkingLot(parkingLot: ParkingLot): Observable<ParkingLot> {
    return this.http.put<ParkingLot>(`${this.baseUrl}/parking-lot/${parkingLot.id}`, parkingLot);
  }
}
