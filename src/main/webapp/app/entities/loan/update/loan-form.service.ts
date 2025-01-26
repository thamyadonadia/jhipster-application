import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import { ILoan, NewLoan } from '../loan.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts ILoan for edit and NewLoanFormGroupInput for create.
 */
type LoanFormGroupInput = ILoan | PartialWithRequiredKeyOf<NewLoan>;

type LoanFormDefaults = Pick<NewLoan, 'id'>;

type LoanFormGroupContent = {
  id: FormControl<ILoan['id'] | NewLoan['id']>;
  loanDate: FormControl<ILoan['loanDate']>;
  returnDate: FormControl<ILoan['returnDate']>;
  book: FormControl<ILoan['book']>;
  member: FormControl<ILoan['member']>;
};

export type LoanFormGroup = FormGroup<LoanFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class LoanFormService {
  createLoanFormGroup(loan: LoanFormGroupInput = { id: null }): LoanFormGroup {
    const loanRawValue = {
      ...this.getFormDefaults(),
      ...loan,
    };
    return new FormGroup<LoanFormGroupContent>({
      id: new FormControl(
        { value: loanRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      loanDate: new FormControl(loanRawValue.loanDate, {
        validators: [Validators.required],
      }),
      returnDate: new FormControl(loanRawValue.returnDate),
      book: new FormControl(loanRawValue.book),
      member: new FormControl(loanRawValue.member),
    });
  }

  getLoan(form: LoanFormGroup): ILoan | NewLoan {
    return form.getRawValue() as ILoan | NewLoan;
  }

  resetForm(form: LoanFormGroup, loan: LoanFormGroupInput): void {
    const loanRawValue = { ...this.getFormDefaults(), ...loan };
    form.reset(
      {
        ...loanRawValue,
        id: { value: loanRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): LoanFormDefaults {
    return {
      id: null,
    };
  }
}
