package cz.muni.ics.kypo.training.persistence.utils;

import org.hibernate.dialect.PostgreSQL9Dialect;

import java.sql.Types;

/**
 * The type Json postgre sql dialect.
 */
public class JsonPostgreSQLDialect extends PostgreSQL9Dialect {

    /**
     * Instantiates a new Json postgre sql dialect.
     */
    public JsonPostgreSQLDialect() {
        super();
        this.registerColumnType(Types.JAVA_OBJECT, "jsonb");
        this.registerColumnType(Types.JAVA_OBJECT, "json");
    }

}
