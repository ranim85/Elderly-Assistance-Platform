import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface TimelineEventDTO {
    id: string;
    date: Date;
    eventType: string;
    title: string;
    description: string;
    severity: string;
}

@Injectable({
  providedIn: 'root'
})
export class TimelineService {
  private http = inject(HttpClient);
  private baseUrl = '/api/timeline';

  getFamilyTimeline(): Observable<TimelineEventDTO[]> {
    return this.http.get<TimelineEventDTO[]>(this.baseUrl);
  }
}
