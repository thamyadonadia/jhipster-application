import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { ILoan } from '../loan.model';
import { LoanService } from '../service/loan.service';

@Component({
  templateUrl: './loan-delete-dialog.component.html',
  imports: [SharedModule, FormsModule],
})
export class LoanDeleteDialogComponent {
  loan?: ILoan;

  protected loanService = inject(LoanService);
  protected activeModal = inject(NgbActiveModal);

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.loanService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
