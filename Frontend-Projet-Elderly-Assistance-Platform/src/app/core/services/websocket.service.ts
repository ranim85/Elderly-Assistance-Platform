import { Injectable, inject } from '@angular/core';
import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { Subject } from 'rxjs';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class WebsocketService {
  private authService = inject(AuthService);
  private stompClient: Client;
  /** Same-origin in dev (`ng serve` + proxy); production should serve app and API behind one origin or adjust here. */
  private readonly baseUrl =
    typeof window !== 'undefined' ? `${window.location.origin}/ws` : 'http://localhost:8080/ws';

  public alerts$ = new Subject<any>();

  constructor() {
    this.stompClient = new Client({
      // @ts-ignore
      webSocketFactory: () => new SockJS(this.baseUrl),
      debug: (str) => {
        console.log('[STOMP]', str);
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    this.stompClient.onConnect = (frame) => {
      console.log('Connected to WS: ' + frame);
      
      this.stompClient.subscribe('/user/queue/alerts', (message: IMessage) => {
        if (message.body) {
          const alertDTO = JSON.parse(message.body);
          this.alerts$.next(alertDTO);
        }
      });
      
      this.stompClient.subscribe('/topic/alerts', (message: IMessage) => {
          if (message.body) {
            const alertDTO = JSON.parse(message.body);
            this.alerts$.next(alertDTO);
          }
      });
    };

    this.stompClient.onStompError = (frame) => {
      console.error('Broker reported error: ' + frame.headers['message']);
      console.error('Additional details: ' + frame.body);
    };
  }

  public connect(): void {
    const token = this.authService.getToken();
    if (!token) {
      return;
    }
    this.stompClient.connectHeaders = {
      Authorization: `Bearer ${token}`
    };
    if (this.stompClient.active) {
      return;
    }
    this.stompClient.activate();
  }

  public disconnect(): void {
    if (this.stompClient.active) {
      this.stompClient.deactivate();
    }
  }
}
