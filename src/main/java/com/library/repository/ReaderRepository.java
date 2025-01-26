package com.library.repository;

import com.library.domain.Reader;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for the Reader entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ReaderRepository extends ReactiveCrudRepository<Reader, Long>, ReaderRepositoryInternal {
    @Override
    <S extends Reader> Mono<S> save(S entity);

    @Override
    Flux<Reader> findAll();

    @Override
    Mono<Reader> findById(Long id);

    @Override
    Mono<Void> deleteById(Long id);
}

interface ReaderRepositoryInternal {
    <S extends Reader> Mono<S> save(S entity);

    Flux<Reader> findAllBy(Pageable pageable);

    Flux<Reader> findAll();

    Mono<Reader> findById(Long id);
    // this is not supported at the moment because of https://github.com/jhipster/generator-jhipster/issues/18269
    // Flux<Reader> findAllBy(Pageable pageable, Criteria criteria);
}
