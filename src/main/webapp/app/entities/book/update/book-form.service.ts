import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import { IBook, NewBook } from '../book.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IBook for edit and NewBookFormGroupInput for create.
 */
type BookFormGroupInput = IBook | PartialWithRequiredKeyOf<NewBook>;

type BookFormDefaults = Pick<NewBook, 'id' | 'authors'>;

type BookFormGroupContent = {
  id: FormControl<IBook['id'] | NewBook['id']>;
  title: FormControl<IBook['title']>;
  publicationDate: FormControl<IBook['publicationDate']>;
  copiesOwned: FormControl<IBook['copiesOwned']>;
  status: FormControl<IBook['status']>;
  category: FormControl<IBook['category']>;
  authors: FormControl<IBook['authors']>;
};

export type BookFormGroup = FormGroup<BookFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class BookFormService {
  createBookFormGroup(book: BookFormGroupInput = { id: null }): BookFormGroup {
    const bookRawValue = {
      ...this.getFormDefaults(),
      ...book,
    };
    return new FormGroup<BookFormGroupContent>({
      id: new FormControl(
        { value: bookRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      title: new FormControl(bookRawValue.title, {
        validators: [Validators.required],
      }),
      publicationDate: new FormControl(bookRawValue.publicationDate),
      copiesOwned: new FormControl(bookRawValue.copiesOwned, {
        validators: [Validators.required, Validators.min(0)],
      }),
      status: new FormControl(bookRawValue.status, {
        validators: [Validators.required],
      }),
      category: new FormControl(bookRawValue.category),
      authors: new FormControl(bookRawValue.authors ?? []),
    });
  }

  getBook(form: BookFormGroup): IBook | NewBook {
    return form.getRawValue() as IBook | NewBook;
  }

  resetForm(form: BookFormGroup, book: BookFormGroupInput): void {
    const bookRawValue = { ...this.getFormDefaults(), ...book };
    form.reset(
      {
        ...bookRawValue,
        id: { value: bookRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): BookFormDefaults {
    return {
      id: null,
      authors: [],
    };
  }
}
