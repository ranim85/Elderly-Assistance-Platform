import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavigationStart, Router, RouterOutlet } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { filter } from 'rxjs/operators';
import { NavbarComponent } from './core/components/layout/navbar/navbar.component';
import { SidebarComponent } from './core/components/layout/sidebar/sidebar.component';
import { AuthService } from './core/services/auth.service';
import { WebsocketService } from './core/services/websocket.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, NavbarComponent, SidebarComponent],
  template: `
    <!-- Authenticated App Shell (Private) -->
    <ng-container *ngIf="authService.isAuthenticated$ | async; else publicLayout">
      <div class="flex h-screen bg-gray-50 font-sans text-gray-900">
        <!-- Sidebar -->
        <app-sidebar class="hidden md:flex z-20"></app-sidebar>
        
        <!-- Main Layout Window -->
        <div class="flex-1 flex flex-col overflow-hidden">
          <app-navbar class="z-10 relative shadow-sm"></app-navbar>

          <!-- Dynamic Content Router (Dashboard etc) -->
          <main class="flex-1 overflow-x-hidden overflow-y-auto p-6 bg-gray-50">
            <router-outlet></router-outlet>
          </main>
        </div>
      </div>
    </ng-container>

    <!-- Unauthenticated Fullscreen (Public: Login) -->
    <ng-template #publicLayout>
      <div class="h-screen w-full bg-gray-50">
        <router-outlet></router-outlet>
      </div>
    </ng-template>
  `
})
export class AppComponent {
  constructor(
    public authService: AuthService,
    private websocketService: WebsocketService,
    router: Router,
    snackBar: MatSnackBar
  ) {
    router.events
      .pipe(filter((e): e is NavigationStart => e instanceof NavigationStart))
      .subscribe(() => snackBar.dismiss());

    this.authService.isAuthenticated$.subscribe(isAuth => {
      if (isAuth) {
        this.websocketService.connect();
      } else {
        this.websocketService.disconnect();
      }
    });

    this.websocketService.alerts$.subscribe((alert) => {
      try {
        const type = alert?.alertType ?? 'ALERT';
        const desc = alert?.description ?? '';
        snackBar.open(`ALERT: ${type} — ${desc}`, 'OK', {
          duration: 10000,
          panelClass: ['bg-red-500', 'text-white']
        });
      } catch {
        snackBar.open('New alert received.', 'OK', { duration: 8000 });
      }
    });
  }
}
