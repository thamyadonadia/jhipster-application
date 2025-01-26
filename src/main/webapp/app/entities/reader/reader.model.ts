import dayjs from 'dayjs/esm';

export interface IReader {
  id: number;
  firstName?: string | null;
  lastName?: string | null;
  email?: string | null;
  joinedDate?: dayjs.Dayjs | null;
}

export type NewReader = Omit<IReader, 'id'> & { id: null };
