import { IBook } from 'app/entities/book/book.model';

export interface IAuthor {
  id: number;
  firstName?: string | null;
  lastName?: string | null;
  books?: IBook[] | null;
}

export type NewAuthor = Omit<IAuthor, 'id'> & { id: null };
