import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../reader.test-samples';

import { ReaderFormService } from './reader-form.service';

describe('Reader Form Service', () => {
  let service: ReaderFormService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ReaderFormService);
  });

  describe('Service methods', () => {
    describe('createReaderFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createReaderFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            firstName: expect.any(Object),
            lastName: expect.any(Object),
            email: expect.any(Object),
            joinedDate: expect.any(Object),
          }),
        );
      });

      it('passing IReader should create a new form with FormGroup', () => {
        const formGroup = service.createReaderFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            firstName: expect.any(Object),
            lastName: expect.any(Object),
            email: expect.any(Object),
            joinedDate: expect.any(Object),
          }),
        );
      });
    });

    describe('getReader', () => {
      it('should return NewReader for default Reader initial value', () => {
        const formGroup = service.createReaderFormGroup(sampleWithNewData);

        const reader = service.getReader(formGroup) as any;

        expect(reader).toMatchObject(sampleWithNewData);
      });

      it('should return NewReader for empty Reader initial value', () => {
        const formGroup = service.createReaderFormGroup();

        const reader = service.getReader(formGroup) as any;

        expect(reader).toMatchObject({});
      });

      it('should return IReader', () => {
        const formGroup = service.createReaderFormGroup(sampleWithRequiredData);

        const reader = service.getReader(formGroup) as any;

        expect(reader).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IReader should not enable id FormControl', () => {
        const formGroup = service.createReaderFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewReader should disable id FormControl', () => {
        const formGroup = service.createReaderFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
