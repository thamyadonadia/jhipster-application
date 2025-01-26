package com.library.web.rest;

import static com.library.domain.BookAsserts.*;
import static com.library.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.IntegrationTest;
import com.library.domain.Book;
import com.library.domain.enumeration.BookStatusEnum;
import com.library.repository.BookRepository;
import com.library.repository.EntityManager;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

/**
 * Integration tests for the {@link BookResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class BookResourceIT {

    private static final String DEFAULT_TITLE = "AAAAAAAAAA";
    private static final String UPDATED_TITLE = "BBBBBBBBBB";

    private static final LocalDate DEFAULT_PUBLICATION_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_PUBLICATION_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final Integer DEFAULT_COPIES_OWNED = 0;
    private static final Integer UPDATED_COPIES_OWNED = 1;

    private static final BookStatusEnum DEFAULT_STATUS = BookStatusEnum.AVAILABLE;
    private static final BookStatusEnum UPDATED_STATUS = BookStatusEnum.BORROWED;

    private static final String ENTITY_API_URL = "/api/books";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private BookRepository bookRepository;

    @Mock
    private BookRepository bookRepositoryMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private Book book;

    private Book insertedBook;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Book createEntity() {
        return new Book()
            .title(DEFAULT_TITLE)
            .publicationDate(DEFAULT_PUBLICATION_DATE)
            .copiesOwned(DEFAULT_COPIES_OWNED)
            .status(DEFAULT_STATUS);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Book createUpdatedEntity() {
        return new Book()
            .title(UPDATED_TITLE)
            .publicationDate(UPDATED_PUBLICATION_DATE)
            .copiesOwned(UPDATED_COPIES_OWNED)
            .status(UPDATED_STATUS);
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll("rel_book__author").block();
            em.deleteAll(Book.class).block();
        } catch (Exception e) {
            // It can fail, if other entities are still referring this - it will be removed later.
        }
    }

    @BeforeEach
    public void initTest() {
        book = createEntity();
    }

    @AfterEach
    public void cleanup() {
        if (insertedBook != null) {
            bookRepository.delete(insertedBook).block();
            insertedBook = null;
        }
        deleteEntities(em);
    }

    @Test
    void createBook() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Book
        var returnedBook = webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(book))
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(Book.class)
            .returnResult()
            .getResponseBody();

        // Validate the Book in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertBookUpdatableFieldsEquals(returnedBook, getPersistedBook(returnedBook));

        insertedBook = returnedBook;
    }

    @Test
    void createBookWithExistingId() throws Exception {
        // Create the Book with an existing ID
        book.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(book))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Book in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void checkTitleIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        book.setTitle(null);

        // Create the Book, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(book))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkCopiesOwnedIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        book.setCopiesOwned(null);

        // Create the Book, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(book))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkStatusIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        book.setStatus(null);

        // Create the Book, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(book))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void getAllBooksAsStream() {
        // Initialize the database
        bookRepository.save(book).block();

        List<Book> bookList = webTestClient
            .get()
            .uri(ENTITY_API_URL)
            .accept(MediaType.APPLICATION_NDJSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentTypeCompatibleWith(MediaType.APPLICATION_NDJSON)
            .returnResult(Book.class)
            .getResponseBody()
            .filter(book::equals)
            .collectList()
            .block(Duration.ofSeconds(5));

        assertThat(bookList).isNotNull();
        assertThat(bookList).hasSize(1);
        Book testBook = bookList.get(0);

        // Test fails because reactive api returns an empty object instead of null
        // assertBookAllPropertiesEquals(book, testBook);
        assertBookUpdatableFieldsEquals(book, testBook);
    }

    @Test
    void getAllBooks() {
        // Initialize the database
        insertedBook = bookRepository.save(book).block();

        // Get all the bookList
        webTestClient
            .get()
            .uri(ENTITY_API_URL + "?sort=id,desc")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(book.getId().intValue()))
            .jsonPath("$.[*].title")
            .value(hasItem(DEFAULT_TITLE))
            .jsonPath("$.[*].publicationDate")
            .value(hasItem(DEFAULT_PUBLICATION_DATE.toString()))
            .jsonPath("$.[*].copiesOwned")
            .value(hasItem(DEFAULT_COPIES_OWNED))
            .jsonPath("$.[*].status")
            .value(hasItem(DEFAULT_STATUS.toString()));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllBooksWithEagerRelationshipsIsEnabled() {
        when(bookRepositoryMock.findAllWithEagerRelationships(any())).thenReturn(Flux.empty());

        webTestClient.get().uri(ENTITY_API_URL + "?eagerload=true").exchange().expectStatus().isOk();

        verify(bookRepositoryMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllBooksWithEagerRelationshipsIsNotEnabled() {
        when(bookRepositoryMock.findAllWithEagerRelationships(any())).thenReturn(Flux.empty());

        webTestClient.get().uri(ENTITY_API_URL + "?eagerload=false").exchange().expectStatus().isOk();
        verify(bookRepositoryMock, times(1)).findAllWithEagerRelationships(any());
    }

    @Test
    void getBook() {
        // Initialize the database
        insertedBook = bookRepository.save(book).block();

        // Get the book
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, book.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(book.getId().intValue()))
            .jsonPath("$.title")
            .value(is(DEFAULT_TITLE))
            .jsonPath("$.publicationDate")
            .value(is(DEFAULT_PUBLICATION_DATE.toString()))
            .jsonPath("$.copiesOwned")
            .value(is(DEFAULT_COPIES_OWNED))
            .jsonPath("$.status")
            .value(is(DEFAULT_STATUS.toString()));
    }

    @Test
    void getNonExistingBook() {
        // Get the book
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_PROBLEM_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingBook() throws Exception {
        // Initialize the database
        insertedBook = bookRepository.save(book).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the book
        Book updatedBook = bookRepository.findById(book.getId()).block();
        updatedBook.title(UPDATED_TITLE).publicationDate(UPDATED_PUBLICATION_DATE).copiesOwned(UPDATED_COPIES_OWNED).status(UPDATED_STATUS);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, updatedBook.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(updatedBook))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Book in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedBookToMatchAllProperties(updatedBook);
    }

    @Test
    void putNonExistingBook() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        book.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, book.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(book))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Book in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchBook() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        book.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(book))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Book in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamBook() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        book.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(book))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Book in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateBookWithPatch() throws Exception {
        // Initialize the database
        insertedBook = bookRepository.save(book).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the book using partial update
        Book partialUpdatedBook = new Book();
        partialUpdatedBook.setId(book.getId());

        partialUpdatedBook.publicationDate(UPDATED_PUBLICATION_DATE).status(UPDATED_STATUS);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedBook.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedBook))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Book in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertBookUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedBook, book), getPersistedBook(book));
    }

    @Test
    void fullUpdateBookWithPatch() throws Exception {
        // Initialize the database
        insertedBook = bookRepository.save(book).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the book using partial update
        Book partialUpdatedBook = new Book();
        partialUpdatedBook.setId(book.getId());

        partialUpdatedBook
            .title(UPDATED_TITLE)
            .publicationDate(UPDATED_PUBLICATION_DATE)
            .copiesOwned(UPDATED_COPIES_OWNED)
            .status(UPDATED_STATUS);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedBook.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedBook))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Book in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertBookUpdatableFieldsEquals(partialUpdatedBook, getPersistedBook(partialUpdatedBook));
    }

    @Test
    void patchNonExistingBook() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        book.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, book.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(book))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Book in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchBook() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        book.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(book))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Book in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamBook() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        book.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(book))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Book in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteBook() {
        // Initialize the database
        insertedBook = bookRepository.save(book).block();

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the book
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, book.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return bookRepository.count().block();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected Book getPersistedBook(Book book) {
        return bookRepository.findById(book.getId()).block();
    }

    protected void assertPersistedBookToMatchAllProperties(Book expectedBook) {
        // Test fails because reactive api returns an empty object instead of null
        // assertBookAllPropertiesEquals(expectedBook, getPersistedBook(expectedBook));
        assertBookUpdatableFieldsEquals(expectedBook, getPersistedBook(expectedBook));
    }

    protected void assertPersistedBookToMatchUpdatableProperties(Book expectedBook) {
        // Test fails because reactive api returns an empty object instead of null
        // assertBookAllUpdatablePropertiesEquals(expectedBook, getPersistedBook(expectedBook));
        assertBookUpdatableFieldsEquals(expectedBook, getPersistedBook(expectedBook));
    }
}
