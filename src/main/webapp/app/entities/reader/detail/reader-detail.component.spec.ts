import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { of } from 'rxjs';

import { ReaderDetailComponent } from './reader-detail.component';

describe('Reader Management Detail Component', () => {
  let comp: ReaderDetailComponent;
  let fixture: ComponentFixture<ReaderDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReaderDetailComponent],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              loadComponent: () => import('./reader-detail.component').then(m => m.ReaderDetailComponent),
              resolve: { reader: () => of({ id: 4627 }) },
            },
          ],
          withComponentInputBinding(),
        ),
      ],
    })
      .overrideTemplate(ReaderDetailComponent, '')
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ReaderDetailComponent);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('Should load reader on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', ReaderDetailComponent);

      // THEN
      expect(instance.reader()).toEqual(expect.objectContaining({ id: 4627 }));
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
