import { Component, input } from '@angular/core';
import { RouterModule } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { FormatMediumDatePipe } from 'app/shared/date';
import { ILoan } from '../loan.model';

@Component({
  selector: 'jhi-loan-detail',
  templateUrl: './loan-detail.component.html',
  imports: [SharedModule, RouterModule, FormatMediumDatePipe],
})
export class LoanDetailComponent {
  loan = input<ILoan | null>(null);

  previousState(): void {
    window.history.back();
  }
}
