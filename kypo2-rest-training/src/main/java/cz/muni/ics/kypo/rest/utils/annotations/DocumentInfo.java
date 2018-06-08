package cz.muni.ics.kypo.rest.utils.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Document info.
 *
 * @author Pavel Seda
 * 
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
