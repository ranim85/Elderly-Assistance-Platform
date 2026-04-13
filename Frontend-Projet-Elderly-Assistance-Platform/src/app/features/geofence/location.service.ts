import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ElderlySettingsDTO {
    id?: number;
    elderlyId: number;
    homeLatitude: number;
    homeLongitude: number;
    safeZoneRadius: number;
}

export interface LocationPingDTO {
    elderlyId: number;
    latitude: number;
    longitude: number;
}

@Injectable({
  providedIn: 'root'
})
export class LocationService {
  private http = inject(HttpClient);
  private baseUrl = '/api/location';

  getSettings(elderlyId: number): Observable<ElderlySettingsDTO> {
    return this.http.get<ElderlySettingsDTO>(`${this.baseUrl}/settings/${elderlyId}`);
  }

  updateSettings(settings: ElderlySettingsDTO): Observable<ElderlySettingsDTO> {
    return this.http.put<ElderlySettingsDTO>(`${this.baseUrl}/settings`, settings);
  }

  pingLocation(ping: LocationPingDTO): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/ping`, ping);
  }
}
