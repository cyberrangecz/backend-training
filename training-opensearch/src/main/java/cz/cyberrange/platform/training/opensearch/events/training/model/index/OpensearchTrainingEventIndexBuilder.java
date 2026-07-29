package cz.cyberrange.platform.training.opensearch.events.training.model.index;

/**
 * Builder for constructing OpenSearch training-event index name patterns.
 *
 * <p>Each segment defaults to {@code *} (wildcard), so calling {@link #builder()}.{@link #build()}
 * with no further configuration yields a pattern that matches all training-event indices.
 *
 * <p>Example:
 *
 * <pre>{@code
 * String idx = OpensearchTrainingEventIndexBuilder.builder()
 *     .pool(2L).sandbox("ba347345-...").definition(1L).instance(2L).run(4L)
 *     .build();
 * // → "crczp.events.trainings.pool=2.sandbox=ba347345-....definition=1.instance=2.run=4"
 * }</pre>
 *
 * <p><b>By default, the filter matches everything!</b>
 */
public class OpensearchTrainingEventIndexBuilder {

  private static final String TEMPLATE =
      "crczp.events.trainings.pool={}.sandbox={}.definition={}.instance={}.run={}";
  private static final String WILDCARD = "*";

  private Long pool = null;
  private String sandbox = null;
  private Long definition = null;
  private Long instance = null;
  private Long run = null;

  private OpensearchTrainingEventIndexBuilder() {}

  /**
   * Creates a new builder with all segments set to wildcard ({@code *}).
   *
   * @return a new {@link OpensearchTrainingEventIndexBuilder}
   */
  public static OpensearchTrainingEventIndexBuilder builder() {
    return new OpensearchTrainingEventIndexBuilder();
  }

  /**
   * Sets the pool segment.
   *
   * @param pool the pool ID; {@code null} produces {@code *}
   * @return this builder
   */
  public OpensearchTrainingEventIndexBuilder pool(Long pool) {
    this.pool = pool;
    return this;
  }

  /**
   * Sets the sandbox segment.
   *
   * @param sandbox the sandbox UUID; {@code null} produces {@code *}
   * @return this builder
   */
  public OpensearchTrainingEventIndexBuilder sandbox(String sandbox) {
    this.sandbox = sandbox;
    return this;
  }

  /**
   * Sets the training-definition segment.
   *
   * @param definition the training definition ID; {@code null} produces {@code *}
   * @return this builder
   */
  public OpensearchTrainingEventIndexBuilder definition(Long definition) {
    this.definition = definition;
    return this;
  }

  /**
   * Sets the training-instance segment.
   *
   * @param instance the training instance ID; {@code null} produces {@code *}
   * @return this builder
   */
  public OpensearchTrainingEventIndexBuilder instance(Long instance) {
    this.instance = instance;
    return this;
  }

  /**
   * Sets the training-run segment.
   *
   * @param run the training run ID; {@code null} produces {@code *}
   * @return this builder
   */
  public OpensearchTrainingEventIndexBuilder run(Long run) {
    this.run = run;
    return this;
  }

  /**
   * Builds the index name or wildcard pattern string from the current segment values.
   *
   * @return the formatted index name / wildcard pattern
   */
  public String build() {
    return TEMPLATE.formatted(
        segment(pool), segment(sandbox), segment(definition), segment(instance), segment(run));
  }

  private static String segment(Object value) {
    return value == null ? WILDCARD : String.valueOf(value);
  }
}
