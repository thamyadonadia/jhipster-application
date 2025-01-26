import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

import { DATE_FORMAT } from 'app/config/input.constants';
import { ILoan } from '../loan.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../loan.test-samples';

import { LoanService, RestLoan } from './loan.service';

const requireRestSample: RestLoan = {
  ...sampleWithRequiredData,
  loanDate: sampleWithRequiredData.loanDate?.format(DATE_FORMAT),
  returnDate: sampleWithRequiredData.returnDate?.format(DATE_FORMAT),
};

describe('Loan Service', () => {
  let service: LoanService;
  let httpMock: HttpTestingController;
  let expectedResult: ILoan | ILoan[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    expectedResult = null;
    service = TestBed.inject(LoanService);
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

    it('should create a Loan', () => {
      const loan = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(loan).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a Loan', () => {
      const loan = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(loan).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a Loan', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of Loan', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a Loan', () => {
      const expected = true;

      service.delete(123).subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult).toBe(expected);
    });

    describe('addLoanToCollectionIfMissing', () => {
      it('should add a Loan to an empty array', () => {
        const loan: ILoan = sampleWithRequiredData;
        expectedResult = service.addLoanToCollectionIfMissing([], loan);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(loan);
      });

      it('should not add a Loan to an array that contains it', () => {
        const loan: ILoan = sampleWithRequiredData;
        const loanCollection: ILoan[] = [
          {
            ...loan,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addLoanToCollectionIfMissing(loanCollection, loan);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a Loan to an array that doesn't contain it", () => {
        const loan: ILoan = sampleWithRequiredData;
        const loanCollection: ILoan[] = [sampleWithPartialData];
        expectedResult = service.addLoanToCollectionIfMissing(loanCollection, loan);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(loan);
      });

      it('should add only unique Loan to an array', () => {
        const loanArray: ILoan[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const loanCollection: ILoan[] = [sampleWithRequiredData];
        expectedResult = service.addLoanToCollectionIfMissing(loanCollection, ...loanArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const loan: ILoan = sampleWithRequiredData;
        const loan2: ILoan = sampleWithPartialData;
        expectedResult = service.addLoanToCollectionIfMissing([], loan, loan2);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(loan);
        expect(expectedResult).toContain(loan2);
      });

      it('should accept null and undefined values', () => {
        const loan: ILoan = sampleWithRequiredData;
        expectedResult = service.addLoanToCollectionIfMissing([], null, loan, undefined);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(loan);
      });

      it('should return initial array if no Loan is added', () => {
        const loanCollection: ILoan[] = [sampleWithRequiredData];
        expectedResult = service.addLoanToCollectionIfMissing(loanCollection, undefined, null);
        expect(expectedResult).toEqual(loanCollection);
      });
    });

    describe('compareLoan', () => {
      it('Should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareLoan(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('Should return false if one entity is null', () => {
        const entity1 = { id: 1685 };
        const entity2 = null;

        const compareResult1 = service.compareLoan(entity1, entity2);
        const compareResult2 = service.compareLoan(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey differs', () => {
        const entity1 = { id: 1685 };
        const entity2 = { id: 441 };

        const compareResult1 = service.compareLoan(entity1, entity2);
        const compareResult2 = service.compareLoan(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey matches', () => {
        const entity1 = { id: 1685 };
        const entity2 = { id: 1685 };

        const compareResult1 = service.compareLoan(entity1, entity2);
        const compareResult2 = service.compareLoan(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
