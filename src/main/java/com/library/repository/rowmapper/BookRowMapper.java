package com.library.repository.rowmapper;

import com.library.domain.Book;
import com.library.domain.enumeration.BookStatusEnum;
import io.r2dbc.spi.Row;
import java.time.LocalDate;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link Book}, with proper type conversions.
 */
@Service
public class BookRowMapper implements BiFunction<Row, String, Book> {

    private final ColumnConverter converter;

    public BookRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link Book} stored in the database.
     */
    @Override
    public Book apply(Row row, String prefix) {
        Book entity = new Book();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setTitle(converter.fromRow(row, prefix + "_title", String.class));
        entity.setPublicationDate(converter.fromRow(row, prefix + "_publication_date", LocalDate.class));
        entity.setCopiesOwned(converter.fromRow(row, prefix + "_copies_owned", Integer.class));
        entity.setStatus(converter.fromRow(row, prefix + "_status", BookStatusEnum.class));
        entity.setCategoryId(converter.fromRow(row, prefix + "_category_id", Long.class));
        return entity;
    }
}
