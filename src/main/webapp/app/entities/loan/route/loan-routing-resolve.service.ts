import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { EMPTY, Observable, of } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { ILoan } from '../loan.model';
import { LoanService } from '../service/loan.service';

const loanResolve = (route: ActivatedRouteSnapshot): Observable<null | ILoan> => {
  const id = route.params.id;
  if (id) {
    return inject(LoanService)
      .find(id)
      .pipe(
        mergeMap((loan: HttpResponse<ILoan>) => {
          if (loan.body) {
            return of(loan.body);
          }
          inject(Router).navigate(['404']);
          return EMPTY;
        }),
      );
  }
  return of(null);
};

export default loanResolve;
