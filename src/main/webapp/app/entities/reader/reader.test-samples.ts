import dayjs from 'dayjs/esm';

import { IReader, NewReader } from './reader.model';

export const sampleWithRequiredData: IReader = {
  id: 20850,
  firstName: 'Laverne',
  lastName: 'Smith',
  email: 'Howell.Lang@hotmail.com',
};

export const sampleWithPartialData: IReader = {
  id: 26341,
  firstName: 'Bernadine',
  lastName: 'Boyle',
  email: 'Emma_Zemlak41@gmail.com',
  joinedDate: dayjs('2025-01-25'),
};

export const sampleWithFullData: IReader = {
  id: 26816,
  firstName: 'Anjali',
  lastName: 'Howell',
  email: 'Chauncey70@gmail.com',
  joinedDate: dayjs('2025-01-25'),
};

export const sampleWithNewData: NewReader = {
  firstName: 'Felicita',
  lastName: 'Graham',
  email: 'Emilia.Beier@hotmail.com',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
