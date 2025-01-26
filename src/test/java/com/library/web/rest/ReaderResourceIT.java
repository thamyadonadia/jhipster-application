package com.library.web.rest;

import static com.library.domain.ReaderAsserts.*;
import static com.library.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.IntegrationTest;
import com.library.domain.Reader;
import com.library.repository.EntityManager;
import com.library.repository.ReaderRepository;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
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
 * Integration tests for the {@link ReaderResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class ReaderResourceIT {

    private static final String DEFAULT_FIRST_NAME = "AAAAAAAAAA";
    private static final String UPDATED_FIRST_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_LAST_NAME = "AAAAAAAAAA";
    private static final String UPDATED_LAST_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_EMAIL = "AAAAAAAAAA";
    private static final String UPDATED_EMAIL = "BBBBBBBBBB";

    private static final LocalDate DEFAULT_JOINED_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_JOINED_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final String ENTITY_API_URL = "/api/readers";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ReaderRepository readerRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private Reader reader;

    private Reader insertedReader;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Reader createEntity() {
        return new Reader().firstName(DEFAULT_FIRST_NAME).lastName(DEFAULT_LAST_NAME).email(DEFAULT_EMAIL).joinedDate(DEFAULT_JOINED_DATE);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Reader createUpdatedEntity() {
        return new Reader().firstName(UPDATED_FIRST_NAME).lastName(UPDATED_LAST_NAME).email(UPDATED_EMAIL).joinedDate(UPDATED_JOINED_DATE);
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(Reader.class).block();
        } catch (Exception e) {
            // It can fail, if other entities are still referring this - it will be removed later.
        }
    }

    @BeforeEach
    public void initTest() {
        reader = createEntity();
    }

    @AfterEach
    public void cleanup() {
        if (insertedReader != null) {
            readerRepository.delete(insertedReader).block();
            insertedReader = null;
        }
        deleteEntities(em);
    }

    @Test
    void createReader() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Reader
        var returnedReader = webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(reader))
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(Reader.class)
            .returnResult()
            .getResponseBody();

        // Validate the Reader in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertReaderUpdatableFieldsEquals(returnedReader, getPersistedReader(returnedReader));

        insertedReader = returnedReader;
    }

    @Test
    void createReaderWithExistingId() throws Exception {
        // Create the Reader with an existing ID
        reader.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(reader))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Reader in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void checkFirstNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        reader.setFirstName(null);

        // Create the Reader, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(reader))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkLastNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        reader.setLastName(null);

        // Create the Reader, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(reader))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkEmailIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        reader.setEmail(null);

        // Create the Reader, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(reader))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void getAllReadersAsStream() {
        // Initialize the database
        readerRepository.save(reader).block();

        List<Reader> readerList = webTestClient
            .get()
            .uri(ENTITY_API_URL)
            .accept(MediaType.APPLICATION_NDJSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentTypeCompatibleWith(MediaType.APPLICATION_NDJSON)
            .returnResult(Reader.class)
            .getResponseBody()
            .filter(reader::equals)
            .collectList()
            .block(Duration.ofSeconds(5));

        assertThat(readerList).isNotNull();
        assertThat(readerList).hasSize(1);
        Reader testReader = readerList.get(0);

        // Test fails because reactive api returns an empty object instead of null
        // assertReaderAllPropertiesEquals(reader, testReader);
        assertReaderUpdatableFieldsEquals(reader, testReader);
    }

    @Test
    void getAllReaders() {
        // Initialize the database
        insertedReader = readerRepository.save(reader).block();

        // Get all the readerList
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
            .value(hasItem(reader.getId().intValue()))
            .jsonPath("$.[*].firstName")
            .value(hasItem(DEFAULT_FIRST_NAME))
            .jsonPath("$.[*].lastName")
            .value(hasItem(DEFAULT_LAST_NAME))
            .jsonPath("$.[*].email")
            .value(hasItem(DEFAULT_EMAIL))
            .jsonPath("$.[*].joinedDate")
            .value(hasItem(DEFAULT_JOINED_DATE.toString()));
    }

    @Test
    void getReader() {
        // Initialize the database
        insertedReader = readerRepository.save(reader).block();

        // Get the reader
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, reader.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(reader.getId().intValue()))
            .jsonPath("$.firstName")
            .value(is(DEFAULT_FIRST_NAME))
            .jsonPath("$.lastName")
            .value(is(DEFAULT_LAST_NAME))
            .jsonPath("$.email")
            .value(is(DEFAULT_EMAIL))
            .jsonPath("$.joinedDate")
            .value(is(DEFAULT_JOINED_DATE.toString()));
    }

    @Test
    void getNonExistingReader() {
        // Get the reader
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_PROBLEM_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingReader() throws Exception {
        // Initialize the database
        insertedReader = readerRepository.save(reader).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the reader
        Reader updatedReader = readerRepository.findById(reader.getId()).block();
        updatedReader.firstName(UPDATED_FIRST_NAME).lastName(UPDATED_LAST_NAME).email(UPDATED_EMAIL).joinedDate(UPDATED_JOINED_DATE);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, updatedReader.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(updatedReader))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Reader in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedReaderToMatchAllProperties(updatedReader);
    }

    @Test
    void putNonExistingReader() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        reader.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, reader.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(reader))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Reader in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchReader() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        reader.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(reader))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Reader in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamReader() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        reader.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(reader))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Reader in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateReaderWithPatch() throws Exception {
        // Initialize the database
        insertedReader = readerRepository.save(reader).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the reader using partial update
        Reader partialUpdatedReader = new Reader();
        partialUpdatedReader.setId(reader.getId());

        partialUpdatedReader.firstName(UPDATED_FIRST_NAME).email(UPDATED_EMAIL);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedReader.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedReader))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Reader in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertReaderUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedReader, reader), getPersistedReader(reader));
    }

    @Test
    void fullUpdateReaderWithPatch() throws Exception {
        // Initialize the database
        insertedReader = readerRepository.save(reader).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the reader using partial update
        Reader partialUpdatedReader = new Reader();
        partialUpdatedReader.setId(reader.getId());

        partialUpdatedReader.firstName(UPDATED_FIRST_NAME).lastName(UPDATED_LAST_NAME).email(UPDATED_EMAIL).joinedDate(UPDATED_JOINED_DATE);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedReader.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedReader))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Reader in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertReaderUpdatableFieldsEquals(partialUpdatedReader, getPersistedReader(partialUpdatedReader));
    }

    @Test
    void patchNonExistingReader() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        reader.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, reader.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(reader))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Reader in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchReader() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        reader.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(reader))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Reader in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamReader() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        reader.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(reader))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Reader in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteReader() {
        // Initialize the database
        insertedReader = readerRepository.save(reader).block();

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the reader
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, reader.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return readerRepository.count().block();
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

    protected Reader getPersistedReader(Reader reader) {
        return readerRepository.findById(reader.getId()).block();
    }

    protected void assertPersistedReaderToMatchAllProperties(Reader expectedReader) {
        // Test fails because reactive api returns an empty object instead of null
        // assertReaderAllPropertiesEquals(expectedReader, getPersistedReader(expectedReader));
        assertReaderUpdatableFieldsEquals(expectedReader, getPersistedReader(expectedReader));
    }

    protected void assertPersistedReaderToMatchUpdatableProperties(Reader expectedReader) {
        // Test fails because reactive api returns an empty object instead of null
        // assertReaderAllUpdatablePropertiesEquals(expectedReader, getPersistedReader(expectedReader));
        assertReaderUpdatableFieldsEquals(expectedReader, getPersistedReader(expectedReader));
    }
}
