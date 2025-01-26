import { Component, OnInit, inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { ICategory } from 'app/entities/category/category.model';
import { CategoryService } from 'app/entities/category/service/category.service';
import { IAuthor } from 'app/entities/author/author.model';
import { AuthorService } from 'app/entities/author/service/author.service';
import { BookStatusEnum } from 'app/entities/enumerations/book-status-enum.model';
import { BookService } from '../service/book.service';
import { IBook } from '../book.model';
import { BookFormGroup, BookFormService } from './book-form.service';

@Component({
  selector: 'jhi-book-update',
  templateUrl: './book-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class BookUpdateComponent implements OnInit {
  isSaving = false;
  book: IBook | null = null;
  bookStatusEnumValues = Object.keys(BookStatusEnum);

  categoriesSharedCollection: ICategory[] = [];
  authorsSharedCollection: IAuthor[] = [];

  protected bookService = inject(BookService);
  protected bookFormService = inject(BookFormService);
  protected categoryService = inject(CategoryService);
  protected authorService = inject(AuthorService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: BookFormGroup = this.bookFormService.createBookFormGroup();

  compareCategory = (o1: ICategory | null, o2: ICategory | null): boolean => this.categoryService.compareCategory(o1, o2);

  compareAuthor = (o1: IAuthor | null, o2: IAuthor | null): boolean => this.authorService.compareAuthor(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ book }) => {
      this.book = book;
      if (book) {
        this.updateForm(book);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const book = this.bookFormService.getBook(this.editForm);
    if (book.id !== null) {
      this.subscribeToSaveResponse(this.bookService.update(book));
    } else {
      this.subscribeToSaveResponse(this.bookService.create(book));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IBook>>): void {
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

  protected updateForm(book: IBook): void {
    this.book = book;
    this.bookFormService.resetForm(this.editForm, book);

    this.categoriesSharedCollection = this.categoryService.addCategoryToCollectionIfMissing<ICategory>(
      this.categoriesSharedCollection,
      book.category,
    );
    this.authorsSharedCollection = this.authorService.addAuthorToCollectionIfMissing<IAuthor>(
      this.authorsSharedCollection,
      ...(book.authors ?? []),
    );
  }

  protected loadRelationshipsOptions(): void {
    this.categoryService
      .query()
      .pipe(map((res: HttpResponse<ICategory[]>) => res.body ?? []))
      .pipe(
        map((categories: ICategory[]) => this.categoryService.addCategoryToCollectionIfMissing<ICategory>(categories, this.book?.category)),
      )
      .subscribe((categories: ICategory[]) => (this.categoriesSharedCollection = categories));

    this.authorService
      .query()
      .pipe(map((res: HttpResponse<IAuthor[]>) => res.body ?? []))
      .pipe(map((authors: IAuthor[]) => this.authorService.addAuthorToCollectionIfMissing<IAuthor>(authors, ...(this.book?.authors ?? []))))
      .subscribe((authors: IAuthor[]) => (this.authorsSharedCollection = authors));
  }
}
