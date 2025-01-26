import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse, provideHttpClient } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Subject, from, of } from 'rxjs';

import { ReaderService } from '../service/reader.service';
import { IReader } from '../reader.model';
import { ReaderFormService } from './reader-form.service';

import { ReaderUpdateComponent } from './reader-update.component';

describe('Reader Management Update Component', () => {
  let comp: ReaderUpdateComponent;
  let fixture: ComponentFixture<ReaderUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let readerFormService: ReaderFormService;
  let readerService: ReaderService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ReaderUpdateComponent],
      providers: [
        provideHttpClient(),
        FormBuilder,
        {
          provide: ActivatedRoute,
          useValue: {
            params: from([{}]),
          },
        },
      ],
    })
      .overrideTemplate(ReaderUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(ReaderUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    readerFormService = TestBed.inject(ReaderFormService);
    readerService = TestBed.inject(ReaderService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('Should update editForm', () => {
      const reader: IReader = { id: 18215 };

      activatedRoute.data = of({ reader });
      comp.ngOnInit();

      expect(comp.reader).toEqual(reader);
    });
  });

  describe('save', () => {
    it('Should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IReader>>();
      const reader = { id: 4627 };
      jest.spyOn(readerFormService, 'getReader').mockReturnValue(reader);
      jest.spyOn(readerService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ reader });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: reader }));
      saveSubject.complete();

      // THEN
      expect(readerFormService.getReader).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(readerService.update).toHaveBeenCalledWith(expect.objectContaining(reader));
      expect(comp.isSaving).toEqual(false);
    });

    it('Should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IReader>>();
      const reader = { id: 4627 };
      jest.spyOn(readerFormService, 'getReader').mockReturnValue({ id: null });
      jest.spyOn(readerService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ reader: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: reader }));
      saveSubject.complete();

      // THEN
      expect(readerFormService.getReader).toHaveBeenCalled();
      expect(readerService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('Should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IReader>>();
      const reader = { id: 4627 };
      jest.spyOn(readerService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ reader });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(readerService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });
});
