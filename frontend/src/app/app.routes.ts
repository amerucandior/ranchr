import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { LoginComponent } from './features/auth/components/login/login.component';
import { RegisterComponent } from './features/auth/components/register/register.component';
import { KycVerificationComponent } from './features/kyc/components/kyc-verification/kyc-verification.component';
import { FarmerDispatchComponent } from './features/vet-dispatch/components/farmer-dispatch/farmer-dispatch.component';
import { VetConsoleComponent } from './features/vet-dispatch/components/vet-console/vet-console.component';
import { RequestTrackerComponent } from './features/vet-dispatch/components/request-tracker/request-tracker.component';
import { CycleListComponent } from './features/reproduction/components/cycle-list/cycle-list.component';
import { MarketplaceCatalogComponent } from './features/marketplace/components/marketplace-catalog/marketplace-catalog.component';
import { CreateListingComponent } from './features/marketplace/components/create-listing/create-listing.component';
import { ConnectionsComponent } from './features/marketplace/components/connections/connections.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'marketplace', component: MarketplaceCatalogComponent },
  {
    path: 'marketplace/new',
    component: CreateListingComponent,
    canActivate: [authGuard],
  },
  {
    path: 'connections',
    component: ConnectionsComponent,
    canActivate: [authGuard],
  },
  {
    path: 'kyc',
    component: KycVerificationComponent,
    canActivate: [authGuard],
  },
  {
    path: 'dispatch',
    component: FarmerDispatchComponent,
    canActivate: [authGuard],
  },
  {
    path: 'vet-console',
    component: VetConsoleComponent,
    canActivate: [authGuard],
  },
  {
    path: 'tracker/:id',
    component: RequestTrackerComponent,
    canActivate: [authGuard],
  },
  {
    path: 'reproduction',
    component: CycleListComponent,
    canActivate: [authGuard],
  },
  { path: '', redirectTo: 'dispatch', pathMatch: 'full' },
  { path: '**', redirectTo: 'dispatch' },
];
