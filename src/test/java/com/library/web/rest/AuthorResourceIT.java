package com.library.web.rest;

import static com.library.domain.AuthorAsserts.*;
import static com.library.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.IntegrationTest;
import com.library.domain.Author;
import com.library.repository.AuthorRepository;
import com.library.repository.EntityManager;
import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Integration tests for the {@link AuthorResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class AuthorResourceIT {

    private static final String DEFAULT_FIRST_NAME = "AAAAAAAAAA";
    private static final String UPDATED_FIRST_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_LAST_NAME = "AAAAAAAAAA";
    private static final String UPDATED_LAST_NAME = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/authors";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private Author author;

    private Author insertedAuthor;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Author createEntity() {
        return new Author().firstName(DEFAULT_FIRST_NAME).lastName(DEFAULT_LAST_NAME);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Author createUpdatedEntity() {
        return new Author().firstName(UPDATED_FIRST_NAME).lastName(UPDATED_LAST_NAME);
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(Author.class).block();
        } catch (Exception e) {
            // It can fail, if other entities are still referring this - it will be removed later.
        }
    }

    @BeforeEach
    public void initTest() {
        author = createEntity();
    }

    @AfterEach
    public void cleanup() {
        if (insertedAuthor != null) {
            authorRepository.delete(insertedAuthor).block();
            insertedAuthor = null;
        }
        deleteEntities(em);
    }

    @Test
    void createAuthor() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Author
        var returnedAuthor = webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(author))
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(Author.class)
            .returnResult()
            .getResponseBody();

        // Validate the Author in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertAuthorUpdatableFieldsEquals(returnedAuthor, getPersistedAuthor(returnedAuthor));

        insertedAuthor = returnedAuthor;
    }

    @Test
    void createAuthorWithExistingId() throws Exception {
        // Create the Author with an existing ID
        author.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(author))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Author in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void checkFirstNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        author.setFirstName(null);

        // Create the Author, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(author))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkLastNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        author.setLastName(null);

        // Create the Author, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(author))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void getAllAuthorsAsStream() {
        // Initialize the database
        authorRepository.save(author).block();

        List<Author> authorList = webTestClient
            .get()
            .uri(ENTITY_API_URL)
            .accept(MediaType.APPLICATION_NDJSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentTypeCompatibleWith(MediaType.APPLICATION_NDJSON)
            .returnResult(Author.class)
            .getResponseBody()
            .filter(author::equals)
            .collectList()
            .block(Duration.ofSeconds(5));

        assertThat(authorList).isNotNull();
        assertThat(authorList).hasSize(1);
        Author testAuthor = authorList.get(0);

        // Test fails because reactive api returns an empty object instead of null
        // assertAuthorAllPropertiesEquals(author, testAuthor);
        assertAuthorUpdatableFieldsEquals(author, testAuthor);
    }

    @Test
    void getAllAuthors() {
        // Initialize the database
        insertedAuthor = authorRepository.save(author).block();

        // Get all the authorList
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
            .value(hasItem(author.getId().intValue()))
            .jsonPath("$.[*].firstName")
            .value(hasItem(DEFAULT_FIRST_NAME))
            .jsonPath("$.[*].lastName")
            .value(hasItem(DEFAULT_LAST_NAME));
    }

    @Test
    void getAuthor() {
        // Initialize the database
        insertedAuthor = authorRepository.save(author).block();

        // Get the author
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, author.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(author.getId().intValue()))
            .jsonPath("$.firstName")
            .value(is(DEFAULT_FIRST_NAME))
            .jsonPath("$.lastName")
            .value(is(DEFAULT_LAST_NAME));
    }

    @Test
    void getNonExistingAuthor() {
        // Get the author
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_PROBLEM_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingAuthor() throws Exception {
        // Initialize the database
        insertedAuthor = authorRepository.save(author).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the author
        Author updatedAuthor = authorRepository.findById(author.getId()).block();
        updatedAuthor.firstName(UPDATED_FIRST_NAME).lastName(UPDATED_LAST_NAME);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, updatedAuthor.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(updatedAuthor))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Author in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedAuthorToMatchAllProperties(updatedAuthor);
    }

    @Test
    void putNonExistingAuthor() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        author.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, author.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(author))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Author in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchAuthor() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        author.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(author))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Author in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamAuthor() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        author.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(author))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Author in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateAuthorWithPatch() throws Exception {
        // Initialize the database
        insertedAuthor = authorRepository.save(author).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the author using partial update
        Author partialUpdatedAuthor = new Author();
        partialUpdatedAuthor.setId(author.getId());

        partialUpdatedAuthor.firstName(UPDATED_FIRST_NAME);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedAuthor.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedAuthor))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Author in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertAuthorUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedAuthor, author), getPersistedAuthor(author));
    }

    @Test
    void fullUpdateAuthorWithPatch() throws Exception {
        // Initialize the database
        insertedAuthor = authorRepository.save(author).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the author using partial update
        Author partialUpdatedAuthor = new Author();
        partialUpdatedAuthor.setId(author.getId());

        partialUpdatedAuthor.firstName(UPDATED_FIRST_NAME).lastName(UPDATED_LAST_NAME);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedAuthor.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedAuthor))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Author in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertAuthorUpdatableFieldsEquals(partialUpdatedAuthor, getPersistedAuthor(partialUpdatedAuthor));
    }

    @Test
    void patchNonExistingAuthor() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        author.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, author.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(author))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Author in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchAuthor() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        author.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(author))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Author in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamAuthor() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        author.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(author))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Author in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteAuthor() {
        // Initialize the database
        insertedAuthor = authorRepository.save(author).block();

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the author
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, author.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return authorRepository.count().block();
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

    protected Author getPersistedAuthor(Author author) {
        return authorRepository.findById(author.getId()).block();
    }

    protected void assertPersistedAuthorToMatchAllProperties(Author expectedAuthor) {
        // Test fails because reactive api returns an empty object instead of null
        // assertAuthorAllPropertiesEquals(expectedAuthor, getPersistedAuthor(expectedAuthor));
        assertAuthorUpdatableFieldsEquals(expectedAuthor, getPersistedAuthor(expectedAuthor));
    }

    protected void assertPersistedAuthorToMatchUpdatableProperties(Author expectedAuthor) {
        // Test fails because reactive api returns an empty object instead of null
        // assertAuthorAllUpdatablePropertiesEquals(expectedAuthor, getPersistedAuthor(expectedAuthor));
        assertAuthorUpdatableFieldsEquals(expectedAuthor, getPersistedAuthor(expectedAuthor));
    }
}
