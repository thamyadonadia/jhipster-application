package com.library.repository;

import com.library.domain.Author;
import com.library.domain.Book;
import com.library.repository.rowmapper.BookRowMapper;
import com.library.repository.rowmapper.CategoryRowMapper;
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
 * Spring Data R2DBC custom repository implementation for the Book entity.
 */
@SuppressWarnings("unused")
class BookRepositoryInternalImpl extends SimpleR2dbcRepository<Book, Long> implements BookRepositoryInternal {

    private final DatabaseClient db;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final EntityManager entityManager;

    private final CategoryRowMapper categoryMapper;
    private final BookRowMapper bookMapper;

    private static final Table entityTable = Table.aliased("book", EntityManager.ENTITY_ALIAS);
    private static final Table categoryTable = Table.aliased("category", "category");

    private static final EntityManager.LinkTable authorLink = new EntityManager.LinkTable("rel_book__author", "book_id", "author_id");

    public BookRepositoryInternalImpl(
        R2dbcEntityTemplate template,
        EntityManager entityManager,
        CategoryRowMapper categoryMapper,
        BookRowMapper bookMapper,
        R2dbcEntityOperations entityOperations,
        R2dbcConverter converter
    ) {
        super(
            new MappingRelationalEntityInformation(converter.getMappingContext().getRequiredPersistentEntity(Book.class)),
            entityOperations,
            converter
        );
        this.db = template.getDatabaseClient();
        this.r2dbcEntityTemplate = template;
        this.entityManager = entityManager;
        this.categoryMapper = categoryMapper;
        this.bookMapper = bookMapper;
    }

    @Override
    public Flux<Book> findAllBy(Pageable pageable) {
        return createQuery(pageable, null).all();
    }

    RowsFetchSpec<Book> createQuery(Pageable pageable, Condition whereClause) {
        List<Expression> columns = BookSqlHelper.getColumns(entityTable, EntityManager.ENTITY_ALIAS);
        columns.addAll(CategorySqlHelper.getColumns(categoryTable, "category"));
        SelectFromAndJoinCondition selectFrom = Select.builder()
            .select(columns)
            .from(entityTable)
            .leftOuterJoin(categoryTable)
            .on(Column.create("category_id", entityTable))
            .equals(Column.create("id", categoryTable));
        // we do not support Criteria here for now as of https://github.com/jhipster/generator-jhipster/issues/18269
        String select = entityManager.createSelect(selectFrom, Book.class, pageable, whereClause);
        return db.sql(select).map(this::process);
    }

    @Override
    public Flux<Book> findAll() {
        return findAllBy(null);
    }

    @Override
    public Mono<Book> findById(Long id) {
        Comparison whereClause = Conditions.isEqual(entityTable.column("id"), Conditions.just(id.toString()));
        return createQuery(null, whereClause).one();
    }

    @Override
    public Mono<Book> findOneWithEagerRelationships(Long id) {
        return findById(id);
    }

    @Override
    public Flux<Book> findAllWithEagerRelationships() {
        return findAll();
    }

    @Override
    public Flux<Book> findAllWithEagerRelationships(Pageable page) {
        return findAllBy(page);
    }

    private Book process(Row row, RowMetadata metadata) {
        Book entity = bookMapper.apply(row, "e");
        entity.setCategory(categoryMapper.apply(row, "category"));
        return entity;
    }

    @Override
    public <S extends Book> Mono<S> save(S entity) {
        return super.save(entity).flatMap((S e) -> updateRelations(e));
    }

    protected <S extends Book> Mono<S> updateRelations(S entity) {
        Mono<Void> result = entityManager
            .updateLinkTable(authorLink, entity.getId(), entity.getAuthors().stream().map(Author::getId))
            .then();
        return result.thenReturn(entity);
    }

    @Override
    public Mono<Void> deleteById(Long entityId) {
        return deleteRelations(entityId).then(super.deleteById(entityId));
    }

    protected Mono<Void> deleteRelations(Long entityId) {
        return entityManager.deleteFromLinkTable(authorLink, entityId);
    }
}
