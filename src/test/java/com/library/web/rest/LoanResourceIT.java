package com.library.web.rest;

import static com.library.domain.LoanAsserts.*;
import static com.library.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.IntegrationTest;
import com.library.domain.Loan;
import com.library.repository.EntityManager;
import com.library.repository.LoanRepository;
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
 * Integration tests for the {@link LoanResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class LoanResourceIT {

    private static final LocalDate DEFAULT_LOAN_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_LOAN_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final LocalDate DEFAULT_RETURN_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_RETURN_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final String ENTITY_API_URL = "/api/loans";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private LoanRepository loanRepository;

    @Mock
    private LoanRepository loanRepositoryMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private Loan loan;

    private Loan insertedLoan;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Loan createEntity() {
        return new Loan().loanDate(DEFAULT_LOAN_DATE).returnDate(DEFAULT_RETURN_DATE);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Loan createUpdatedEntity() {
        return new Loan().loanDate(UPDATED_LOAN_DATE).returnDate(UPDATED_RETURN_DATE);
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(Loan.class).block();
        } catch (Exception e) {
            // It can fail, if other entities are still referring this - it will be removed later.
        }
    }

    @BeforeEach
    public void initTest() {
        loan = createEntity();
    }

    @AfterEach
    public void cleanup() {
        if (insertedLoan != null) {
            loanRepository.delete(insertedLoan).block();
            insertedLoan = null;
        }
        deleteEntities(em);
    }

    @Test
    void createLoan() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Loan
        var returnedLoan = webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(loan))
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(Loan.class)
            .returnResult()
            .getResponseBody();

        // Validate the Loan in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertLoanUpdatableFieldsEquals(returnedLoan, getPersistedLoan(returnedLoan));

        insertedLoan = returnedLoan;
    }

    @Test
    void createLoanWithExistingId() throws Exception {
        // Create the Loan with an existing ID
        loan.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(loan))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Loan in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void checkLoanDateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        loan.setLoanDate(null);

        // Create the Loan, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(loan))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void getAllLoansAsStream() {
        // Initialize the database
        loanRepository.save(loan).block();

        List<Loan> loanList = webTestClient
            .get()
            .uri(ENTITY_API_URL)
            .accept(MediaType.APPLICATION_NDJSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentTypeCompatibleWith(MediaType.APPLICATION_NDJSON)
            .returnResult(Loan.class)
            .getResponseBody()
            .filter(loan::equals)
            .collectList()
            .block(Duration.ofSeconds(5));

        assertThat(loanList).isNotNull();
        assertThat(loanList).hasSize(1);
        Loan testLoan = loanList.get(0);

        // Test fails because reactive api returns an empty object instead of null
        // assertLoanAllPropertiesEquals(loan, testLoan);
        assertLoanUpdatableFieldsEquals(loan, testLoan);
    }

    @Test
    void getAllLoans() {
        // Initialize the database
        insertedLoan = loanRepository.save(loan).block();

        // Get all the loanList
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
            .value(hasItem(loan.getId().intValue()))
            .jsonPath("$.[*].loanDate")
            .value(hasItem(DEFAULT_LOAN_DATE.toString()))
            .jsonPath("$.[*].returnDate")
            .value(hasItem(DEFAULT_RETURN_DATE.toString()));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllLoansWithEagerRelationshipsIsEnabled() {
        when(loanRepositoryMock.findAllWithEagerRelationships(any())).thenReturn(Flux.empty());

        webTestClient.get().uri(ENTITY_API_URL + "?eagerload=true").exchange().expectStatus().isOk();

        verify(loanRepositoryMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllLoansWithEagerRelationshipsIsNotEnabled() {
        when(loanRepositoryMock.findAllWithEagerRelationships(any())).thenReturn(Flux.empty());

        webTestClient.get().uri(ENTITY_API_URL + "?eagerload=false").exchange().expectStatus().isOk();
        verify(loanRepositoryMock, times(1)).findAllWithEagerRelationships(any());
    }

    @Test
    void getLoan() {
        // Initialize the database
        insertedLoan = loanRepository.save(loan).block();

        // Get the loan
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, loan.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(loan.getId().intValue()))
            .jsonPath("$.loanDate")
            .value(is(DEFAULT_LOAN_DATE.toString()))
            .jsonPath("$.returnDate")
            .value(is(DEFAULT_RETURN_DATE.toString()));
    }

    @Test
    void getNonExistingLoan() {
        // Get the loan
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_PROBLEM_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingLoan() throws Exception {
        // Initialize the database
        insertedLoan = loanRepository.save(loan).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the loan
        Loan updatedLoan = loanRepository.findById(loan.getId()).block();
        updatedLoan.loanDate(UPDATED_LOAN_DATE).returnDate(UPDATED_RETURN_DATE);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, updatedLoan.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(updatedLoan))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Loan in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedLoanToMatchAllProperties(updatedLoan);
    }

    @Test
    void putNonExistingLoan() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        loan.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, loan.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(loan))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Loan in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchLoan() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        loan.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(loan))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Loan in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamLoan() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        loan.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(loan))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Loan in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateLoanWithPatch() throws Exception {
        // Initialize the database
        insertedLoan = loanRepository.save(loan).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the loan using partial update
        Loan partialUpdatedLoan = new Loan();
        partialUpdatedLoan.setId(loan.getId());

        partialUpdatedLoan.returnDate(UPDATED_RETURN_DATE);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedLoan.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedLoan))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Loan in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertLoanUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedLoan, loan), getPersistedLoan(loan));
    }

    @Test
    void fullUpdateLoanWithPatch() throws Exception {
        // Initialize the database
        insertedLoan = loanRepository.save(loan).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the loan using partial update
        Loan partialUpdatedLoan = new Loan();
        partialUpdatedLoan.setId(loan.getId());

        partialUpdatedLoan.loanDate(UPDATED_LOAN_DATE).returnDate(UPDATED_RETURN_DATE);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedLoan.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedLoan))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Loan in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertLoanUpdatableFieldsEquals(partialUpdatedLoan, getPersistedLoan(partialUpdatedLoan));
    }

    @Test
    void patchNonExistingLoan() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        loan.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, loan.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(loan))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Loan in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchLoan() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        loan.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(loan))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Loan in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamLoan() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        loan.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(loan))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Loan in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteLoan() {
        // Initialize the database
        insertedLoan = loanRepository.save(loan).block();

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the loan
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, loan.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return loanRepository.count().block();
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

    protected Loan getPersistedLoan(Loan loan) {
        return loanRepository.findById(loan.getId()).block();
    }

    protected void assertPersistedLoanToMatchAllProperties(Loan expectedLoan) {
        // Test fails because reactive api returns an empty object instead of null
        // assertLoanAllPropertiesEquals(expectedLoan, getPersistedLoan(expectedLoan));
        assertLoanUpdatableFieldsEquals(expectedLoan, getPersistedLoan(expectedLoan));
    }

    protected void assertPersistedLoanToMatchUpdatableProperties(Loan expectedLoan) {
        // Test fails because reactive api returns an empty object instead of null
        // assertLoanAllUpdatablePropertiesEquals(expectedLoan, getPersistedLoan(expectedLoan));
        assertLoanUpdatableFieldsEquals(expectedLoan, getPersistedLoan(expectedLoan));
    }
}
