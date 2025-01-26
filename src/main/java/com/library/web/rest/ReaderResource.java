package com.library.web.rest;

import com.library.domain.Reader;
import com.library.repository.ReaderRepository;
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
 * REST controller for managing {@link com.library.domain.Reader}.
 */
@RestController
@RequestMapping("/api/readers")
@Transactional
public class ReaderResource {

    private static final Logger LOG = LoggerFactory.getLogger(ReaderResource.class);

    private static final String ENTITY_NAME = "reader";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ReaderRepository readerRepository;

    public ReaderResource(ReaderRepository readerRepository) {
        this.readerRepository = readerRepository;
    }

    /**
     * {@code POST  /readers} : Create a new reader.
     *
     * @param reader the reader to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new reader, or with status {@code 400 (Bad Request)} if the reader has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public Mono<ResponseEntity<Reader>> createReader(@Valid @RequestBody Reader reader) throws URISyntaxException {
        LOG.debug("REST request to save Reader : {}", reader);
        if (reader.getId() != null) {
            throw new BadRequestAlertException("A new reader cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return readerRepository
            .save(reader)
            .map(result -> {
                try {
                    return ResponseEntity.created(new URI("/api/readers/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code PUT  /readers/:id} : Updates an existing reader.
     *
     * @param id the id of the reader to save.
     * @param reader the reader to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated reader,
     * or with status {@code 400 (Bad Request)} if the reader is not valid,
     * or with status {@code 500 (Internal Server Error)} if the reader couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public Mono<ResponseEntity<Reader>> updateReader(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody Reader reader
    ) throws URISyntaxException {
        LOG.debug("REST request to update Reader : {}, {}", id, reader);
        if (reader.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, reader.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return readerRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                return readerRepository
                    .save(reader)
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                    .map(result ->
                        ResponseEntity.ok()
                            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
                            .body(result)
                    );
            });
    }

    /**
     * {@code PATCH  /readers/:id} : Partial updates given fields of an existing reader, field will ignore if it is null
     *
     * @param id the id of the reader to save.
     * @param reader the reader to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated reader,
     * or with status {@code 400 (Bad Request)} if the reader is not valid,
     * or with status {@code 404 (Not Found)} if the reader is not found,
     * or with status {@code 500 (Internal Server Error)} if the reader couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public Mono<ResponseEntity<Reader>> partialUpdateReader(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody Reader reader
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Reader partially : {}, {}", id, reader);
        if (reader.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, reader.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return readerRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                Mono<Reader> result = readerRepository
                    .findById(reader.getId())
                    .map(existingReader -> {
                        if (reader.getFirstName() != null) {
                            existingReader.setFirstName(reader.getFirstName());
                        }
                        if (reader.getLastName() != null) {
                            existingReader.setLastName(reader.getLastName());
                        }
                        if (reader.getEmail() != null) {
                            existingReader.setEmail(reader.getEmail());
                        }
                        if (reader.getJoinedDate() != null) {
                            existingReader.setJoinedDate(reader.getJoinedDate());
                        }

                        return existingReader;
                    })
                    .flatMap(readerRepository::save);

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
     * {@code GET  /readers} : get all the readers.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of readers in body.
     */
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<List<Reader>> getAllReaders() {
        LOG.debug("REST request to get all Readers");
        return readerRepository.findAll().collectList();
    }

    /**
     * {@code GET  /readers} : get all the readers as a stream.
     * @return the {@link Flux} of readers.
     */
    @GetMapping(value = "", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<Reader> getAllReadersAsStream() {
        LOG.debug("REST request to get all Readers as a stream");
        return readerRepository.findAll();
    }

    /**
     * {@code GET  /readers/:id} : get the "id" reader.
     *
     * @param id the id of the reader to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the reader, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<Reader>> getReader(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Reader : {}", id);
        Mono<Reader> reader = readerRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(reader);
    }

    /**
     * {@code DELETE  /readers/:id} : delete the "id" reader.
     *
     * @param id the id of the reader to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteReader(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Reader : {}", id);
        return readerRepository
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
