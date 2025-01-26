import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse, provideHttpClient } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Subject, from, of } from 'rxjs';

import { IBook } from 'app/entities/book/book.model';
import { BookService } from 'app/entities/book/service/book.service';
import { IReader } from 'app/entities/reader/reader.model';
import { ReaderService } from 'app/entities/reader/service/reader.service';
import { ILoan } from '../loan.model';
import { LoanService } from '../service/loan.service';
import { LoanFormService } from './loan-form.service';

import { LoanUpdateComponent } from './loan-update.component';

describe('Loan Management Update Component', () => {
  let comp: LoanUpdateComponent;
  let fixture: ComponentFixture<LoanUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let loanFormService: LoanFormService;
  let loanService: LoanService;
  let bookService: BookService;
  let readerService: ReaderService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [LoanUpdateComponent],
      providers: [
        provideHttpClient(),
        FormBuilder,
        {
          provide: ActivatedRoute,
          useValue: {
            params: from([{}]),
          },
        },
      ],
    })
      .overrideTemplate(LoanUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(LoanUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    loanFormService = TestBed.inject(LoanFormService);
    loanService = TestBed.inject(LoanService);
    bookService = TestBed.inject(BookService);
    readerService = TestBed.inject(ReaderService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('Should call Book query and add missing value', () => {
      const loan: ILoan = { id: 441 };
      const book: IBook = { id: 32624 };
      loan.book = book;

      const bookCollection: IBook[] = [{ id: 32624 }];
      jest.spyOn(bookService, 'query').mockReturnValue(of(new HttpResponse({ body: bookCollection })));
      const additionalBooks = [book];
      const expectedCollection: IBook[] = [...additionalBooks, ...bookCollection];
      jest.spyOn(bookService, 'addBookToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ loan });
      comp.ngOnInit();

      expect(bookService.query).toHaveBeenCalled();
      expect(bookService.addBookToCollectionIfMissing).toHaveBeenCalledWith(
        bookCollection,
        ...additionalBooks.map(expect.objectContaining),
      );
      expect(comp.booksSharedCollection).toEqual(expectedCollection);
    });

    it('Should call Reader query and add missing value', () => {
      const loan: ILoan = { id: 441 };
      const member: IReader = { id: 4627 };
      loan.member = member;

      const readerCollection: IReader[] = [{ id: 4627 }];
      jest.spyOn(readerService, 'query').mockReturnValue(of(new HttpResponse({ body: readerCollection })));
      const additionalReaders = [member];
      const expectedCollection: IReader[] = [...additionalReaders, ...readerCollection];
      jest.spyOn(readerService, 'addReaderToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ loan });
      comp.ngOnInit();

      expect(readerService.query).toHaveBeenCalled();
      expect(readerService.addReaderToCollectionIfMissing).toHaveBeenCalledWith(
        readerCollection,
        ...additionalReaders.map(expect.objectContaining),
      );
      expect(comp.readersSharedCollection).toEqual(expectedCollection);
    });

    it('Should update editForm', () => {
      const loan: ILoan = { id: 441 };
      const book: IBook = { id: 32624 };
      loan.book = book;
      const member: IReader = { id: 4627 };
      loan.member = member;

      activatedRoute.data = of({ loan });
      comp.ngOnInit();

      expect(comp.booksSharedCollection).toContainEqual(book);
      expect(comp.readersSharedCollection).toContainEqual(member);
      expect(comp.loan).toEqual(loan);
    });
  });

  describe('save', () => {
    it('Should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<ILoan>>();
      const loan = { id: 1685 };
      jest.spyOn(loanFormService, 'getLoan').mockReturnValue(loan);
      jest.spyOn(loanService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ loan });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: loan }));
      saveSubject.complete();

      // THEN
      expect(loanFormService.getLoan).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(loanService.update).toHaveBeenCalledWith(expect.objectContaining(loan));
      expect(comp.isSaving).toEqual(false);
    });

    it('Should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<ILoan>>();
      const loan = { id: 1685 };
      jest.spyOn(loanFormService, 'getLoan').mockReturnValue({ id: null });
      jest.spyOn(loanService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ loan: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: loan }));
      saveSubject.complete();

      // THEN
      expect(loanFormService.getLoan).toHaveBeenCalled();
      expect(loanService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('Should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<ILoan>>();
      const loan = { id: 1685 };
      jest.spyOn(loanService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ loan });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(loanService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
    describe('compareBook', () => {
      it('Should forward to bookService', () => {
        const entity = { id: 32624 };
        const entity2 = { id: 17120 };
        jest.spyOn(bookService, 'compareBook');
        comp.compareBook(entity, entity2);
        expect(bookService.compareBook).toHaveBeenCalledWith(entity, entity2);
      });
    });

    describe('compareReader', () => {
      it('Should forward to readerService', () => {
        const entity = { id: 4627 };
        const entity2 = { id: 18215 };
        jest.spyOn(readerService, 'compareReader');
        comp.compareReader(entity, entity2);
        expect(readerService.compareReader).toHaveBeenCalledWith(entity, entity2);
      });
    });
  });
});
