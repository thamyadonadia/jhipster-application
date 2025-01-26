package com.library.repository;

import com.library.domain.Loan;
import com.library.repository.rowmapper.BookRowMapper;
import com.library.repository.rowmapper.LoanRowMapper;
import com.library.repository.rowmapper.ReaderRowMapper;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.convert.R2dbcConverter;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.repository.support.SimpleR2dbcRepository;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Comparison;
import org.springframework.data.relational.core.sql.Condition;
import org.springframework.data.relational.core.sql.Conditions;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.Select;
import org.springframework.data.relational.core.sql.SelectBuilder.SelectFromAndJoinCondition;
import org.springframework.data.relational.core.sql.Table;
import org.springframework.data.relational.repository.support.MappingRelationalEntityInformation;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.RowsFetchSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC custom repository implementation for the Loan entity.
 */
@SuppressWarnings("unused")
class LoanRepositoryInternalImpl extends SimpleR2dbcRepository<Loan, Long> implements LoanRepositoryInternal {

    private final DatabaseClient db;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final EntityManager entityManager;

    private final BookRowMapper bookMapper;
    private final ReaderRowMapper readerMapper;
    private final LoanRowMapper loanMapper;

    private static final Table entityTable = Table.aliased("loan", EntityManager.ENTITY_ALIAS);
    private static final Table bookTable = Table.aliased("book", "book");
    private static final Table memberTable = Table.aliased("reader", "e_member");

    public LoanRepositoryInternalImpl(
        R2dbcEntityTemplate template,
        EntityManager entityManager,
        BookRowMapper bookMapper,
        ReaderRowMapper readerMapper,
        LoanRowMapper loanMapper,
        R2dbcEntityOperations entityOperations,
        R2dbcConverter converter
    ) {
        super(
            new MappingRelationalEntityInformation(converter.getMappingContext().getRequiredPersistentEntity(Loan.class)),
            entityOperations,
            converter
        );
        this.db = template.getDatabaseClient();
        this.r2dbcEntityTemplate = template;
        this.entityManager = entityManager;
        this.bookMapper = bookMapper;
        this.readerMapper = readerMapper;
        this.loanMapper = loanMapper;
    }

    @Override
    public Flux<Loan> findAllBy(Pageable pageable) {
        return createQuery(pageable, null).all();
    }

    RowsFetchSpec<Loan> createQuery(Pageable pageable, Condition whereClause) {
        List<Expression> columns = LoanSqlHelper.getColumns(entityTable, EntityManager.ENTITY_ALIAS);
        columns.addAll(BookSqlHelper.getColumns(bookTable, "book"));
        columns.addAll(ReaderSqlHelper.getColumns(memberTable, "member"));
        SelectFromAndJoinCondition selectFrom = Select.builder()
            .select(columns)
            .from(entityTable)
            .leftOuterJoin(bookTable)
            .on(Column.create("book_id", entityTable))
            .equals(Column.create("id", bookTable))
            .leftOuterJoin(memberTable)
            .on(Column.create("member_id", entityTable))
            .equals(Column.create("id", memberTable));
        // we do not support Criteria here for now as of https://github.com/jhipster/generator-jhipster/issues/18269
        String select = entityManager.createSelect(selectFrom, Loan.class, pageable, whereClause);
        return db.sql(select).map(this::process);
    }

    @Override
    public Flux<Loan> findAll() {
        return findAllBy(null);
    }

    @Override
    public Mono<Loan> findById(Long id) {
        Comparison whereClause = Conditions.isEqual(entityTable.column("id"), Conditions.just(id.toString()));
        return createQuery(null, whereClause).one();
    }

    @Override
    public Mono<Loan> findOneWithEagerRelationships(Long id) {
        return findById(id);
    }

    @Override
    public Flux<Loan> findAllWithEagerRelationships() {
        return findAll();
    }

    @Override
    public Flux<Loan> findAllWithEagerRelationships(Pageable page) {
        return findAllBy(page);
    }

    private Loan process(Row row, RowMetadata metadata) {
        Loan entity = loanMapper.apply(row, "e");
        entity.setBook(bookMapper.apply(row, "book"));
        entity.setMember(readerMapper.apply(row, "member"));
        return entity;
    }

    @Override
    public <S extends Loan> Mono<S> save(S entity) {
        return super.save(entity);
    }
}
