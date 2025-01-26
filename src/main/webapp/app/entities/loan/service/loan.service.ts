import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import dayjs from 'dayjs/esm';

import { isPresent } from 'app/core/util/operators';
import { DATE_FORMAT } from 'app/config/input.constants';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { ILoan, NewLoan } from '../loan.model';

export type PartialUpdateLoan = Partial<ILoan> & Pick<ILoan, 'id'>;

type RestOf<T extends ILoan | NewLoan> = Omit<T, 'loanDate' | 'returnDate'> & {
  loanDate?: string | null;
  returnDate?: string | null;
};

export type RestLoan = RestOf<ILoan>;

export type NewRestLoan = RestOf<NewLoan>;

export type PartialUpdateRestLoan = RestOf<PartialUpdateLoan>;

export type EntityResponseType = HttpResponse<ILoan>;
export type EntityArrayResponseType = HttpResponse<ILoan[]>;

@Injectable({ providedIn: 'root' })
export class LoanService {
  protected readonly http = inject(HttpClient);
  protected readonly applicationConfigService = inject(ApplicationConfigService);

  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/loans');

  create(loan: NewLoan): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(loan);
    return this.http.post<RestLoan>(this.resourceUrl, copy, { observe: 'response' }).pipe(map(res => this.convertResponseFromServer(res)));
  }

  update(loan: ILoan): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(loan);
    return this.http
      .put<RestLoan>(`${this.resourceUrl}/${this.getLoanIdentifier(loan)}`, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  partialUpdate(loan: PartialUpdateLoan): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(loan);
    return this.http
      .patch<RestLoan>(`${this.resourceUrl}/${this.getLoanIdentifier(loan)}`, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http
      .get<RestLoan>(`${this.resourceUrl}/${id}`, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<RestLoan[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map(res => this.convertResponseArrayFromServer(res)));
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  getLoanIdentifier(loan: Pick<ILoan, 'id'>): number {
    return loan.id;
  }

  compareLoan(o1: Pick<ILoan, 'id'> | null, o2: Pick<ILoan, 'id'> | null): boolean {
    return o1 && o2 ? this.getLoanIdentifier(o1) === this.getLoanIdentifier(o2) : o1 === o2;
  }

  addLoanToCollectionIfMissing<Type extends Pick<ILoan, 'id'>>(
    loanCollection: Type[],
    ...loansToCheck: (Type | null | undefined)[]
  ): Type[] {
    const loans: Type[] = loansToCheck.filter(isPresent);
    if (loans.length > 0) {
      const loanCollectionIdentifiers = loanCollection.map(loanItem => this.getLoanIdentifier(loanItem));
      const loansToAdd = loans.filter(loanItem => {
        const loanIdentifier = this.getLoanIdentifier(loanItem);
        if (loanCollectionIdentifiers.includes(loanIdentifier)) {
          return false;
        }
        loanCollectionIdentifiers.push(loanIdentifier);
        return true;
      });
      return [...loansToAdd, ...loanCollection];
    }
    return loanCollection;
  }

  protected convertDateFromClient<T extends ILoan | NewLoan | PartialUpdateLoan>(loan: T): RestOf<T> {
    return {
      ...loan,
      loanDate: loan.loanDate?.format(DATE_FORMAT) ?? null,
      returnDate: loan.returnDate?.format(DATE_FORMAT) ?? null,
    };
  }

  protected convertDateFromServer(restLoan: RestLoan): ILoan {
    return {
      ...restLoan,
      loanDate: restLoan.loanDate ? dayjs(restLoan.loanDate) : undefined,
      returnDate: restLoan.returnDate ? dayjs(restLoan.returnDate) : undefined,
    };
  }

  protected convertResponseFromServer(res: HttpResponse<RestLoan>): HttpResponse<ILoan> {
    return res.clone({
      body: res.body ? this.convertDateFromServer(res.body) : null,
    });
  }

  protected convertResponseArrayFromServer(res: HttpResponse<RestLoan[]>): HttpResponse<ILoan[]> {
    return res.clone({
      body: res.body ? res.body.map(item => this.convertDateFromServer(item)) : null,
    });
  }
}
