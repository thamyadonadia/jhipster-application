import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import dayjs from 'dayjs/esm';

import { isPresent } from 'app/core/util/operators';
import { DATE_FORMAT } from 'app/config/input.constants';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { IReader, NewReader } from '../reader.model';

export type PartialUpdateReader = Partial<IReader> & Pick<IReader, 'id'>;

type RestOf<T extends IReader | NewReader> = Omit<T, 'joinedDate'> & {
  joinedDate?: string | null;
};

export type RestReader = RestOf<IReader>;

export type NewRestReader = RestOf<NewReader>;

export type PartialUpdateRestReader = RestOf<PartialUpdateReader>;

export type EntityResponseType = HttpResponse<IReader>;
export type EntityArrayResponseType = HttpResponse<IReader[]>;

@Injectable({ providedIn: 'root' })
export class ReaderService {
  protected readonly http = inject(HttpClient);
  protected readonly applicationConfigService = inject(ApplicationConfigService);

  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/readers');

  create(reader: NewReader): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(reader);
    return this.http
      .post<RestReader>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  update(reader: IReader): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(reader);
    return this.http
      .put<RestReader>(`${this.resourceUrl}/${this.getReaderIdentifier(reader)}`, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  partialUpdate(reader: PartialUpdateReader): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(reader);
    return this.http
      .patch<RestReader>(`${this.resourceUrl}/${this.getReaderIdentifier(reader)}`, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http
      .get<RestReader>(`${this.resourceUrl}/${id}`, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<RestReader[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map(res => this.convertResponseArrayFromServer(res)));
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  getReaderIdentifier(reader: Pick<IReader, 'id'>): number {
    return reader.id;
  }

  compareReader(o1: Pick<IReader, 'id'> | null, o2: Pick<IReader, 'id'> | null): boolean {
    return o1 && o2 ? this.getReaderIdentifier(o1) === this.getReaderIdentifier(o2) : o1 === o2;
  }

  addReaderToCollectionIfMissing<Type extends Pick<IReader, 'id'>>(
    readerCollection: Type[],
    ...readersToCheck: (Type | null | undefined)[]
  ): Type[] {
    const readers: Type[] = readersToCheck.filter(isPresent);
    if (readers.length > 0) {
      const readerCollectionIdentifiers = readerCollection.map(readerItem => this.getReaderIdentifier(readerItem));
      const readersToAdd = readers.filter(readerItem => {
        const readerIdentifier = this.getReaderIdentifier(readerItem);
        if (readerCollectionIdentifiers.includes(readerIdentifier)) {
          return false;
        }
        readerCollectionIdentifiers.push(readerIdentifier);
        return true;
      });
      return [...readersToAdd, ...readerCollection];
    }
    return readerCollection;
  }

  protected convertDateFromClient<T extends IReader | NewReader | PartialUpdateReader>(reader: T): RestOf<T> {
    return {
      ...reader,
      joinedDate: reader.joinedDate?.format(DATE_FORMAT) ?? null,
    };
  }

  protected convertDateFromServer(restReader: RestReader): IReader {
    return {
      ...restReader,
      joinedDate: restReader.joinedDate ? dayjs(restReader.joinedDate) : undefined,
    };
  }

  protected convertResponseFromServer(res: HttpResponse<RestReader>): HttpResponse<IReader> {
    return res.clone({
      body: res.body ? this.convertDateFromServer(res.body) : null,
    });
  }

  protected convertResponseArrayFromServer(res: HttpResponse<RestReader[]>): HttpResponse<IReader[]> {
    return res.clone({
      body: res.body ? res.body.map(item => this.convertDateFromServer(item)) : null,
    });
  }
}
