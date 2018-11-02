package cz.muni.ics.kypo.training.rest.utils.annotations;

import java.lang.annotation.*;

/**
 * Document info.
 *
 * @author Pavel Seda
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface DocumentInfo {

    String index();

    String type() default "events";

    boolean useServerConfiguration() default false;

    short shards() default 5;

    short replicas() default 1;

    String refreshInterval() default "1s";

    boolean createIndex() default true;

}
