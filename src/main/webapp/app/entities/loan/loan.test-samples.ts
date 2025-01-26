import dayjs from 'dayjs/esm';

import { ILoan, NewLoan } from './loan.model';

export const sampleWithRequiredData: ILoan = {
  id: 12643,
  loanDate: dayjs('2025-01-25'),
};

export const sampleWithPartialData: ILoan = {
  id: 19895,
  loanDate: dayjs('2025-01-25'),
  returnDate: dayjs('2025-01-25'),
};

export const sampleWithFullData: ILoan = {
  id: 27230,
  loanDate: dayjs('2025-01-25'),
  returnDate: dayjs('2025-01-25'),
};

export const sampleWithNewData: NewLoan = {
  loanDate: dayjs('2025-01-25'),
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
