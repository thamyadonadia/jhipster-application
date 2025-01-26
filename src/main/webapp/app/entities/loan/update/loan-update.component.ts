import { Component, OnInit, inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IBook } from 'app/entities/book/book.model';
import { BookService } from 'app/entities/book/service/book.service';
import { IReader } from 'app/entities/reader/reader.model';
import { ReaderService } from 'app/entities/reader/service/reader.service';
import { LoanService } from '../service/loan.service';
import { ILoan } from '../loan.model';
import { LoanFormGroup, LoanFormService } from './loan-form.service';

@Component({
  selector: 'jhi-loan-update',
  templateUrl: './loan-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class LoanUpdateComponent implements OnInit {
  isSaving = false;
  loan: ILoan | null = null;

  booksSharedCollection: IBook[] = [];
  readersSharedCollection: IReader[] = [];

  protected loanService = inject(LoanService);
  protected loanFormService = inject(LoanFormService);
  protected bookService = inject(BookService);
  protected readerService = inject(ReaderService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: LoanFormGroup = this.loanFormService.createLoanFormGroup();

  compareBook = (o1: IBook | null, o2: IBook | null): boolean => this.bookService.compareBook(o1, o2);

  compareReader = (o1: IReader | null, o2: IReader | null): boolean => this.readerService.compareReader(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ loan }) => {
      this.loan = loan;
      if (loan) {
        this.updateForm(loan);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const loan = this.loanFormService.getLoan(this.editForm);
    if (loan.id !== null) {
      this.subscribeToSaveResponse(this.loanService.update(loan));
    } else {
      this.subscribeToSaveResponse(this.loanService.create(loan));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<ILoan>>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe({
      next: () => this.onSaveSuccess(),
      error: () => this.onSaveError(),
    });
  }

  protected onSaveSuccess(): void {
    this.previousState();
  }

  protected onSaveError(): void {
    // Api for inheritance.
  }

  protected onSaveFinalize(): void {
    this.isSaving = false;
  }

  protected updateForm(loan: ILoan): void {
    this.loan = loan;
    this.loanFormService.resetForm(this.editForm, loan);

    this.booksSharedCollection = this.bookService.addBookToCollectionIfMissing<IBook>(this.booksSharedCollection, loan.book);
    this.readersSharedCollection = this.readerService.addReaderToCollectionIfMissing<IReader>(this.readersSharedCollection, loan.member);
  }

  protected loadRelationshipsOptions(): void {
    this.bookService
      .query()
      .pipe(map((res: HttpResponse<IBook[]>) => res.body ?? []))
      .pipe(map((books: IBook[]) => this.bookService.addBookToCollectionIfMissing<IBook>(books, this.loan?.book)))
      .subscribe((books: IBook[]) => (this.booksSharedCollection = books));

    this.readerService
      .query()
      .pipe(map((res: HttpResponse<IReader[]>) => res.body ?? []))
      .pipe(map((readers: IReader[]) => this.readerService.addReaderToCollectionIfMissing<IReader>(readers, this.loan?.member)))
      .subscribe((readers: IReader[]) => (this.readersSharedCollection = readers));
  }
}
