package com.library.repository;

import com.library.domain.Author;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for the Author entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AuthorRepository extends ReactiveCrudRepository<Author, Long>, AuthorRepositoryInternal {
    @Override
    <S extends Author> Mono<S> save(S entity);

    @Override
    Flux<Author> findAll();

    @Override
    Mono<Author> findById(Long id);

    @Override
    Mono<Void> deleteById(Long id);
}

interface AuthorRepositoryInternal {
    <S extends Author> Mono<S> save(S entity);

    Flux<Author> findAllBy(Pageable pageable);

    Flux<Author> findAll();

    Mono<Author> findById(Long id);
    // this is not supported at the moment because of https://github.com/jhipster/generator-jhipster/issues/18269
    // Flux<Author> findAllBy(Pageable pageable, Criteria criteria);
}
