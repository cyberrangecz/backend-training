package cz.muni.ics.kypo.training.utils;

import org.hibernate.dialect.PostgreSQL9Dialect;

import java.sql.Types;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
public class JsonPostgreSQLDialect extends PostgreSQL9Dialect {

  public JsonPostgreSQLDialect() {
    super();
    this.registerColumnType(Types.JAVA_OBJECT, "jsonb");
    this.registerColumnType(Types.JAVA_OBJECT, "json");
  }

}
