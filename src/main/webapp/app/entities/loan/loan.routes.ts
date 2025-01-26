import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import LoanResolve from './route/loan-routing-resolve.service';

const loanRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/loan.component').then(m => m.LoanComponent),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/loan-detail.component').then(m => m.LoanDetailComponent),
    resolve: {
      loan: LoanResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/loan-update.component').then(m => m.LoanUpdateComponent),
    resolve: {
      loan: LoanResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/loan-update.component').then(m => m.LoanUpdateComponent),
    resolve: {
      loan: LoanResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default loanRoute;
