import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { of } from 'rxjs';

import { LoanDetailComponent } from './loan-detail.component';

describe('Loan Management Detail Component', () => {
  let comp: LoanDetailComponent;
  let fixture: ComponentFixture<LoanDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LoanDetailComponent],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              loadComponent: () => import('./loan-detail.component').then(m => m.LoanDetailComponent),
              resolve: { loan: () => of({ id: 1685 }) },
            },
          ],
          withComponentInputBinding(),
        ),
      ],
    })
      .overrideTemplate(LoanDetailComponent, '')
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(LoanDetailComponent);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('Should load loan on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', LoanDetailComponent);

      // THEN
      expect(instance.loan()).toEqual(expect.objectContaining({ id: 1685 }));
    });
  });

  describe('PreviousState', () => {
    it('Should navigate to previous state', () => {
      jest.spyOn(window.history, 'back');
      comp.previousState();
      expect(window.history.back).toHaveBeenCalled();
    });
  });
});
