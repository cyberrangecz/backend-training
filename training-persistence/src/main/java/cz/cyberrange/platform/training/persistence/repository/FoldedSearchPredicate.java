package cz.cyberrange.platform.training.persistence.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;

/**
 * Builds a database predicate matching a string column that contains a user-typed text, ignoring
 * case and accents. Both sides are folded by the {@code search_fold} database function, and the
 * wildcard characters of the typed text match literally.
 */
public final class FoldedSearchPredicate {

  private FoldedSearchPredicate() {}

  /**
   * Creates the predicate matching rows whose column contains the typed text.
   *
   * @param path the string column to search
   * @param rawQuery the text typed by the user, matched literally
   * @return the case- and accent-insensitive containment predicate
   */
  public static BooleanExpression contains(StringPath path, String rawQuery) {
    return Expressions.booleanTemplate(
        "cast(search_fold({0}) as string) like concat('%', cast(search_fold({1}) as string), '%') escape '!'",
        path,
        escapeWildcards(rawQuery));
  }

  private static String escapeWildcards(String rawQuery) {
    return rawQuery
        .replace("!", "!!")
        .replace("%", "!%")
        .replace("_", "!_");
  }
}
