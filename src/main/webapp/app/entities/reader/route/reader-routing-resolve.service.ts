import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { EMPTY, Observable, of } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IReader } from '../reader.model';
import { ReaderService } from '../service/reader.service';

const readerResolve = (route: ActivatedRouteSnapshot): Observable<null | IReader> => {
  const id = route.params.id;
  if (id) {
    return inject(ReaderService)
      .find(id)
      .pipe(
        mergeMap((reader: HttpResponse<IReader>) => {
          if (reader.body) {
            return of(reader.body);
          }
          inject(Router).navigate(['404']);
          return EMPTY;
        }),
      );
  }
  return of(null);
};

export default readerResolve;
