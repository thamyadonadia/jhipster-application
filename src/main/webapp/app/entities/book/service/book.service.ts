import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import dayjs from 'dayjs/esm';

import { isPresent } from 'app/core/util/operators';
import { DATE_FORMAT } from 'app/config/input.constants';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { IBook, NewBook } from '../book.model';

export type PartialUpdateBook = Partial<IBook> & Pick<IBook, 'id'>;

type RestOf<T extends IBook | NewBook> = Omit<T, 'publicationDate'> & {
  publicationDate?: string | null;
};

export type RestBook = RestOf<IBook>;

export type NewRestBook = RestOf<NewBook>;

export type PartialUpdateRestBook = RestOf<PartialUpdateBook>;

export type EntityResponseType = HttpResponse<IBook>;
export type EntityArrayResponseType = HttpResponse<IBook[]>;

@Injectable({ providedIn: 'root' })
export class BookService {
  protected readonly http = inject(HttpClient);
  protected readonly applicationConfigService = inject(ApplicationConfigService);

  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/books');

  create(book: NewBook): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(book);
    return this.http.post<RestBook>(this.resourceUrl, copy, { observe: 'response' }).pipe(map(res => this.convertResponseFromServer(res)));
  }

  update(book: IBook): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(book);
    return this.http
      .put<RestBook>(`${this.resourceUrl}/${this.getBookIdentifier(book)}`, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  partialUpdate(book: PartialUpdateBook): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(book);
    return this.http
      .patch<RestBook>(`${this.resourceUrl}/${this.getBookIdentifier(book)}`, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http
      .get<RestBook>(`${this.resourceUrl}/${id}`, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<RestBook[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map(res => this.convertResponseArrayFromServer(res)));
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  getBookIdentifier(book: Pick<IBook, 'id'>): number {
    return book.id;
  }

  compareBook(o1: Pick<IBook, 'id'> | null, o2: Pick<IBook, 'id'> | null): boolean {
    return o1 && o2 ? this.getBookIdentifier(o1) === this.getBookIdentifier(o2) : o1 === o2;
  }

  addBookToCollectionIfMissing<Type extends Pick<IBook, 'id'>>(
    bookCollection: Type[],
    ...booksToCheck: (Type | null | undefined)[]
  ): Type[] {
    const books: Type[] = booksToCheck.filter(isPresent);
    if (books.length > 0) {
      const bookCollectionIdentifiers = bookCollection.map(bookItem => this.getBookIdentifier(bookItem));
      const booksToAdd = books.filter(bookItem => {
        const bookIdentifier = this.getBookIdentifier(bookItem);
        if (bookCollectionIdentifiers.includes(bookIdentifier)) {
          return false;
        }
        bookCollectionIdentifiers.push(bookIdentifier);
        return true;
      });
      return [...booksToAdd, ...bookCollection];
    }
    return bookCollection;
  }

  protected convertDateFromClient<T extends IBook | NewBook | PartialUpdateBook>(book: T): RestOf<T> {
    return {
      ...book,
      publicationDate: book.publicationDate?.format(DATE_FORMAT) ?? null,
    };
  }

  protected convertDateFromServer(restBook: RestBook): IBook {
    return {
      ...restBook,
      publicationDate: restBook.publicationDate ? dayjs(restBook.publicationDate) : undefined,
    };
  }

  protected convertResponseFromServer(res: HttpResponse<RestBook>): HttpResponse<IBook> {
    return res.clone({
      body: res.body ? this.convertDateFromServer(res.body) : null,
    });
  }

  protected convertResponseArrayFromServer(res: HttpResponse<RestBook[]>): HttpResponse<IBook[]> {
    return res.clone({
      body: res.body ? res.body.map(item => this.convertDateFromServer(item)) : null,
    });
  }
}
