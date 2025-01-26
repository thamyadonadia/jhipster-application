import dayjs from 'dayjs/esm';
import { IBook } from 'app/entities/book/book.model';
import { IReader } from 'app/entities/reader/reader.model';

export interface ILoan {
  id: number;
  loanDate?: dayjs.Dayjs | null;
  returnDate?: dayjs.Dayjs | null;
  book?: IBook | null;
  member?: IReader | null;
}

export type NewLoan = Omit<ILoan, 'id'> & { id: null };
