import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { IReader } from '../reader.model';
import { ReaderService } from '../service/reader.service';

@Component({
  templateUrl: './reader-delete-dialog.component.html',
  imports: [SharedModule, FormsModule],
})
export class ReaderDeleteDialogComponent {
  reader?: IReader;

  protected readerService = inject(ReaderService);
  protected activeModal = inject(NgbActiveModal);

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.readerService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
