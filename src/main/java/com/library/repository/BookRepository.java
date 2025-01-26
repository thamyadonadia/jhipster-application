package com.library.repository;

import com.library.domain.Book;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for the Book entity.
 */
@SuppressWarnings("unused")
@Repository
public interface BookRepository extends ReactiveCrudRepository<Book, Long>, BookRepositoryInternal {
    @Override
    Mono<Book> findOneWithEagerRelationships(Long id);

    @Override
    Flux<Book> findAllWithEagerRelationships();

    @Override
    Flux<Book> findAllWithEagerRelationships(Pageable page);

    @Query("SELECT * FROM book entity WHERE entity.category_id = :id")
    Flux<Book> findByCategory(Long id);

    @Query("SELECT * FROM book entity WHERE entity.category_id IS NULL")
    Flux<Book> findAllWhereCategoryIsNull();

    @Query(
        "SELECT entity.* FROM book entity JOIN rel_book__author joinTable ON entity.id = joinTable.author_id WHERE joinTable.author_id = :id"
    )
    Flux<Book> findByAuthor(Long id);

    @Override
    <S extends Book> Mono<S> save(S entity);

    @Override
    Flux<Book> findAll();

    @Override
    Mono<Book> findById(Long id);

    @Override
    Mono<Void> deleteById(Long id);
}

interface BookRepositoryInternal {
    <S extends Book> Mono<S> save(S entity);

    Flux<Book> findAllBy(Pageable pageable);

    Flux<Book> findAll();

    Mono<Book> findById(Long id);
    // this is not supported at the moment because of https://github.com/jhipster/generator-jhipster/issues/18269
    // Flux<Book> findAllBy(Pageable pageable, Criteria criteria);

    Mono<Book> findOneWithEagerRelationships(Long id);

    Flux<Book> findAllWithEagerRelationships();

    Flux<Book> findAllWithEagerRelationships(Pageable page);

    Mono<Void> deleteById(Long id);
}
