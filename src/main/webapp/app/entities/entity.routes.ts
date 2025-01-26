import { Routes } from '@angular/router';

const routes: Routes = [
  {
    path: 'authority',
    data: { pageTitle: 'Authorities' },
    loadChildren: () => import('./admin/authority/authority.routes'),
  },
  {
    path: 'book',
    data: { pageTitle: 'Books' },
    loadChildren: () => import('./book/book.routes'),
  },
  {
    path: 'category',
    data: { pageTitle: 'Categories' },
    loadChildren: () => import('./category/category.routes'),
  },
  {
    path: 'author',
    data: { pageTitle: 'Authors' },
    loadChildren: () => import('./author/author.routes'),
  },
  {
    path: 'reader',
    data: { pageTitle: 'Readers' },
    loadChildren: () => import('./reader/reader.routes'),
  },
  {
    path: 'loan',
    data: { pageTitle: 'Loans' },
    loadChildren: () => import('./loan/loan.routes'),
  },
  /* jhipster-needle-add-entity-route - JHipster will add entity modules routes here */
];

export default routes;
