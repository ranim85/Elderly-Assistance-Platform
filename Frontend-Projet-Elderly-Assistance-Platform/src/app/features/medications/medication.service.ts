import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface MedicationDTO {
    id: number;
    name: string;
    dosage: string;
    scheduledTime: Date;
    isTaken: boolean;
    timeTaken?: Date;
    elderlyPersonId: number;
}

@Injectable({
  providedIn: 'root'
})
export class MedicationService {
  private http = inject(HttpClient);
  private baseUrl = '/api/medications';

  getMedicationsByElderly(elderlyId: number): Observable<MedicationDTO[]> {
    return this.http.get<MedicationDTO[]>(`${this.baseUrl}/elderly/${elderlyId}`);
  }

  createMedication(medication: any): Observable<MedicationDTO> {
    return this.http.post<MedicationDTO>(this.baseUrl, medication);
  }

  markAsTaken(id: number): Observable<MedicationDTO> {
    return this.http.put<MedicationDTO>(`${this.baseUrl}/${id}/take`, {});
  }
}
