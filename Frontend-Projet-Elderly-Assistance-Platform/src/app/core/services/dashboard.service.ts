import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

export interface DashboardStats {
  totalAssisted: number;
  activeCaregivers: number;
  urgentAlerts: number;
  elderlyStable: number;
  elderlyWarning: number;
  elderlyCritical: number;
}

export interface Alert {
  id: number;
  alertType: string;
  priority?: string;
  description: string;
  timestamp: string;
  isResolved: boolean;
  resolvedAt?: string | null;
  resolvedByEmail?: string | null;
  elderlyPerson?: { id: number; firstName: string; lastName: string };
}

export interface AlertPageResponse {
  content: Alert[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface ElderlyOption {
  id: number;
  firstName: string;
  lastName: string;
  careStatus?: string;
}

export interface ReportSummary {
  status: string;
  message?: string;
  totalElderly: number;
  totalCaregivers: number;
  unresolvedAlerts: number;
  alertsInRangeCount?: number;
  alertsByDay: Record<string, number>;
  from: string;
  to: string;
}

export interface AlertsQuery {
  page?: number;
  size?: number;
  resolved?: boolean;
  priority?: string;
  from?: string;
  to?: string;
}

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private http = inject(HttpClient);

  getStats(): Observable<DashboardStats> {
    return this.http.get<DashboardStats>('/api/dashboard/stats');
  }

  getRecentAlerts(): Observable<Alert[]> {
    return this.http.get<Alert[]>('/api/alerts/recent');
  }

  getAllAlertsPaged(query: AlertsQuery = {}): Observable<AlertPageResponse> {
    let params = new HttpParams();
    if (query.page != null) {
      params = params.set('page', String(query.page));
    }
    if (query.size != null) {
      params = params.set('size', String(query.size));
    }
    if (query.priority != null && query.priority !== '') {
      params = params.set('priority', query.priority);
    }
    if (query.from != null && query.from !== '') {
      params = params.set('from', query.from);
    }
    if (query.to != null && query.to !== '') {
      params = params.set('to', query.to);
    }
    if (query.resolved !== undefined) {
      params = params.set('resolved', String(query.resolved));
    }
    return this.http.get<AlertPageResponse>('/api/alerts', { params });
  }

  resolveAlert(id: number): Observable<Alert> {
    return this.http.put<Alert>(`/api/alerts/${id}/resolve`, {});
  }

  getElderlyPersons(): Observable<ElderlyOption[]> {
    return this.http.get<ElderlyOption[]>('/api/elderly-persons');
  }

  createAlert(body: {
    elderlyPersonId: number;
    alertType: string;
    description: string;
    priority?: string;
  }): Observable<Alert> {
    return this.http.post<Alert>('/api/alerts', body);
  }

  updateAlert(
    id: number,
    body: { alertType?: string; description?: string; isResolved?: boolean; priority?: string }
  ): Observable<Alert> {
    return this.http.put<Alert>(`/api/alerts/${id}`, body);
  }

  getReportSummary(filters: {
    from?: string;
    to?: string;
    caregiverId?: number;
  }): Observable<ReportSummary> {
    let params = new HttpParams();
    if (filters.from) {
      params = params.set('from', filters.from);
    }
    if (filters.to) {
      params = params.set('to', filters.to);
    }
    if (filters.caregiverId != null) {
      params = params.set('caregiverId', String(filters.caregiverId));
    }
    return this.http.get<ReportSummary>('/api/reports/summary', { params });
  }

  downloadReportCsv(filters: { from?: string; to?: string; caregiverId?: number }): Observable<Blob> {
    let params = new HttpParams();
    if (filters.from) {
      params = params.set('from', filters.from);
    }
    if (filters.to) {
      params = params.set('to', filters.to);
    }
    if (filters.caregiverId != null) {
      params = params.set('caregiverId', String(filters.caregiverId));
    }
    return this.http
      .get('/api/reports/summary.csv', {
        params,
        responseType: 'text',
        observe: 'response'
      })
      .pipe(
        map((res: HttpResponse<string>) => {
          const body = res.body ?? '';
          return new Blob([body], { type: 'text/csv;charset=utf-8' });
        })
      );
  }
}
