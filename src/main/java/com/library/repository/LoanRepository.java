package com.library.repository;

import com.library.domain.Loan;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for the Loan entity.
 */
@SuppressWarnings("unused")
@Repository
public interface LoanRepository extends ReactiveCrudRepository<Loan, Long>, LoanRepositoryInternal {
    @Override
    Mono<Loan> findOneWithEagerRelationships(Long id);

    @Override
    Flux<Loan> findAllWithEagerRelationships();

    @Override
    Flux<Loan> findAllWithEagerRelationships(Pageable page);

    @Query("SELECT * FROM loan entity WHERE entity.book_id = :id")
    Flux<Loan> findByBook(Long id);

    @Query("SELECT * FROM loan entity WHERE entity.book_id IS NULL")
    Flux<Loan> findAllWhereBookIsNull();

    @Query("SELECT * FROM loan entity WHERE entity.member_id = :id")
    Flux<Loan> findByMember(Long id);

    @Query("SELECT * FROM loan entity WHERE entity.member_id IS NULL")
    Flux<Loan> findAllWhereMemberIsNull();

    @Override
    <S extends Loan> Mono<S> save(S entity);

    @Override
    Flux<Loan> findAll();

    @Override
    Mono<Loan> findById(Long id);

    @Override
    Mono<Void> deleteById(Long id);
}

interface LoanRepositoryInternal {
    <S extends Loan> Mono<S> save(S entity);

    Flux<Loan> findAllBy(Pageable pageable);

    Flux<Loan> findAll();

    Mono<Loan> findById(Long id);
    // this is not supported at the moment because of https://github.com/jhipster/generator-jhipster/issues/18269
    // Flux<Loan> findAllBy(Pageable pageable, Criteria criteria);

    Mono<Loan> findOneWithEagerRelationships(Long id);

    Flux<Loan> findAllWithEagerRelationships();

    Flux<Loan> findAllWithEagerRelationships(Pageable page);

    Mono<Void> deleteById(Long id);
}
