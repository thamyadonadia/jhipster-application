import { Component, OnInit, inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IReader } from '../reader.model';
import { ReaderService } from '../service/reader.service';
import { ReaderFormGroup, ReaderFormService } from './reader-form.service';

@Component({
  selector: 'jhi-reader-update',
  templateUrl: './reader-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class ReaderUpdateComponent implements OnInit {
  isSaving = false;
  reader: IReader | null = null;

  protected readerService = inject(ReaderService);
  protected readerFormService = inject(ReaderFormService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: ReaderFormGroup = this.readerFormService.createReaderFormGroup();

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ reader }) => {
      this.reader = reader;
      if (reader) {
        this.updateForm(reader);
      }
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const reader = this.readerFormService.getReader(this.editForm);
    if (reader.id !== null) {
      this.subscribeToSaveResponse(this.readerService.update(reader));
    } else {
      this.subscribeToSaveResponse(this.readerService.create(reader));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IReader>>): void {
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

  protected updateForm(reader: IReader): void {
    this.reader = reader;
    this.readerFormService.resetForm(this.editForm, reader);
  }
}
