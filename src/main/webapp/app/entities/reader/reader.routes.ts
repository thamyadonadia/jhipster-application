import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import ReaderResolve from './route/reader-routing-resolve.service';

const readerRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/reader.component').then(m => m.ReaderComponent),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/reader-detail.component').then(m => m.ReaderDetailComponent),
    resolve: {
      reader: ReaderResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/reader-update.component').then(m => m.ReaderUpdateComponent),
    resolve: {
      reader: ReaderResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/reader-update.component').then(m => m.ReaderUpdateComponent),
    resolve: {
      reader: ReaderResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default readerRoute;
