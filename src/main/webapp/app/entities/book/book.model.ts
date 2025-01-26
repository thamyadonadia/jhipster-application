import dayjs from 'dayjs/esm';
import { ICategory } from 'app/entities/category/category.model';
import { IAuthor } from 'app/entities/author/author.model';
import { BookStatusEnum } from 'app/entities/enumerations/book-status-enum.model';

export interface IBook {
  id: number;
  title?: string | null;
  publicationDate?: dayjs.Dayjs | null;
  copiesOwned?: number | null;
  status?: keyof typeof BookStatusEnum | null;
  category?: ICategory | null;
  authors?: IAuthor[] | null;
}

export type NewBook = Omit<IBook, 'id'> & { id: null };
