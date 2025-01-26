package com.library.web.rest;

import com.library.domain.Loan;
import com.library.repository.LoanRepository;
import com.library.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.reactive.ResponseUtil;

/**
 * REST controller for managing {@link com.library.domain.Loan}.
 */
@RestController
@RequestMapping("/api/loans")
@Transactional
public class LoanResource {

    private static final Logger LOG = LoggerFactory.getLogger(LoanResource.class);

    private static final String ENTITY_NAME = "loan";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final LoanRepository loanRepository;

    public LoanResource(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    /**
     * {@code POST  /loans} : Create a new loan.
     *
     * @param loan the loan to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new loan, or with status {@code 400 (Bad Request)} if the loan has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public Mono<ResponseEntity<Loan>> createLoan(@Valid @RequestBody Loan loan) throws URISyntaxException {
        LOG.debug("REST request to save Loan : {}", loan);
        if (loan.getId() != null) {
            throw new BadRequestAlertException("A new loan cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return loanRepository
            .save(loan)
            .map(result -> {
                try {
                    return ResponseEntity.created(new URI("/api/loans/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code PUT  /loans/:id} : Updates an existing loan.
     *
     * @param id the id of the loan to save.
     * @param loan the loan to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated loan,
     * or with status {@code 400 (Bad Request)} if the loan is not valid,
     * or with status {@code 500 (Internal Server Error)} if the loan couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public Mono<ResponseEntity<Loan>> updateLoan(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody Loan loan
    ) throws URISyntaxException {
        LOG.debug("REST request to update Loan : {}, {}", id, loan);
        if (loan.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, loan.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return loanRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                return loanRepository
                    .save(loan)
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                    .map(result ->
                        ResponseEntity.ok()
                            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
                            .body(result)
                    );
            });
    }

    /**
     * {@code PATCH  /loans/:id} : Partial updates given fields of an existing loan, field will ignore if it is null
     *
     * @param id the id of the loan to save.
     * @param loan the loan to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated loan,
     * or with status {@code 400 (Bad Request)} if the loan is not valid,
     * or with status {@code 404 (Not Found)} if the loan is not found,
     * or with status {@code 500 (Internal Server Error)} if the loan couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public Mono<ResponseEntity<Loan>> partialUpdateLoan(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody Loan loan
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Loan partially : {}, {}", id, loan);
        if (loan.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, loan.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return loanRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                Mono<Loan> result = loanRepository
                    .findById(loan.getId())
                    .map(existingLoan -> {
                        if (loan.getLoanDate() != null) {
                            existingLoan.setLoanDate(loan.getLoanDate());
                        }
                        if (loan.getReturnDate() != null) {
                            existingLoan.setReturnDate(loan.getReturnDate());
                        }

                        return existingLoan;
                    })
                    .flatMap(loanRepository::save);

                return result
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                    .map(res ->
                        ResponseEntity.ok()
                            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, res.getId().toString()))
                            .body(res)
                    );
            });
    }

    /**
     * {@code GET  /loans} : get all the loans.
     *
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of loans in body.
     */
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<List<Loan>> getAllLoans(@RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload) {
        LOG.debug("REST request to get all Loans");
        if (eagerload) {
            return loanRepository.findAllWithEagerRelationships().collectList();
        } else {
            return loanRepository.findAll().collectList();
        }
    }

    /**
     * {@code GET  /loans} : get all the loans as a stream.
     * @return the {@link Flux} of loans.
     */
    @GetMapping(value = "", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<Loan> getAllLoansAsStream() {
        LOG.debug("REST request to get all Loans as a stream");
        return loanRepository.findAll();
    }

    /**
     * {@code GET  /loans/:id} : get the "id" loan.
     *
     * @param id the id of the loan to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the loan, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<Loan>> getLoan(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Loan : {}", id);
        Mono<Loan> loan = loanRepository.findOneWithEagerRelationships(id);
        return ResponseUtil.wrapOrNotFound(loan);
    }

    /**
     * {@code DELETE  /loans/:id} : delete the "id" loan.
     *
     * @param id the id of the loan to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteLoan(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Loan : {}", id);
        return loanRepository
            .deleteById(id)
            .then(
                Mono.just(
                    ResponseEntity.noContent()
                        .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
                        .build()
                )
            );
    }
}
