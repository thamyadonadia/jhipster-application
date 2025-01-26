import { IAuthor, NewAuthor } from './author.model';

export const sampleWithRequiredData: IAuthor = {
  id: 24433,
  firstName: 'Leonardo',
  lastName: 'Bednar',
};

export const sampleWithPartialData: IAuthor = {
  id: 12726,
  firstName: 'Adriel',
  lastName: 'Kemmer',
};

export const sampleWithFullData: IAuthor = {
  id: 16232,
  firstName: 'Jena',
  lastName: 'Beatty',
};

export const sampleWithNewData: NewAuthor = {
  firstName: 'Mossie',
  lastName: 'Tillman',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
