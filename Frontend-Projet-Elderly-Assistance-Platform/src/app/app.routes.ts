import { Routes } from '@angular/router';
import { LoginComponent } from './core/components/login/login.component';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { ProfileComponent } from './features/profile/profile.component';
import { UsersComponent } from './features/users/users.component';
import { AlertsComponent } from './features/alerts/alerts.component';
import { ElderlyPersonsComponent } from './features/elderly-persons/elderly-persons.component';

export const routes: Routes = [
    { path: 'login', component: LoginComponent },
    { 
        path: 'dashboard', 
        component: DashboardComponent,
        canActivate: [authGuard] 
    },
    { 
        path: 'profile', 
        component: ProfileComponent,
        canActivate: [authGuard] 
    },
    { 
        path: 'users', 
        component: UsersComponent,
        canActivate: [roleGuard],
        data: { roles: ['ADMIN'] }
    },
    { 
        path: 'alerts', 
        component: AlertsComponent,
        canActivate: [authGuard] 
    },
    { 
        path: 'elderly-persons', 
        component: ElderlyPersonsComponent,
        canActivate: [authGuard] 
    },
    {
        path: 'reports',
        loadComponent: () => import('./features/reports/reports.component').then(m => m.ReportsComponent),
        canActivate: [roleGuard],
        data: { roles: ['ADMIN', 'CAREGIVER'] }
    },
    {
        path: 'family-timeline',
        loadComponent: () => import('./features/family-timeline/family-timeline.component').then(m => m.FamilyTimelineComponent),
        canActivate: [roleGuard],
        data: { roles: ['ADMIN', 'FAMILY_MEMBER'] }
    },
    {
        path: 'medications/:elderlyId',
        loadComponent: () => import('./features/medications/medication-hub.component').then(m => m.MedicationHubComponent),
        canActivate: [authGuard]
    },
    {
        path: 'geofence/:elderlyId',
        loadComponent: () => import('./features/geofence/geofence-simulator.component').then(m => m.GeofenceSimulatorComponent),
        canActivate: [authGuard]
    },
    { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
    { path: '**', redirectTo: '/login' }
];
