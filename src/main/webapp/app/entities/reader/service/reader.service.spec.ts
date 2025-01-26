import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

import { DATE_FORMAT } from 'app/config/input.constants';
import { IReader } from '../reader.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../reader.test-samples';

import { ReaderService, RestReader } from './reader.service';

const requireRestSample: RestReader = {
  ...sampleWithRequiredData,
  joinedDate: sampleWithRequiredData.joinedDate?.format(DATE_FORMAT),
};

describe('Reader Service', () => {
  let service: ReaderService;
  let httpMock: HttpTestingController;
  let expectedResult: IReader | IReader[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    expectedResult = null;
    service = TestBed.inject(ReaderService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  describe('Service methods', () => {
    it('should find an element', () => {
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.find(123).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should create a Reader', () => {
      const reader = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(reader).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a Reader', () => {
      const reader = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(reader).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a Reader', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of Reader', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a Reader', () => {
      const expected = true;

      service.delete(123).subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult).toBe(expected);
    });

    describe('addReaderToCollectionIfMissing', () => {
      it('should add a Reader to an empty array', () => {
        const reader: IReader = sampleWithRequiredData;
        expectedResult = service.addReaderToCollectionIfMissing([], reader);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(reader);
      });

      it('should not add a Reader to an array that contains it', () => {
        const reader: IReader = sampleWithRequiredData;
        const readerCollection: IReader[] = [
          {
            ...reader,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addReaderToCollectionIfMissing(readerCollection, reader);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a Reader to an array that doesn't contain it", () => {
        const reader: IReader = sampleWithRequiredData;
        const readerCollection: IReader[] = [sampleWithPartialData];
        expectedResult = service.addReaderToCollectionIfMissing(readerCollection, reader);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(reader);
      });

      it('should add only unique Reader to an array', () => {
        const readerArray: IReader[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const readerCollection: IReader[] = [sampleWithRequiredData];
        expectedResult = service.addReaderToCollectionIfMissing(readerCollection, ...readerArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const reader: IReader = sampleWithRequiredData;
        const reader2: IReader = sampleWithPartialData;
        expectedResult = service.addReaderToCollectionIfMissing([], reader, reader2);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(reader);
        expect(expectedResult).toContain(reader2);
      });

      it('should accept null and undefined values', () => {
        const reader: IReader = sampleWithRequiredData;
        expectedResult = service.addReaderToCollectionIfMissing([], null, reader, undefined);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(reader);
      });

      it('should return initial array if no Reader is added', () => {
        const readerCollection: IReader[] = [sampleWithRequiredData];
        expectedResult = service.addReaderToCollectionIfMissing(readerCollection, undefined, null);
        expect(expectedResult).toEqual(readerCollection);
      });
    });

    describe('compareReader', () => {
      it('Should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareReader(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('Should return false if one entity is null', () => {
        const entity1 = { id: 4627 };
        const entity2 = null;

        const compareResult1 = service.compareReader(entity1, entity2);
        const compareResult2 = service.compareReader(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey differs', () => {
        const entity1 = { id: 4627 };
        const entity2 = { id: 18215 };

        const compareResult1 = service.compareReader(entity1, entity2);
        const compareResult2 = service.compareReader(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey matches', () => {
        const entity1 = { id: 4627 };
        const entity2 = { id: 4627 };

        const compareResult1 = service.compareReader(entity1, entity2);
        const compareResult2 = service.compareReader(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
