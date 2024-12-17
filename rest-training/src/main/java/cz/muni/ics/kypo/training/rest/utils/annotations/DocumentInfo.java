package cz.muni.ics.kypo.training.rest.utils.annotations;

import java.lang.annotation.*;

/**
 * Document info.
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface DocumentInfo {

    /**
     * Index string.
     *
     * @return the string
     */
    String index();

    /**
     * Type string.
     *
     * @return the string
     */
    String type() default "events";

    /**
     * Use server configuration boolean.
     *
     * @return the boolean
     */
    boolean useServerConfiguration() default false;

    /**
     * Shards short.
     *
     * @return the short
     */
    short shards() default 5;

    /**
     * Replicas short.
     *
     * @return the short
     */
    short replicas() default 1;

    /**
     * Refresh interval string.
     *
     * @return the string
     */
    String refreshInterval() default "1s";

    /**
     * Create index boolean.
     *
     * @return the boolean
     */
    boolean createIndex() default true;

}
