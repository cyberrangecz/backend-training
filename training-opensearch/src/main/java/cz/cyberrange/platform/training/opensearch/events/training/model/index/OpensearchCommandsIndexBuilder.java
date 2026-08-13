package cz.cyberrange.platform.training.opensearch.events.training.model.index;

/**
 * Builder for constructing OpenSearch console-command event index name patterns.
 *
 * <p>Each segment defaults to {@code *} (wildcard), so calling {@link #builder()}.{@link #build()}
 * with no further configuration yields a pattern that matches all console-command event indices.
 *
 * <p>Example:
 *
 * <pre>{@code
 * String idx = OpensearchCommandsIndexBuilder.builder()
 *     .pool(2L).sandbox("ba347345-...")
 *     .build();
 * // → "crczp.logs.console.pool=2.sandbox=ba347345-..."
 * }</pre>
 *
 * <p><b>By default, the filter matches everything!</b>
 */
public class OpensearchCommandsIndexBuilder {

  private static final String TEMPLATE = "crczp.logs.console.pool=%s.sandbox=%s";
  private static final String WILDCARD = "*";

  private Long pool = null;
  private String sandbox = null;

  private OpensearchCommandsIndexBuilder() {}

  /**
   * Creates a new builder with all segments set to wildcard ({@code *}).
   *
   * @return a new {@link OpensearchCommandsIndexBuilder}
   */
  public static OpensearchCommandsIndexBuilder builder() {
    return new OpensearchCommandsIndexBuilder();
  }

  /**
   * Sets the pool segment.
   *
   * @param pool the pool ID; {@code null} produces {@code *}
   * @return this builder
   */
  public OpensearchCommandsIndexBuilder pool(Long pool) {
    this.pool = pool;
    return this;
  }

  /**
   * Sets the sandbox segment.
   *
   * @param sandbox the sandbox UUID; {@code null} produces {@code *}
   * @return this builder
   */
  public OpensearchCommandsIndexBuilder sandbox(String sandbox) {
    this.sandbox = sandbox;
    return this;
  }

  /**
   * Builds the index name or wildcard pattern string from the current segment values.
   *
   * @return the formatted index name / wildcard pattern
   */
  public String build() {
    return TEMPLATE.formatted(segment(pool), segment(sandbox));
  }

  private static String segment(Object value) {
    return value == null ? WILDCARD : String.valueOf(value);
  }
}
