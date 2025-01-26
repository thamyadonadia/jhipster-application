import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import { IReader, NewReader } from '../reader.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IReader for edit and NewReaderFormGroupInput for create.
 */
type ReaderFormGroupInput = IReader | PartialWithRequiredKeyOf<NewReader>;

type ReaderFormDefaults = Pick<NewReader, 'id'>;

type ReaderFormGroupContent = {
  id: FormControl<IReader['id'] | NewReader['id']>;
  firstName: FormControl<IReader['firstName']>;
  lastName: FormControl<IReader['lastName']>;
  email: FormControl<IReader['email']>;
  joinedDate: FormControl<IReader['joinedDate']>;
};

export type ReaderFormGroup = FormGroup<ReaderFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class ReaderFormService {
  createReaderFormGroup(reader: ReaderFormGroupInput = { id: null }): ReaderFormGroup {
    const readerRawValue = {
      ...this.getFormDefaults(),
      ...reader,
    };
    return new FormGroup<ReaderFormGroupContent>({
      id: new FormControl(
        { value: readerRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      firstName: new FormControl(readerRawValue.firstName, {
        validators: [Validators.required],
      }),
      lastName: new FormControl(readerRawValue.lastName, {
        validators: [Validators.required],
      }),
      email: new FormControl(readerRawValue.email, {
        validators: [Validators.required],
      }),
      joinedDate: new FormControl(readerRawValue.joinedDate),
    });
  }

  getReader(form: ReaderFormGroup): IReader | NewReader {
    return form.getRawValue() as IReader | NewReader;
  }

  resetForm(form: ReaderFormGroup, reader: ReaderFormGroupInput): void {
    const readerRawValue = { ...this.getFormDefaults(), ...reader };
    form.reset(
      {
        ...readerRawValue,
        id: { value: readerRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): ReaderFormDefaults {
    return {
      id: null,
    };
  }
}
