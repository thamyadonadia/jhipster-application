import dayjs from 'dayjs/esm';

import { IBook, NewBook } from './book.model';

export const sampleWithRequiredData: IBook = {
  id: 3991,
  title: 'blue regal',
  copiesOwned: 21315,
  status: 'AVAILABLE',
};

export const sampleWithPartialData: IBook = {
  id: 20784,
  title: 'blah legislature',
  copiesOwned: 10868,
  status: 'BORROWED',
};

export const sampleWithFullData: IBook = {
  id: 8637,
  title: 'sniff',
  publicationDate: dayjs('2025-01-25'),
  copiesOwned: 8807,
  status: 'BORROWED',
};

export const sampleWithNewData: NewBook = {
  title: 'taxicab nor',
  copiesOwned: 12703,
  status: 'UNAVAILABLE',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
