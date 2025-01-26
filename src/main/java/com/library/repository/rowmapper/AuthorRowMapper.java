package com.library.repository.rowmapper;

import com.library.domain.Author;
import io.r2dbc.spi.Row;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link Author}, with proper type conversions.
 */
@Service
public class AuthorRowMapper implements BiFunction<Row, String, Author> {

    private final ColumnConverter converter;

    public AuthorRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link Author} stored in the database.
     */
    @Override
    public Author apply(Row row, String prefix) {
        Author entity = new Author();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setFirstName(converter.fromRow(row, prefix + "_first_name", String.class));
        entity.setLastName(converter.fromRow(row, prefix + "_last_name", String.class));
        return entity;
    }
}
