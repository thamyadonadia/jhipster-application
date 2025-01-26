import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse, provideHttpClient } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Subject, from, of } from 'rxjs';

import { IBook } from 'app/entities/book/book.model';
import { BookService } from 'app/entities/book/service/book.service';
import { AuthorService } from '../service/author.service';
import { IAuthor } from '../author.model';
import { AuthorFormService } from './author-form.service';

import { AuthorUpdateComponent } from './author-update.component';

describe('Author Management Update Component', () => {
  let comp: AuthorUpdateComponent;
  let fixture: ComponentFixture<AuthorUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let authorFormService: AuthorFormService;
  let authorService: AuthorService;
  let bookService: BookService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [AuthorUpdateComponent],
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
      .overrideTemplate(AuthorUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(AuthorUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    authorFormService = TestBed.inject(AuthorFormService);
    authorService = TestBed.inject(AuthorService);
    bookService = TestBed.inject(BookService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('Should call Book query and add missing value', () => {
      const author: IAuthor = { id: 11676 };
      const books: IBook[] = [{ id: 32624 }];
      author.books = books;

      const bookCollection: IBook[] = [{ id: 32624 }];
      jest.spyOn(bookService, 'query').mockReturnValue(of(new HttpResponse({ body: bookCollection })));
      const additionalBooks = [...books];
      const expectedCollection: IBook[] = [...additionalBooks, ...bookCollection];
      jest.spyOn(bookService, 'addBookToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ author });
      comp.ngOnInit();

      expect(bookService.query).toHaveBeenCalled();
      expect(bookService.addBookToCollectionIfMissing).toHaveBeenCalledWith(
        bookCollection,
        ...additionalBooks.map(expect.objectContaining),
      );
      expect(comp.booksSharedCollection).toEqual(expectedCollection);
    });

    it('Should update editForm', () => {
      const author: IAuthor = { id: 11676 };
      const book: IBook = { id: 32624 };
      author.books = [book];

      activatedRoute.data = of({ author });
      comp.ngOnInit();

      expect(comp.booksSharedCollection).toContainEqual(book);
      expect(comp.author).toEqual(author);
    });
  });

  describe('save', () => {
    it('Should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IAuthor>>();
      const author = { id: 32542 };
      jest.spyOn(authorFormService, 'getAuthor').mockReturnValue(author);
      jest.spyOn(authorService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ author });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: author }));
      saveSubject.complete();

      // THEN
      expect(authorFormService.getAuthor).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(authorService.update).toHaveBeenCalledWith(expect.objectContaining(author));
      expect(comp.isSaving).toEqual(false);
    });

    it('Should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IAuthor>>();
      const author = { id: 32542 };
      jest.spyOn(authorFormService, 'getAuthor').mockReturnValue({ id: null });
      jest.spyOn(authorService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ author: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: author }));
      saveSubject.complete();

      // THEN
      expect(authorFormService.getAuthor).toHaveBeenCalled();
      expect(authorService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('Should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IAuthor>>();
      const author = { id: 32542 };
      jest.spyOn(authorService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ author });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(authorService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
    describe('compareBook', () => {
      it('Should forward to bookService', () => {
        const entity = { id: 32624 };
        const entity2 = { id: 17120 };
        jest.spyOn(bookService, 'compareBook');
        comp.compareBook(entity, entity2);
        expect(bookService.compareBook).toHaveBeenCalledWith(entity, entity2);
      });
    });
  });
});
