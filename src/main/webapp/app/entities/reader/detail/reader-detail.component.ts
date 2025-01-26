import { Component, input } from '@angular/core';
import { RouterModule } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { FormatMediumDatePipe } from 'app/shared/date';
import { IReader } from '../reader.model';

@Component({
  selector: 'jhi-reader-detail',
  templateUrl: './reader-detail.component.html',
  imports: [SharedModule, RouterModule, FormatMediumDatePipe],
})
export class ReaderDetailComponent {
  reader = input<IReader | null>(null);

  previousState(): void {
    window.history.back();
  }
}
